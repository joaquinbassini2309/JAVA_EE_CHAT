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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/usuarios")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    // Usernames reservados — no se pueden registrar.
    private static final Set<String> USERNAMES_RESERVADOS = Set.of(
        "sudo - admin", "admin", "root", "system", "superadmin"
    );

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService authService;

    @Inject
    private JWTUtil jwtUtil;

    @Context
    private SecurityContext securityContext;

    @Inject
    private chat.Manejadores.ManejadorTokenBlacklist blacklistHandler;

    /**
     * Listar usuarios — requiere autenticación. No expone email en la respuesta pública.
     */
    @GET
    public Response getAllUsers() {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        List<Usuario> usuarios = sistema.listarUsuarios();
        List<DtUsuario.UsuarioPublicoDTO> dtos = usuarios.stream()
                .map(DtUsuario.UsuarioPublicoDTO::fromEntity)
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

        // Google Client ID desde variable de entorno — nunca hardcodeado.
        String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        if (googleClientId == null || googleClientId.isBlank()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Server configuration error")).build();
        }

        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    new com.google.api.client.json.gson.GsonFactory())
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken =
                verifier.verify(googleDto.getCredential());

            if (idToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(401, "Invalid Google token")).build();
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

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
                String username = name != null ? name.replaceAll("\\s+", "").toLowerCase() : "user";
                if (sistema.buscarUsuarioPorUsername(username).isPresent()) {
                    username = username + (System.currentTimeMillis() % 1000);
                }

                String randomPassword = "GooglePass-" + java.util.UUID.randomUUID();
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
            // No exponer detalle de la excepción al cliente.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error authenticating with Google")).build();
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

        // Bloquear usernames reservados.
        if (USERNAMES_RESERVADOS.contains(username.toLowerCase().trim())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Username not allowed")).build();
        }

        try {
            Usuario usuario = sistema.registrarUsuario(username, email, password);
            DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuario);

            URI location = URI.create("/api/v1/usuarios/" + usuario.getId());

            return Response.created(location).entity(usuarioDto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error registering user")).build();
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

        return sistema.buscarUsuarioPorId(usuarioId).map(usuario -> {
            if (actualizarDto != null) {
                sistema.actualizarPerfilUsuario(usuarioId, actualizarDto);
            }

            Usuario usuarioActualizado = sistema.buscarUsuarioPorId(usuarioId)
                    .orElseThrow(() -> new IllegalStateException("User disappeared during update"));

            DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(usuarioActualizado);
            return Response.ok(usuarioDto).build();

        }).orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(404, "User not found")).build());
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION) String authHeader) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        try {
            String token = authService.extraerTokenDelEncabezado(authHeader);
            if (token != null) {
                java.time.LocalDateTime expiracion = java.time.LocalDateTime.now().plusHours(24);
                blacklistHandler.agregarToken(token, expiracion);
                sistema.actualizarEstadoUsuario(usuarioId, chat.Enum.EstadoUsuario.OFFLINE);
            }
            return Response.ok("{\"message\": \"Logout exitoso\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error en logout")).build();
        }
    }

    private String generateToken(Usuario usuario) {
        // Rol por defecto USUARIO. Extender con lógica de roles si se implementa.
        return jwtUtil.generarToken(usuario.getId(), usuario.getUsername(), "USUARIO");
    }
}