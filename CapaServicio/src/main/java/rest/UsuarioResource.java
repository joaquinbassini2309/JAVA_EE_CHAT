package rest;

import chat.Datatype.DtUsuario;
import chat.Sistema.ISistema;
import chat.clases.Usuario;
import exceptions.ErrorResponse;
import seguridad.AuthService;
import seguridad.JWTUtil;
import seguridad.Secured; // Importar la anotación
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.mindrot.jbcrypt.BCrypt;

import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/usuarios")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

    @Inject
    private JWTUtil jwtUtil;

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured // Proteger este endpoint
    public Response getAllUsers() {
        List<Usuario> usuarios = sistema.listarUsuarios();
        List<DtUsuario.UsuarioResponseDTO> dtos = usuarios.stream()
                .map(DtUsuario.UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @POST
    @Path("/login")
    // NO @Secured aquí, es público
    public Response login(String rawBody) {
        JsonObject body = parseJsonBody(rawBody);
        if (body == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid JSON body")).build();
        }

        String email = getString(body, "email");
        String password = getString(body, "password");

        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Email is required")).build();
        }

        if (password == null || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Password is required")).build();
        }

        return sistema.buscarUsuarioPorEmail(email)
                .filter(usuario -> BCrypt.checkpw(password, usuario.getPasswordHash()))
                .map(usuario -> {
                    String token = generateToken(usuario);
                    DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);
                    DtUsuario.AuthResponseDTO authResponse = new DtUsuario.AuthResponseDTO(token, usuarioDto);
                    return Response.ok(authResponse).build();
                })
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(401, "Invalid credentials")).build());
    }

    @POST
    @Path("/register")
    // NO @Secured aquí, es público
    public Response register(String rawBody) {
        JsonObject body = parseJsonBody(rawBody);
        if (body == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid JSON body")).build();
        }

        if (body == null || body.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Request body is required")).build();
        }

        String username = getString(body, "username");
        String email = getString(body, "email");
        String password = getString(body, "password");

        if (username == null || username.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Username is required")).build();
        }

        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Email is required")).build();
        }

        if (password == null || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Password is required")).build();
        }

        try {
            Usuario usuario = sistema.registrarUsuario(username, email, password);
            DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);

            URI location = URI.create("/api/v1/usuarios/" + usuario.getId());

            return Response.created(location).entity(usuarioDto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Validation error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error registering user", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/perfil")
    @Secured // Proteger este endpoint
    public Response updateProfile(DtUsuario.ActualizarUsuarioDTO actualizarDto) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        // Primero, verificar que el usuario existe.
        return sistema.buscarUsuarioPorId(usuarioId).map(usuario -> {
            // Si hay datos para actualizar, se actualizan.
            if (actualizarDto != null) {
                sistema.actualizarPerfilUsuario(usuarioId, actualizarDto.getFotoUrl(), actualizarDto.getEstado());
            }

            // Se obtiene el usuario (potencialmente actualizado) y se devuelve.
            Usuario usuarioActualizado = sistema.buscarUsuarioPorId(usuarioId)
                    .orElseThrow(() -> new IllegalStateException("User disappeared during update")); // No debería ocurrir

            DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuarioActualizado);
            return Response.ok(usuarioDto).build();

        }).orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(404, "User not found")).build());
    }

    private String generateToken(Usuario usuario) {
        return jwtUtil.generarToken(usuario.getId(), usuario.getUsername());
    }

    private String getString(JsonObject body, String key) {
        if (body == null || key == null || !body.containsKey(key) || body.isNull(key)) {
            return null;
        }
        return body.getString(key, null);
    }

    private JsonObject parseJsonBody(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }
        try (JsonReader reader = Json.createReader(new StringReader(rawBody))) {
            return reader.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
