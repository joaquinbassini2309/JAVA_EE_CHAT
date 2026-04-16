package chat.servicios.seguridad;

import chat.Sistema.ISistema;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;

import java.lang.reflect.Method;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    private ISistema sistema;

    @Inject
    private JWTUtil jwtUtil;

    /**
     * Obtiene el id del usuario autenticado o null si no está autenticado.
     * - Intenta parsear el nombre del principal como Long.
     * - Si falla, consulta ISistema.buscarUsuarioPorUsername(username) y usa reflexión para obtener getId().
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Long getAuthenticatedUserId(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        String principalName = sc.getUserPrincipal().getName();
        if (principalName == null || principalName.isBlank()) return null;

        // Intentar parsear como id numérico
        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException ignored) {
            // no es numérico -> intentar resolver por username
        }

        try {
            Optional userOpt = sistema.buscarUsuarioPorUsername(principalName); // raw Optional para evitar incompatibilidades de tipo
            if (userOpt != null && userOpt.isPresent()) {
                Object user = userOpt.get();
                if (user == null) return null;
                try {
                    Method m = user.getClass().getMethod("getId");
                    Object idVal = m.invoke(user);
                    if (idVal instanceof Number) {
                        return ((Number) idVal).longValue();
                    }
                } catch (NoSuchMethodException nsme) {
                    // si no existe getId(), no se puede resolver
                }
            }
        } catch (Exception e) {
            // Silenciar excepciones para no romper el flujo; el recurso REST manejará el caso null
        }
        return null;
    }

    /** Devuelve el nombre del principal (username) si está presente */
    public String getAuthenticatedUsername(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        String principalName = sc.getUserPrincipal().getName();
        return (principalName == null || principalName.isBlank()) ? null : principalName;
    }

    /**
     * Extrae el token JWT del header Authorization (formato Bearer token)
     * @param encabezadoAutorizacion Valor del header Authorization
     * @return Token sin prefijo "Bearer " o null si no existe
     */
    public String extraerTokenDelEncabezado(String encabezadoAutorizacion) {
        if (encabezadoAutorizacion == null || encabezadoAutorizacion.isBlank()) return null;
        if (!encabezadoAutorizacion.startsWith("Bearer ")) return null;
        return encabezadoAutorizacion.substring("Bearer ".length()).trim();
    }

    /**
     * Valida un token JWT y extrae el ID del usuario
     * @param token Token JWT
     * @return ID del usuario o null si el token es inválido
     */
    public Long validarTokenYExtraerIdUsuario(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return jwtUtil.extraerUsuarioId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Valida un token JWT y extrae el nombre de usuario
     * @param token Token JWT
     * @return Nombre de usuario o null si el token es inválido
     */
    public String validarTokenYExtraerNombreUsuario(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return jwtUtil.extraerUsername(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un token JWT es válido
     * @param token Token JWT
     * @return true si el token es válido y no ha expirado
     */
    public boolean esTokenValido(String token) {
        if (token == null || token.isBlank()) return false;
        return jwtUtil.esValido(token);
    }
}
