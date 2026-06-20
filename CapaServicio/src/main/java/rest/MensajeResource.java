package rest;

import chat.Datatype.DtMensaje;
import chat.Enum.TipoMensaje;
import chat.Sistema.ISistema;
import chat.clases.Mensaje;
import exceptions.ErrorResponse;
import seguridad.AuthService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/mensajes")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MensajeResource {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

    // @Inject
    // private websocket.ChatWebSocketEndpoint chatWebSocketEndpoint;

    @Context
    private SecurityContext securityContext;

    /**
     * POST /api/v1/mensajes
     * Envía un mensaje a una conversación
     * 
     * Body: { conversacionId, contenido, tipoMensaje?, urlAdjunto? }
     */
    @POST
    public Response sendMessage(EnviarMensajeDTO mensajeDto) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (mensajeDto == null || mensajeDto.getConversacionId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "conversacionId is required")).build();
        }

        if (mensajeDto.getContenido() == null || mensajeDto.getContenido().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "contenido is required")).build();
        }

        // Validar que el usuario está en la conversación
        if (!sistema.usuarioEstaEnConversacion(usuarioId, mensajeDto.getConversacionId())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "You don't have access to this conversation")).build();
        }

        try {
            TipoMensaje tipo = mensajeDto.getTipoMensaje() != null ? 
                    mensajeDto.getTipoMensaje() : TipoMensaje.TEXTO;

            Mensaje mensaje = sistema.enviarMensajeConAdjunto(
                    mensajeDto.getConversacionId(),
                    usuarioId,
                    mensajeDto.getContenido(),
                    tipo,
                    mensajeDto.getUrlAdjunto()
            );

            DtMensaje respuesta = DtMensaje.from(mensaje);
            return Response.status(Response.Status.CREATED).entity(respuesta).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Validation error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error sending message", e.getMessage())).build();
        }
    }

    /**
     * GET /api/v1/mensajes/{id}
     * Obtiene un mensaje por ID
     */
    @GET
    @Path("/{id: \\d+}")
    public Response getMensaje(@PathParam("id") Long id) {
        if (id == null || id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid message ID")).build();
        }

        Optional<Mensaje> mensajeOpt = sistema.mensajeHandler().buscarPorId(id);
        
        if (mensajeOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Message not found")).build();
        }

        DtMensaje respuesta = DtMensaje.from(mensajeOpt.get());
        return Response.ok(respuesta).build();
    }

    /**
     * GET /api/v1/mensajes/conversacion/{conversacionId}
     * Obtiene el historial de mensajes de una conversación
     * 
     * Query params:
     *   - limite (default: 50)
     */
    @GET
    @Path("/conversacion/{conversacionId}")
    public Response getMensajesDeConversacion(
            @PathParam("conversacionId") Long conversacionId,
            @QueryParam("limite") Integer limite,
            @QueryParam("offset") @DefaultValue("0") int offset) {

        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (conversacionId == null || conversacionId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid conversation ID")).build();
        }

        System.out.println("DEBUG: MensajeResource.getMensajesDeConversacion - userId: " + usuarioId + ", conversacionId: " + conversacionId);
        // Validar que el usuario está en la conversación
        if (!sistema.usuarioEstaEnConversacion(usuarioId, conversacionId)) {
            System.out.println("DEBUG: MensajeResource - Acceso denegado para usuario " + usuarioId + " en conversacion " + conversacionId);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "You don't have access to this conversation")).build();
        }

        try {
            int lim = limite != null && limite > 0 ? limite : 6;
            List<Mensaje> mensajes = sistema.obtenerMensajesDeConversacion(conversacionId, usuarioId, lim, offset);
            List<DtMensaje> respuesta = mensajes.stream()
                    .map(DtMensaje::from)
                    .collect(Collectors.toList());

            return Response.ok(respuesta).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Validation error", e.getMessage())).build();
        }
    }

    /**
     * POST /api/v1/mensajes/{id}/leido
     * Marca un mensaje como leído
     */
    @POST
    @Path("/{id: \\d+}/leido")
    public Response marcarMensajeLeido(@PathParam("id") Long mensajeId) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (mensajeId == null || mensajeId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid message ID")).build();
        }

        try {
            sistema.marcarMensajeComoLeido(mensajeId, usuarioId);
            Optional<Mensaje> msgOpt = sistema.mensajeHandler().buscarPorId(mensajeId);
            if (msgOpt.isPresent()) {
                Long conversacionId = msgOpt.get().getConversacion().getId();
                websocket.ChatWebSocketEndpoint.difundirMensajesLeidos(conversacionId, usuarioId);
            }
            return Response.ok().build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error marking message as read", e.getMessage())).build();
        }
    }

    /**
     * POST /api/v1/mensajes/conversacion/{conversacionId}/leidos
     * Marca todos los mensajes de una conversación como leídos
     */
    @POST
    @Path("/conversacion/{conversacionId}/leidos")
    public Response marcarConversacionLeida(@PathParam("conversacionId") Long conversacionId) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (conversacionId == null || conversacionId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid conversation ID")).build();
        }

        // Validar que el usuario está en la conversación
        if (!sistema.usuarioEstaEnConversacion(usuarioId, conversacionId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "You don't have access to this conversation")).build();
        }

        try {
            sistema.marcarConversacionComoLeida(conversacionId, usuarioId);
            websocket.ChatWebSocketEndpoint.difundirMensajesLeidos(conversacionId, usuarioId);
            return Response.ok().build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error marking conversation as read", e.getMessage())).build();
        }
    }

    /**
     * DELETE /api/v1/mensajes/{id}
     * Elimina un mensaje. Solo el emisor puede hacerlo.
     */
    @DELETE
    @Path("/{id: \\d+}")
    public Response eliminarMensaje(@PathParam("id") Long mensajeId) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (mensajeId == null || mensajeId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid message ID")).build();
        }

        try {
            Optional<chat.clases.Mensaje> msgOptBefore = sistema.mensajeHandler().buscarPorId(mensajeId);
            if (msgOptBefore.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse(404, "Message not found")).build();
            }
            Long conversacionId = msgOptBefore.get().getConversacion().getId();

            sistema.eliminarMensaje(mensajeId, usuarioId);

            Optional<chat.clases.Mensaje> msgOptAfter = sistema.mensajeHandler().buscarPorId(mensajeId);
            if (msgOptAfter.isPresent()) {
                DtMensaje dt = DtMensaje.from(msgOptAfter.get());
                websocket.ChatWebSocketEndpoint.difundirMensajeEliminado(conversacionId, dt);
            }

            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            // This exception is thrown by Sistema for "Not found" or "Not allowed"
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "Error deleting message", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Could not delete message", e.getMessage())).build();
        }
    }

    /**
     * GET /api/v1/mensajes/{id}/info
     * Obtiene información detallada de un mensaje.
     */
    @GET
    @Path("/{id: \\d+}/info")
    public Response getInfoMensaje(@PathParam("id") Long mensajeId) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (mensajeId == null || mensajeId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid message ID")).build();
        }

        try {
            Optional<Mensaje> mensajeOpt = sistema.obtenerInfoMensaje(mensajeId, usuarioId);

            if (mensajeOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse(404, "Message not found or access denied")).build();
            }

            DtMensaje respuesta = DtMensaje.from(mensajeOpt.get());
            return Response.ok(respuesta).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "Access denied", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error getting message info", e.getMessage())).build();
        }
    }

    /**
     * POST /api/v1/mensajes/{id}/resaltar
     * Resalta un mensaje con un color
     * Query param: color (puede ser null o vacío para quitar el resaltado)
     */
    @POST
    @Path("/{id: \\d+}/resaltar")
    public Response resaltarMensaje(
            @PathParam("id") Long mensajeId,
            @QueryParam("color") String color) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (mensajeId == null || mensajeId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid message ID")).build();
        }

        try {
            sistema.resaltarMensaje(mensajeId, color, usuarioId);

            Optional<Mensaje> mensajeOpt = sistema.mensajeHandler().buscarPorId(mensajeId);
            if (mensajeOpt.isPresent()) {
                DtMensaje dt = DtMensaje.from(mensajeOpt.get());
                websocket.ChatWebSocketEndpoint.difundirMensajeResaltado(mensajeOpt.get().getConversacion().getId(), dt);
                return Response.ok(dt).build();
            }
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error highlighting message", e.getMessage())).build();
        }
    }

    /**
     * DTO para enviar mensajes
     */
    public static class EnviarMensajeDTO {
        private Long conversacionId;
        private String contenido;
        private TipoMensaje tipoMensaje;
        private String urlAdjunto;

        public EnviarMensajeDTO() {}

        public EnviarMensajeDTO(Long conversacionId, String contenido, TipoMensaje tipoMensaje, String urlAdjunto) {
            this.conversacionId = conversacionId;
            this.contenido = contenido;
            this.tipoMensaje = tipoMensaje;
            this.urlAdjunto = urlAdjunto;
        }

        public Long getConversacionId() { return conversacionId; }
        public void setConversacionId(Long conversacionId) { this.conversacionId = conversacionId; }

        public String getContenido() { return contenido; }
        public void setContenido(String contenido) { this.contenido = contenido; }

        public TipoMensaje getTipoMensaje() { return tipoMensaje; }
        public void setTipoMensaje(TipoMensaje tipoMensaje) { this.tipoMensaje = tipoMensaje; }

        public String getUrlAdjunto() { return urlAdjunto; }
        public void setUrlAdjunto(String urlAdjunto) { this.urlAdjunto = urlAdjunto; }
    }
}