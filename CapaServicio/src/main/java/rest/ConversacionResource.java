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

        List<DtConversacion> dtos = convs.stream()
                .map(DtConversacion::from)
                .filter(d -> d.getUltimoMensaje() != null)
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

        // Conversacion privada: Se espera exactamente 1 participante adicional
        if (dto.getTipo() == TipoConversacion.PRIVADA) {
            List<Long> participantes = dto.getParticipanteIds();

            if (participantes == null || participantes.size() != 1) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "For private chat provide exactly 1 participant")).build();
            }
            Long otroId = participantes.getFirst();
            chat.clases.Conversacion creada = sistema.iniciarChatPrivado(userId, otroId);
            DtConversacion res = DtConversacion.from(creada);
            return Response.status(Response.Status.CREATED).entity(res).build();
        }

        // Conversacion Grupo
        List<Long> miembros = dto.getParticipanteIds() == null ? List.of() : dto.getParticipanteIds();
        chat.clases.Conversacion creada = sistema.crearGrupo(dto.getNombre(), userId, miembros);
        DtConversacion res = DtConversacion.from(creada);

        return Response.status(Response.Status.CREATED).entity(res).build();
    }

    @GET
    @Path("/{id}")
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

        DtConversacion dto = DtConversacion.from(opt.get());
        return Response.ok(dto).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateConversacion(@PathParam("id") Long id, UpdateConversacionDTO dto, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            sistema.actualizarInfoGrupo(id, dto.getNombre(), userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(500, "Error updating group", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id}/mensajes")
    public Response getMensajesConversacion(@PathParam("id") Long id,
            @QueryParam("limit") @DefaultValue("50") int limit, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (!sistema.usuarioEstaEnConversacion(userId, id)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "Access denied to conversation")).build();
        }

        List<chat.clases.Mensaje> mensajes = sistema.obtenerMensajesDeConversacion(id, userId, limit);
        List<DtMensaje> dtos = mensajes.stream().map(DtMensaje::from).collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    @POST
    @Path("/{id}/participantes")
    public Response addParticipant(@PathParam("id") Long id, Map<String, Long> body, @Context SecurityContext securityContext) {
        Long currentUserId = authService.getAuthenticatedUserId(securityContext);
        if (currentUserId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        Long newUserId = body.get("usuarioId");
        if (newUserId == null) return Response.status(Response.Status.BAD_REQUEST).build();

        try {
            sistema.agregarMiembroAGrupo(id, newUserId, currentUserId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error adding participant", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}/participantes/{participanteId}")
    public Response removeParticipant(@PathParam("id") Long conversacionId, @PathParam("participanteId") Long participanteId, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            sistema.removerMiembroDeGrupo(conversacionId, participanteId, userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(500, "Error removing participant", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/participantes/{participanteId}/rol")
    public Response updateParticipantRole(@PathParam("id") Long conversacionId, @PathParam("participanteId") Long participanteId, UpdateRoleDTO dto, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);
        if (userId == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            sistema.cambiarRolParticipante(conversacionId, participanteId, dto.getRol(), userId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(new ErrorResponse(403, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(500, "Error updating role", e.getMessage())).build();
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
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    public static class UpdateRoleDTO {
        private RolParticipante rol;
        public RolParticipante getRol() { return rol; }
        public void setRol(RolParticipante rol) { this.rol = rol; }
    }
}