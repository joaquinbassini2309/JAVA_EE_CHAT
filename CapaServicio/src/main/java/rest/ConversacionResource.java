package rest;

import chat.Datatype.DtConversacion;
import chat.Datatype.DtMensaje;
import chat.Enum.RolParticipante;
import chat.Enum.TipoConversacion;
import chat.Sistema.ISistema;
import exceptions.ErrorResponse;
import seguridad.AuthService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/conversaciones")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConversacionResource {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

    @GET
    public Response listConversaciones(@Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        List<chat.clases.Conversacion> convs = sistema.obtenerConversacionesDeUsuario(userId);
        final String username = sistema.buscarUsuarioPorId(userId).map(chat.clases.Usuario::getUsername).orElse("");

        List<DtConversacion> dtos = convs.stream()
                .map(conv -> {
                    chat.clases.Mensaje ultimo = sistema.buscarUltimoMensaje(conv.getId()).orElse(null);
                    DtConversacion dto = DtConversacion.from(conv, userId, ultimo);
                    int noLeidos = sistema.mensajeHandler().contarMensajesSinLeer(conv.getId(), userId).intValue();
                    dto.setNoLeidos(noLeidos);
                    
                    int menciones = 0;
                    if (!username.isEmpty()) {
                        menciones = sistema.mensajeHandler().contarMencionesSinLeer(conv.getId(), userId, username).intValue();
                    }
                    dto.setMencionesSinLeer(menciones);
                    return dto;
                })
                .collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    @POST
    public Response createConversacion(CreateConversacionDTO dto, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (dto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Request body is required")).build();
        }

        // Conversacion tipo AVISO (Canal de avisos)
        if (dto.getTipo() == TipoConversacion.AVISO) {
            chat.clases.Conversacion creada = sistema.crearCanalAvisos(dto.getNombre(), userId);
            notificarNuevaConversacion(creada);
            DtConversacion res = DtConversacion.from(creada, userId);
            return Response.status(Response.Status.CREATED).entity(res).build();
        }

        // Conversacion privada: Se espera exactamente 1 participante adicional
        if (dto.getTipo() == TipoConversacion.PRIVADA) {
            List<Long> participantes = dto.getParticipanteIds();

            if (participantes == null || participantes.size() != 1) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "For private chat provide exactly 1 participant")).build();
            }
            Long otroId = participantes.getFirst();
            chat.clases.Conversacion creada = sistema.iniciarChatPrivado(userId, otroId);
            notificarNuevaConversacion(creada);
            DtConversacion res = DtConversacion.from(creada, userId);
            return Response.status(Response.Status.CREATED).entity(res).build();
        }

        // Conversacion Grupo
        List<Long> miembros = dto.getParticipanteIds() == null ? List.of() : dto.getParticipanteIds();
        chat.clases.Conversacion creada = sistema.crearGrupo(dto.getNombre(), userId, miembros);
        notificarNuevaConversacion(creada);
        DtConversacion res = DtConversacion.from(creada, userId);

        return Response.status(Response.Status.CREATED).entity(res).build();
    }

    private void notificarNuevaConversacion(chat.clases.Conversacion creada) {
        if (creada == null || creada.getParticipantes() == null) return;
        for (chat.clases.Participante participante : creada.getParticipantes()) {
            if (participante.getUsuario() != null) {
                websocket.PresenciaWebSocketEndpoint.notificarAUsuario(
                    participante.getUsuario().getId(), 
                    "NUEVA_CONVERSACION", 
                    DtConversacion.from(creada, participante.getUsuario().getId())
                );
            }
        }
    }

    @GET
    @Path("/{id: \\d+}")
    public Response getConversacion(@PathParam("id") Long id,
            @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (!sistema.usuarioEstaEnConversacion(userId, id)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "Access denied to conversation")).build();
        }

        Optional<chat.clases.Conversacion> opt = sistema.buscarConversacionPorId(id);

        if (opt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Conversation not found")).build();
        }

        DtConversacion dto = DtConversacion.from(opt.get(), userId);
        int noLeidos = sistema.mensajeHandler().contarMensajesSinLeer(id, userId).intValue();
        dto.setNoLeidos(noLeidos);
        
        String username = sistema.buscarUsuarioPorId(userId).map(chat.clases.Usuario::getUsername).orElse("");
        int menciones = 0;
        if (!username.isEmpty()) {
            menciones = sistema.mensajeHandler().contarMencionesSinLeer(id, userId, username).intValue();
        }
        dto.setMencionesSinLeer(menciones);
        return Response.ok(dto).build();
    }

    @PUT
    @Path("/{id: \\d+}")
    public Response updateConversacion(@PathParam("id") Long id, UpdateConversacionDTO dto, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            sistema.actualizarInfoGrupo(id, dto.getNombre(), dto.getFotoUrl(), dto.getImagenBanner(), userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(500, "Error updating group")).build();
        }
    }

    @GET
    @Path("/{id: \\d+}/mensajes")
    public Response getMensajesConversacion(@PathParam("id") Long id,
            @QueryParam("limit") @DefaultValue("6") int limit, 
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (!sistema.usuarioEstaEnConversacion(userId, id)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "Access denied to conversation")).build();
        }

        List<chat.clases.Mensaje> mensajes = sistema.obtenerMensajesDeConversacion(id, userId, limit, offset);
        List<DtMensaje> dtos = mensajes.stream().map(DtMensaje::from).collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    @POST
    @Path("/{id: \\d+}/participantes")
    public Response addParticipant(@PathParam("id") Long id, AddParticipantDTO dto, @Context SecurityContext securityContext) {
        Long currentUserId = authService.getAuthenticatedUserId(securityContext);
        if (currentUserId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        if (dto == null || dto.getUsuarioId() == null) return Response.status(Response.Status.BAD_REQUEST).build();
        Long newUserId = dto.getUsuarioId();

        try {
            sistema.agregarMiembroAGrupo(id, newUserId, currentUserId);
            
            // Obtener la info actualizada de la conversacion y el nuevo participante
            chat.clases.Conversacion conv = sistema.buscarConversacionPorId(id).orElse(null);
            if (conv != null) {
                DtConversacion dtoConv = DtConversacion.from(conv, newUserId);
                
                // 1. Notificar al nuevo usuario que ha sido añadido a un grupo
                websocket.PresenciaWebSocketEndpoint.notificarAUsuario(newUserId, "NUEVA_CONVERSACION", dtoConv);
                
                // 2. Notificar a los miembros existentes que hay un nuevo participante
                java.util.Map<String, Object> payloadParticipante = new java.util.HashMap<>();
                payloadParticipante.put("conversacionId", id);
                
                chat.Datatype.DtParticipante dtoParticipante = conv.getParticipantes().stream()
                        .filter(p -> p.getUsuario().getId().equals(newUserId))
                        .map(chat.Datatype.DtParticipante::from)
                        .findFirst().orElse(null);
                        
                payloadParticipante.put("participante", dtoParticipante);
                
                if (dtoParticipante != null) {
                    for (chat.clases.Participante p : conv.getParticipantes()) {
                        Long pId = p.getUsuario().getId();
                        if (!pId.equals(newUserId)) {
                            websocket.PresenciaWebSocketEndpoint.notificarAUsuario(pId, "NUEVO_PARTICIPANTE", payloadParticipante);
                        }
                    }
                }
            }
            
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error adding participant")).build();
        }
    }

    @DELETE
    @Path("/{id: \\d+}/participantes/{participanteId: \\d+}")
    public Response removeParticipant(@PathParam("id") Long conversacionId, @PathParam("participanteId") Long participanteId, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            sistema.removerMiembroDeGrupo(conversacionId, participanteId, userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(500, "Error removing participant")).build();
        }
    }

    @PUT
    @Path("/{id: \\d+}/participantes/{participanteId: \\d+}/rol")
    public Response updateParticipantRole(@PathParam("id") Long conversacionId, @PathParam("participanteId") Long participanteId, UpdateRoleDTO dto, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            sistema.cambiarRolParticipante(conversacionId, participanteId, dto.getRol(), userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(500, "Error updating role")).build();
        }
    }

    @GET
    @Path("/canales")
    public Response listCanalesAvisos(@Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        List<chat.clases.Conversacion> canales = sistema.listarCanalesAvisos();
        final String username = sistema.buscarUsuarioPorId(userId).map(chat.clases.Usuario::getUsername).orElse("");
        List<DtConversacion> dtos = canales.stream()
                .map(canal -> {
                    DtConversacion dto = DtConversacion.from(canal, userId);
                    int noLeidos = sistema.mensajeHandler().contarMensajesSinLeer(canal.getId(), userId).intValue();
                    dto.setNoLeidos(noLeidos);
                    
                    int menciones = 0;
                    if (!username.isEmpty()) {
                        menciones = sistema.mensajeHandler().contarMencionesSinLeer(canal.getId(), userId, username).intValue();
                    }
                    dto.setMencionesSinLeer(menciones);
                    return dto;
                })
                .collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    @POST
    @Path("/canales/{id}/unirse")
    public Response unirseACanalAvisos(@PathParam("id") Long canalId, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            sistema.unirseACanalAvisos(canalId, userId);
            return Response.ok("{\"message\": \"Unido exitosamente al canal de avisos\"}").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id: \\d+}/rol")
    public Response getUserRoleInConversacion(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        Optional<chat.clases.Participante> partOpt = sistema.participanteHandler().buscarParticipante(id, userId);
        if (partOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "No es participante de esta conversación")).build();
        }

        String rol = partOpt.get().getRol().toString();
        return Response.ok("{\"rol\":\"" + rol + "\"}").build();
    }

    @POST
    @Path("/{id: \\d+}/fijar/{mensajeId: \\d+}")
    public Response fijarMensaje(@PathParam("id") Long conversacionId,
                                 @PathParam("mensajeId") Long mensajeId,
                                 @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            sistema.fijarMensaje(conversacionId, mensajeId, userId);
            
            chat.clases.Mensaje mensaje = sistema.mensajeHandler().buscarPorId(mensajeId)
                    .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
            
            DtMensaje dtMensaje = DtMensaje.from(mensaje);
            websocket.ChatWebSocketEndpoint.notificarMensajeFijado(conversacionId, dtMensaje);
            
            return Response.ok(dtMensaje).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error al fijar mensaje")).build();
        }
    }

    @POST
    @Path("/{id: \\d+}/desfijar")
    public Response desfijarMensaje(@PathParam("id") Long conversacionId,
                                    @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            sistema.desfijarMensaje(conversacionId, userId);
            
            websocket.ChatWebSocketEndpoint.notificarMensajeDesfijado(conversacionId);
            
            return Response.ok("{\"message\": \"Mensaje desfijado exitosamente\"}").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error al desfijar mensaje")).build();
        }
    }

    @DELETE
    @Path("/{id: \\d+}")
    public Response eliminarConversacion(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            sistema.eliminarConversacion(id, userId);
            return Response.ok("{\"message\": \"Conversación eliminada exitosamente\"}").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error al eliminar conversación")).build();
        }
    }

    // --- DTOs ---
    public static class CreateConversacionDTO {
        private String nombre;
        private TipoConversacion tipo;
        private List<Long> participanteIds;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public TipoConversacion getTipo() { return tipo; }
        public void setTipo(TipoConversacion tipo) { this.tipo = tipo; }
        public List<Long> getParticipanteIds() { return participanteIds; }
        public void setParticipanteIds(List<Long> participanteIds) { this.participanteIds = participanteIds; }
    }

    public static class UpdateConversacionDTO {
        private String nombre;
        private String fotoUrl;
        private String imagenBanner;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getFotoUrl() { return fotoUrl; }
        public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
        public String getImagenBanner() { return imagenBanner; }
        public void setImagenBanner(String imagenBanner) { this.imagenBanner = imagenBanner; }
    }

    public static class UpdateRoleDTO {
        private RolParticipante rol;
        public RolParticipante getRol() { return rol; }
        public void setRol(RolParticipante rol) { this.rol = rol; }
    }

    public static class AddParticipantDTO {
        private Long usuarioId;
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    }
}