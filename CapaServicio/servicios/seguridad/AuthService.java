// Aca iran todas las operaciones de autenticacion, por ejemplo:
/*
// services/security/AuthService.java
@ApplicationScoped  // o @Singleton
public class AuthService {

    public Long getAuthenticatedUserId(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;

        String principalName = sc.getUserPrincipal().getName();
        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException e) {
            // Buscar por username si no es numérico
            Optional<Usuario> u = usuarioRepository.findByUsername(principalName);
            return u.map(Usuario::getId).orElse(null);
        }
    }

    // Método hardcode para testing
    public Long getUserIdTest() {
        return 1L;
    }
}

Operacion para ConversacionResource.java je
 */