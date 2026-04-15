package chat.servicios.rest;

import chat.Datatype.DtUsuario;
import chat.Sistema.ISistema;
import chat.clases.Usuario;
import chat.servicios.seguridad.AuthService;
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
import java.util.Base64;
import java.util.Optional;

@Path("/api/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    public Response login(DtUsuario.LoginDTO loginDto) {
        if (loginDto == null || loginDto.getUsername() == null || loginDto.getUsername().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail("Username is required")).build();
        }

        if (loginDto.getPassword() == null || loginDto.getPassword().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail("Password is required")).build();
        }

        Optional<Usuario> usuarioOpt = sistema.buscarUsuarioPorUsername(loginDto.getUsername());
        if (usuarioOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorDetail("Invalid credentials")).build();
        }

        Usuario usuario = usuarioOpt.get();
        if (!BCrypt.checkpw(loginDto.getPassword(), usuario.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorDetail("Invalid credentials")).build();
        }

        String token = generateToken(usuario);
        DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);
        DtUsuario.AuthResponseDTO authResponse = new DtUsuario.AuthResponseDTO(token, usuarioDto);

        return Response.ok(authResponse).build();
    }

    @POST
    @Path("/register")
    public Response register(DtUsuario.CrearUsuarioDTO crearDto) {
        if (crearDto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail("Request body is required")).build();
        }

        String username = crearDto.getUsername();
        String email = crearDto.getEmail();
        String password = crearDto.getPassword();

        if (username == null || username.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail("Nombre de usuario requerido")).build();
        }

        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail("Se necesita email")).build();
        }

        if (password == null || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail("Se necesita contraseña")).build();
        }

        try {
            Usuario usuario = sistema.registrarUsuario(username, email, password);
            DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);

            URI location = URI.create("/api/v1/usuarios/" + usuario.getId());

            return Response.created(location).entity(usuarioDto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDetail(e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorDetail("Error registering user")).build();
        }
    }

    @PUT
    @Path("/perfil")
    public Response updateProfile(DtUsuario.ActualizarUsuarioDTO actualizarDto) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorDetail("Authentication required")).build();
        }

        Optional<Usuario> usuarioOpt = sistema.buscarUsuarioPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorDetail("User not found")).build();
        }

        if (actualizarDto != null) {
            sistema.actualizarPerfilUsuario(usuarioId, actualizarDto.getFotoUrl(), actualizarDto.getEstado());
            
            // Obtener usuario actualizado
            usuarioOpt = sistema.buscarUsuarioPorId(usuarioId);
        }

        Usuario usuario = usuarioOpt.get();
        DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);
        return Response.ok(usuarioDto).build();
    }

    private String generateToken(Usuario usuario) {
        String payload = usuario.getId() + ":" + usuario.getUsername();
        return Base64.getEncoder().encodeToString(payload.getBytes());
    }

    public static class ErrorDetail {
        public String message;

        public ErrorDetail(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
