package chat.servicios.rest;

import chat.DtUsuario;
import chat.Sistema.ISistema;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    private ISistema sistema; // fachada de la capa de negocio

    // DTO simple para login; el usuario podrá reemplazar por su propio DTO
    public static class Credentials {
        public String username;
        public String password;
    }

    @POST
    @Path("/login")
    public Response login(Credentials credentials) {
        // TODO: validar input, invocar sistema.login y devolver JWT en caso de éxito
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: login").build();
    }

    @POST
    @Path("/register")
    public Response register(DtUsuario usuario) {
        // TODO: llamar a sistema para crear usuario y devolver 201 con Location
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: register").build();
    }

    @PUT
    @Path("/perfil")
    public Response updateProfile(DtUsuario usuario) {
        // TODO: actualizar perfil del usuario autenticado
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("TODO: updateProfile").build();
    }
}
