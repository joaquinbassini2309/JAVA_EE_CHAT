package chat.servicios.seguridad;

import chat.Sistema.ISistema;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;

import java.lang.reflect.Method;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    private ISistema sistema;

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
}
