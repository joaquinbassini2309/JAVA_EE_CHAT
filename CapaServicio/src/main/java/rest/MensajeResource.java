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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/mensajes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MensajeResource {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

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
    @Path("/{id}")
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
            @QueryParam("limite") Integer limite) {

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
            int lim = limite != null && limite > 0 ? limite : 50;
            List<Mensaje> mensajes = sistema.obtenerMensajesDeConversacion(conversacionId, usuarioId, lim);
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
    @Path("/{id}/leido")
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
            return Response.ok().build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error marking conversation as read", e.getMessage())).build();
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