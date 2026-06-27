package rest;

import chat.Datatype.DtUsuario;
import chat.Sistema.ISistema;
import chat.clases.Usuario;
import exceptions.ErrorResponse;
import seguridad.AuthService;
import seguridad.JWTUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public Response getAllUsers() {
        List<Usuario> usuarios = sistema.listarUsuarios();
        List<DtUsuario.UsuarioResponseDTO> dtos = usuarios.stream()
                .map(DtUsuario.UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

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
    @Path("/login-google")
    public Response loginGoogle(DtUsuario.GoogleLoginDTO googleDto) {
        if (googleDto == null || googleDto.getCredential() == null || googleDto.getCredential().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Google credential token is required")).build();
        }

        try {
            String token = googleDto.getCredential();
            String verificationUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(verificationUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(401, "Invalid Google token")).build();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            String aud = root.path("aud").asText();
            String clientId = "194864765404-658569mqh83fmf7tkr8skp86tmf1224c.apps.googleusercontent.com";
            if (!clientId.equals(aud)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(401, "Token audience mismatch")).build();
            }

            String email = root.path("email").asText();
            String name = root.path("name").asText();
            String picture = root.path("picture").asText();

            if (email == null || email.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Email not provided by Google")).build();
            }

            Optional<Usuario> usuarioOpt = sistema.buscarUsuarioPorEmail(email);
            Usuario usuario;

            if (usuarioOpt.isPresent()) {
                usuario = usuarioOpt.get();
                if (usuario.isActivo() != null && !usuario.isActivo()) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorResponse(401, "El usuario está inactivo")).build();
                }
                
                sistema.actualizarEstadoUsuario(usuario.getId(), chat.Enum.EstadoUsuario.ONLINE);
            } else {
                String username = name.replaceAll("\\s+", "").toLowerCase();
                if (sistema.buscarUsuarioPorUsername(username).isPresent()) {
                    username = username + (System.currentTimeMillis() % 1000);
                }

                String randomPassword = "GooglePass-" + java.util.UUID.randomUUID().toString();
                usuario = sistema.registrarUsuario(username, email, randomPassword);
                
                if (picture != null && !picture.isBlank()) {
                    chat.Datatype.DtUsuario.ActualizarUsuarioDTO perfilDto = new chat.Datatype.DtUsuario.ActualizarUsuarioDTO(
                        usuario.getUsername(),
                        picture,
                        "Registrado con Google",
                        null,
                        chat.Enum.EstadoUsuario.ONLINE
                    );
                    sistema.actualizarPerfilUsuario(usuario.getId(), perfilDto);
                    usuario = sistema.buscarUsuarioPorId(usuario.getId()).orElse(usuario);
                } else {
                    sistema.actualizarEstadoUsuario(usuario.getId(), chat.Enum.EstadoUsuario.ONLINE);
                }
            }

            String jwtToken = generateToken(usuario);
            DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);
            DtUsuario.AuthResponseDTO authResponse = new DtUsuario.AuthResponseDTO(jwtToken, usuarioDto);

            return Response.ok(authResponse).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error authenticating with Google", e.getMessage())).build();
        }
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
                sistema.actualizarPerfilUsuario(usuarioId, actualizarDto);
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