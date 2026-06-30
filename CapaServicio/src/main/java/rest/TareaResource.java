package rest;

import chat.Datatype.DtTarea;
import chat.Enum.EstadoTarea;
import chat.Sistema.ISistema;
import chat.clases.Tarea;
import exceptions.ErrorResponse;
import seguridad.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import websocket.ChatWebSocketEndpoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Path("/tareas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TareaResource {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

    @Context
    private SecurityContext securityContext;

    /**
     * Obtener tareas del usuario autenticado.
     * El userId viene del JWT — nunca del path param.
     */
    @GET
    @Path("/{usuarioId}")
    public Response obtenerTareasDeUsuario(@PathParam("usuarioId") Long usuarioId) {
        Long usuarioAutenticadoId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioAutenticadoId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        // Validar que el usuario solo accede a sus propias tareas.
        if (!usuarioAutenticadoId.equals(usuarioId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, "Access denied")).build();
        }

        try {
            List<Tarea> tareas = sistema.obtenerTareasDeUsuario(usuarioAutenticadoId);
            List<DtTarea> dtoList = tareas.stream().map(t -> {
                try {
                    Long asignadoId = null;
                    String asignadoUsername = null;
                    Long grupoId = null;
                    String grupoNombre = null;
                    try { if (t.getAsignadoA() != null) { asignadoId = t.getAsignadoA().getId(); asignadoUsername = t.getAsignadoA().getUsername(); } } catch (Exception ignored) {}
                    try { if (t.getGrupo() != null) { grupoId = t.getGrupo().getId(); grupoNombre = t.getGrupo().getNombre(); } } catch (Exception ignored) {}
                    return new DtTarea(
                            t.getId(), t.getTitulo(), t.getContenido(),
                            t.getFechaVencimiento(), t.getFechaCreacion(), t.getEstado(),
                            t.getCreador().getId(), t.getCreador().getUsername(),
                            asignadoId, asignadoUsername, grupoId, grupoNombre
                    );
                } catch (Exception ex) {
                    throw new RuntimeException("Error mapeando tarea id=" + t.getId(), ex);
                }
            }).collect(Collectors.toList());
            return Response.ok(dtoList).build();
        } catch (Exception e) {
            // Nunca enviar stack trace al cliente.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error interno del servidor"))
                    .build();
        }
    }

    /**
     * Crear una tarea. El creadorId se toma del JWT — ignorar cualquier valor del body.
     */
    @POST
    public Response crearTarea(NuevaTareaRequest req) {
        Long usuarioAutenticadoId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioAutenticadoId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            // creadorId siempre del JWT — se ignora req.creadorId.
            Tarea t = sistema.crearTarea(
                    req.titulo,
                    req.contenido,
                    req.fechaVencimiento != null ? LocalDateTime.parse(req.fechaVencimiento) : null,
                    usuarioAutenticadoId,
                    req.asignadoAId,
                    req.grupoId
            );
            DtTarea dto = mapearTarea(t);

            // Notificar al asignado por WebSocket si es distinto al creador.
            if (t.getAsignadoA() != null && !t.getAsignadoA().getId().equals(t.getCreador().getId())) {
                ChatWebSocketEndpoint.notificarAUsuario(t.getAsignadoA().getId(), "nuevaTarea", dto);
            }

            return Response.status(Response.Status.CREATED).entity(dto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error interno del servidor"))
                    .build();
        }
    }

    /**
     * Actualizar estado de tarea. El usuarioId se extrae del JWT, no del body.
     */
    @PUT
    @Path("/{tareaId}/estado")
    public Response actualizarEstadoTarea(@PathParam("tareaId") Long tareaId, ActualizarEstadoRequest req) {
        Long usuarioAutenticadoId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioAutenticadoId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            // usuarioId siempre del JWT — se ignora req.usuarioId.
            Tarea t = sistema.actualizarEstadoTarea(tareaId, req.estado, usuarioAutenticadoId);
            DtTarea dto = mapearTarea(t);

            // Notificar por WebSocket.
            if (t.getCreador() != null && !t.getCreador().getId().equals(usuarioAutenticadoId)) {
                ChatWebSocketEndpoint.notificarAUsuario(t.getCreador().getId(), "tareaActualizada", dto);
            }
            if (t.getAsignadoA() != null && !t.getAsignadoA().getId().equals(usuarioAutenticadoId)) {
                ChatWebSocketEndpoint.notificarAUsuario(t.getAsignadoA().getId(), "tareaActualizada", dto);
            }

            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error interno del servidor"))
                    .build();
        }
    }

    /**
     * Eliminar tarea. El usuarioId se toma del JWT — nunca del query param.
     */
    @DELETE
    @Path("/{tareaId}")
    public Response eliminarTarea(@PathParam("tareaId") Long tareaId) {
        Long usuarioAutenticadoId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioAutenticadoId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            Tarea t = sistema.obtenerTareasDeUsuario(usuarioAutenticadoId).stream()
                    .filter(tarea -> tarea.getId().equals(tareaId))
                    .findFirst().orElse(null);

            sistema.eliminarTarea(tareaId, usuarioAutenticadoId);

            if (t != null) {
                if (t.getCreador() != null && !t.getCreador().getId().equals(usuarioAutenticadoId)) {
                    ChatWebSocketEndpoint.notificarAUsuario(t.getCreador().getId(), "tareaEliminada", tareaId);
                }
                if (t.getAsignadoA() != null && !t.getAsignadoA().getId().equals(usuarioAutenticadoId)) {
                    ChatWebSocketEndpoint.notificarAUsuario(t.getAsignadoA().getId(), "tareaEliminada", tareaId);
                }
            }

            return Response.ok(new MessageResponse("Tarea eliminada exitosamente")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(403, e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error interno del servidor"))
                    .build();
        }
    }

    private DtTarea mapearTarea(Tarea t) {
        return new DtTarea(
                t.getId(),
                t.getTitulo(),
                t.getContenido(),
                t.getFechaVencimiento(),
                t.getFechaCreacion(),
                t.getEstado(),
                t.getCreador().getId(),
                t.getCreador().getUsername(),
                t.getAsignadoA() != null ? t.getAsignadoA().getId() : null,
                t.getAsignadoA() != null ? t.getAsignadoA().getUsername() : null,
                t.getGrupo() != null ? t.getGrupo().getId() : null,
                t.getGrupo() != null ? t.getGrupo().getNombre() : null
        );
    }

    public static class NuevaTareaRequest {
        public String titulo;
        public String contenido;
        public String fechaVencimiento;
        public Long creadorId; // Ignorado — se usa el JWT.
        public Long asignadoAId;
        public Long grupoId;
    }

    public static class ActualizarEstadoRequest {
        public EstadoTarea estado;
        public Long usuarioId; // Ignorado — se usa el JWT.
    }

    public static class MessageResponse {
        public String message;
        public MessageResponse(String message) { this.message = message; }
    }
}
