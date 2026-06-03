package rest;

import chat.Datatype.DtTarea;
import chat.Enum.EstadoTarea;
import chat.Sistema.ISistema;
import chat.clases.Tarea;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
            List<DtTarea> dtoList = tareas.stream().map(t -> new DtTarea(
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
            )).collect(Collectors.toList());
            return Response.ok(dtoList).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
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
            sistema.eliminarTarea(tareaId, usuarioId);
            return Response.noContent().build();
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
}
