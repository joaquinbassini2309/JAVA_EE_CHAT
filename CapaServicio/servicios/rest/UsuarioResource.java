package rest;

import chat.Datatype.DtUsuario;
import chat.Sistema.ISistema;
import chat.clases.Usuario;
import chat.servicios.exceptions.ErrorResponse;
import chat.servicios.seguridad.AuthService;
import chat.servicios.seguridad.JWTUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.util.Optional;

@Path("/api/v1/usuarios")
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

    @POST
    @Path("/login")
    public Response login(DtUsuario.LoginDTO loginDto) {
        if (loginDto == null || loginDto.getEmail() == null || loginDto.getEmail().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Email is required")).build();
        }

        if (loginDto.getPassword() == null || loginDto.getPassword().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Password is required")).build();
        }

        return sistema.buscarUsuarioPorEmail(loginDto.getEmail())
                .filter(usuario -> BCrypt.checkpw(loginDto.getPassword(), usuario.getPasswordHash()))
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
    public Response register(DtUsuario.CrearUsuarioDTO crearDto) {
        if (crearDto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Request body is required")).build();
        }

        String username = crearDto.getUsername();
        String email = crearDto.getEmail();
        String password = crearDto.getPassword();

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
}