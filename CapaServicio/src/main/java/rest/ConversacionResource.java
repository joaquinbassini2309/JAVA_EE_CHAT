package rest;

import chat.Datatype.DtConversacion;
import chat.Datatype.DtMensaje;
import chat.Sistema.ISistema;
import exceptions.ErrorResponse;
import seguridad.AuthService;
import seguridad.Secured;

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
@Secured
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
    public Response createConversacion(DtConversacion dto, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (dto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Request body is required")).build();
        }

        List<Long> participantes = dto.getParticipanteIds();
        int cantidadParticipantes = participantes == null ? 0 : participantes.size();
        boolean parecePrivada = dto.getTipo() == chat.Enum.TipoConversacion.PRIVADA
                || (dto.getTipo() == null && cantidadParticipantes == 1);

        // Conversacion privada: exactamente 1 participante adicional
        if (parecePrivada) {
            if (cantidadParticipantes != 1) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "For private chat provide exactly 1 participant")).build();
            }
            Long otroId = participantes.getFirst();
            if (otroId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "participant id is required")).build();
            }
            chat.clases.Conversacion creada = sistema.iniciarChatPrivado(userId, otroId);
            DtConversacion res = DtConversacion.from(creada);
            return Response.status(Response.Status.CREATED).entity(res).build();
        }

        // Conversacion Grupo
        List<Long> miembros = participantes == null ? List.of() : participantes;
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Group name is required")).build();
        }
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

    @GET
    @Path("/{id}/mensajes")
    public Response getMensajesConversacion(@PathParam("id") Long id,
            @QueryParam("limit") @DefaultValue("50") int limit, @Context SecurityContext securityContext) {
        Long userId = authService.getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        System.out.println("DEBUG: getMensajesConversacion - userId: " + userId + ", conversacionId: " + id);
        if (!sistema.usuarioEstaEnConversacion(userId, id)) {
            System.out.println("DEBUG: Acceso denegado para usuario " + userId + " en conversacion " + id);
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
            System.out.println("DEBUG: Añadiendo usuario " + newUserId + " a conversacion " + id + " por admin " + currentUserId);
            sistema.agregarMiembroAGrupo(id, newUserId, currentUserId);
            return Response.ok().build();
        } catch (Exception e) {
            System.err.println("ERROR al añadir participante: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error adding participant", e.getMessage())).build();
        }
    }
}