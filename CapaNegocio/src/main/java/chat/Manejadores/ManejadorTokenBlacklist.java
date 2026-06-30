package chat.Manejadores;

import chat.clases.TokenBlacklist;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

/**
 * Manejador para gestionar el ciclo de vida de los tokens invalidados (blacklist).
 */
@ApplicationScoped
@Transactional
public class ManejadorTokenBlacklist {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager em;

    /**
     * Registrar un token en la lista negra.
     */
    public void agregarToken(String token, LocalDateTime fechaExpiracion) {
        try {
            TokenBlacklist tb = new TokenBlacklist();
            tb.setToken(token);
            tb.setFechaExpiracion(fechaExpiracion);
            em.persist(tb);
            em.flush();
        } catch (Exception e) {
            // Silenciar si ya existe (clave única violada)
            System.err.println("Advertencia al agregar token a blacklist (posible duplicado): " + e.getMessage());
        }
    }

    /**
     * Verificar si el token ya está registrado en la lista negra.
     */
    public boolean estaInvalidado(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Long count = em.createQuery(
                "SELECT COUNT(t) FROM TokenBlacklist t WHERE t.token = :token", Long.class)
                .setParameter("token", token)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error al verificar token en blacklist: " + e.getMessage());
            return false;
        }
    }

    /**
     * Limpiar todos los tokens expirados de la base de datos.
     */
    public void limpiarExpirados() {
        try {
            int eliminados = em.createQuery("DELETE FROM TokenBlacklist t WHERE t.fechaExpiracion < :ahora")
                .setParameter("ahora", LocalDateTime.now())
                .executeUpdate();
            System.out.println("ManejadorTokenBlacklist: Limpieza completada. Tokens expirados eliminados: " + eliminados);
        } catch (Exception e) {
            System.err.println("Error al limpiar tokens expirados: " + e.getMessage());
        }
    }
}
