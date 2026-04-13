package chat.servicios.rest;

import chat.DtConversacion;
import chat.Sistema.ISistema;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/api/v1/conversaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConversacionResource {

    @Inject
    private ISistema sistema;

    @GET
    public Response listConversaciones(@Context SecurityContext securityContext) {
        //obtener conversaciones del usuario autenticado
        Long userId = getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("No autorizado").build();
        }

        List<chat.clases.Conversacion> convs = sistema.obtenerConversacionesDeUsuario(userId);

        List<DtConversacion> dtos = convs.stream().map(DtConversacion::from).collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    @POST
    public Response createConversacion(DtConversacion dto, @Context SecurityContext securityContext) {
        //crear conversación mediante la capa de negocio
        Long userId = getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("No autorizado").build();
        }

        //Conversacion privada: Se espera exactamente 1 participante adicional
        if (dto.tipo() == chat.Enums.TipoConversacion.PRIVADA) {
            List<Long> participantes = dto.participanteIds();

            if (participantes == null || participantes.size() != 1) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Para chat privado proporcione 1 participante").build();
            }
            Long otroId = participantes.get(0);
            chat.clases.Conversacion creada = sistema.IniciarChatPrivado(userId, otroId);
            DtConversacion res = DtConversacion.from(creada);
            return Response.status(Response.Status.CREATED).entity(res).build();
        }

        // Conversacion Grupo
        List<Long> miembros = dto.participanteIds() == null ? List.of() : dto.participantesIds();
        chat.clases.Conversacion creada = sistema.crearGrupo(dto.nombre(), userId, miembros);
        DtConversacion res = DtConversacion.from(creada);

        return Response.status(Response.Status.CREATED).entity(res).build();
    }

    @GET
    @Path("/{id}")
    public Response getConversacion(@PathParam("id") Long id,
                                    @Context SecurityContext securityContext) {
        Long userId = getAuthenticatedUserId(securityContext);

        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("No autorizado").build();
        }

        if (!sistema.usuarioEstaEnConversacion(userId, id)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Acceso denegado a la conversación").build();
        }

        Optional<chat.model.Conversacion> opt = sistema.buscarConversacionPorId(id);

        if (opt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Conversación no encontrada").build();
        }

        DtConversacion dto = DtConversacion.from(opt.get());
        return Response.ok(dto).build();
    }
