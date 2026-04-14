package chat.Datatype;

import chat.Enum.EstadoUsuario;
import chat.clases.Usuario;
import java.time.LocalDateTime;

public final class DtUsuario {

    private DtUsuario() {
        // utilidad - no instanciar
    }

    // DTO para crear usuario (registro)
    public static class CrearUsuarioDTO {
        private String username;
        private String email;
        private String password; // plano, se hashea en el servidor

        public CrearUsuarioDTO() {}

        public CrearUsuarioDTO(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // DTO para respuesta (nunca expone password)
    public static class UsuarioResponseDTO {
        private Long id;
        private String username;
        private String email;
        private String fotoUrl;
        private EstadoUsuario estado;
        private LocalDateTime fechaRegistro;

        public UsuarioResponseDTO() {}

        public UsuarioResponseDTO(
                Long id,
                String username,
                String email,
                String fotoUrl,
                EstadoUsuario estado,
                LocalDateTime fechaRegistro
        ) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fotoUrl = fotoUrl;
            this.estado = estado;
            this.fechaRegistro = fechaRegistro;
        }

        // constructor desde entidad (nulo-safe)
        public static UsuarioResponseDTO fromEntity(Usuario usuario) {
            if (usuario == null) return null;
            UsuarioResponseDTO dto = new UsuarioResponseDTO();
            dto.id = usuario.getId();
            dto.username = usuario.getUsername();
            dto.email = usuario.getEmail();
            dto.fotoUrl = usuario.getFotoUrl();
            dto.estado = usuario.getEstado();
            dto.fechaRegistro = usuario.getFechaRegistro();
            return dto;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFotoUrl() { return fotoUrl; }
        public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

        public EstadoUsuario getEstado() { return estado; }
        public void setEstado(EstadoUsuario estado) { this.estado = estado; }

        // Compatibilidad temporal para capas que aún envían String
        public void setEstado(String estado) { this.estado = parseEstado(estado); }

        public LocalDateTime getFechaRegistro() { return fechaRegistro; }
        public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    }

    // DTO para actualizar perfil
    public static class ActualizarUsuarioDTO {
        private String fotoUrl;
        private EstadoUsuario estado;

        public ActualizarUsuarioDTO() {}

        public ActualizarUsuarioDTO(String fotoUrl, EstadoUsuario estado) {
            this.fotoUrl = fotoUrl;
            this.estado = estado;
        }

        public String getFotoUrl() { return fotoUrl; }
        public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

        public EstadoUsuario getEstado() { return estado; }
        public void setEstado(EstadoUsuario estado) { this.estado = estado; }

        // Compatibilidad temporal para capas que aún envían String
        public void setEstado(String estado) { this.estado = parseEstado(estado); }
    }

    // DTO para login
    public static class LoginDTO {
        private String username;
        private String password;

        public LoginDTO() {}

        public LoginDTO(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // DTO para respuesta de autenticación
    public static class AuthResponseDTO {
        private String token;
        private UsuarioResponseDTO usuario;

        public AuthResponseDTO() {}

        public AuthResponseDTO(String token, UsuarioResponseDTO usuario) {
            this.token = token;
            this.usuario = usuario;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public UsuarioResponseDTO getUsuario() { return usuario; }
        public void setUsuario(UsuarioResponseDTO usuario) { this.usuario = usuario; }
    }

    private static EstadoUsuario parseEstado(String estado) {
        if (estado == null || estado.isBlank()) return null;
        return EstadoUsuario.valueOf(estado.trim().toUpperCase());
    }
}