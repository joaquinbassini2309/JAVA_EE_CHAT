package chat.Factory;

import chat.Enum.EstadoUsuario;
import chat.clases.Usuario;

public class UsuarioFactory {

    public static Usuario crearUsuario(String username, String email, String passwordHash) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("El password hash no puede estar vacío");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordHash);
        usuario.setEstado(EstadoUsuario.OFFLINE);
        return usuario;
    }

    public static Usuario crearUsuarioConFoto(String username, String email, String passwordHash, String fotoUrl) {
        Usuario usuario = crearUsuario(username, email, passwordHash);
        usuario.setFotoUrl(fotoUrl);
        return usuario;
    }
}
