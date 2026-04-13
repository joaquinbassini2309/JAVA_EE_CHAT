package chat.servicios.rest;

import chat.DtMensaje;
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

@Path("/api/v1/mensajes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MensajeResource {

    @Inject
    private ISistema sistema;

    @POST
    public Response sendMessage(DtMensaje mensaje) {
        // TODO: validar JWT, crear mensaje y notificar via WebSocket si procede
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: sendMessage").build();
    }

    @GET
    @Path("/{id}")
    public Response getMensaje(@PathParam("id") Long id) {
        // TODO: devolver mensaje por id
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: getMensaje").build();
    }
}
