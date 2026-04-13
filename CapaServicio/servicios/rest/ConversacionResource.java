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
    public Response listConversaciones() {
        // TODO: obtener conversaciones del usuario autenticado

        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: listConversaciones").build();
    }

    @POST
    public Response createConversacion(DtConversacion dto) {
        // TODO: crear conversación mediante la capa de negocio
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: createConversacion").build();
    }

    @GET
    @Path("/{id}")
    public Response getConversacion(@PathParam("id") Long id) {
        // TODO: devolver conversación por id
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: getConversacion").build();
    }
}
