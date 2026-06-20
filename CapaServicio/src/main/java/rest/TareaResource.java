package rest;

import chat.Datatype.DtTarea;
import chat.Enum.EstadoTarea;
import chat.Sistema.ISistema;
import chat.clases.Tarea;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import websocket.ChatWebSocketEndpoint;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Path("/tareas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TareaResource {

    @Inject
    private ISistema sistema;

    @GET
    @Path("/{usuarioId}")
    public Response obtenerTareasDeUsuario(@PathParam("usuarioId") Long usuarioId) {
        try {
            List<Tarea> tareas = sistema.obtenerTareasDeUsuario(usuarioId);
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
                    throw new RuntimeException("Error mapeando tarea id=" + t.getId() + ": " + ex.getMessage(), ex);
                }
            }).collect(Collectors.toList());
            return Response.ok(dtoList).build();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(sw.toString()))
                    .build();
        }
    }

    @POST
    public Response crearTarea(NuevaTareaRequest req) {
        try {
            Tarea t = sistema.crearTarea(
                    req.titulo,
                    req.contenido,
                    req.fechaVencimiento != null ? LocalDateTime.parse(req.fechaVencimiento) : null,
                    req.creadorId,
                    req.asignadoAId,
                    req.grupoId
            );
            DtTarea dto = new DtTarea(
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
            
            // Notificar al asignado por WebSocket si hay un asignado distinto al creador
            if (t.getAsignadoA() != null && !t.getAsignadoA().getId().equals(t.getCreador().getId())) {
                ChatWebSocketEndpoint.notificarAUsuario(t.getAsignadoA().getId(), "nuevaTarea", dto);
            }
            
            return Response.status(Response.Status.CREATED).entity(dto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{tareaId}/estado")
    public Response actualizarEstadoTarea(@PathParam("tareaId") Long tareaId, ActualizarEstadoRequest req) {
        try {
            Tarea t = sistema.actualizarEstadoTarea(tareaId, req.estado, req.usuarioId);
            DtTarea dto = new DtTarea(
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
            
            // Si el estado de la tarea cambia, podemos notificar al creador de la tarea (si el que la cambia es el asignado)
            if (t.getCreador() != null && !t.getCreador().getId().equals(req.usuarioId)) {
                ChatWebSocketEndpoint.notificarAUsuario(t.getCreador().getId(), "tareaActualizada", dto);
            }
            // O al asignado (si el que la cambia es el creador u otro)
            if (t.getAsignadoA() != null && !t.getAsignadoA().getId().equals(req.usuarioId)) {
                ChatWebSocketEndpoint.notificarAUsuario(t.getAsignadoA().getId(), "tareaActualizada", dto);
            }

            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{tareaId}")
    public Response eliminarTarea(@PathParam("tareaId") Long tareaId, @QueryParam("usuarioId") Long usuarioId) {
        try {
            // Obtenemos la tarea antes de eliminarla para saber a quién notificar
            Tarea t = sistema.obtenerTareasDeUsuario(usuarioId).stream()
                    .filter(tarea -> tarea.getId().equals(tareaId))
                    .findFirst().orElse(null);
                    
            sistema.eliminarTarea(tareaId, usuarioId);
            
            if (t != null) {
                if (t.getCreador() != null && !t.getCreador().getId().equals(usuarioId)) {
                    ChatWebSocketEndpoint.notificarAUsuario(t.getCreador().getId(), "tareaEliminada", tareaId);
                }
                if (t.getAsignadoA() != null && !t.getAsignadoA().getId().equals(usuarioId)) {
                    ChatWebSocketEndpoint.notificarAUsuario(t.getAsignadoA().getId(), "tareaEliminada", tareaId);
                }
            }
            
            return Response.ok(new MessageResponse("Tarea eliminada exitosamente")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    public static class NuevaTareaRequest {
        public String titulo;
        public String contenido;
        public String fechaVencimiento;
        public Long creadorId;
        public Long asignadoAId;
        public Long grupoId;
    }

    public static class ActualizarEstadoRequest {
        public EstadoTarea estado;
        public Long usuarioId;
    }

    public static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }

    public static class MessageResponse {
        public String message;
        public MessageResponse(String message) { this.message = message; }
    }
}
