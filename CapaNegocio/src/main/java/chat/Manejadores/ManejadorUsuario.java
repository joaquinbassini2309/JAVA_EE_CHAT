package chat.Manejadores;

import com.example.chat.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.Optional;

public class ManejadorUsuario {

    private final EntityManagerFactory emf;

    public ManejadorUsuario(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() { return emf.createEntityManager(); }

    public Usuario crearUsuario(String username, String email, String passwordHash) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setEmail(email);
            u.setPasswordHash(passwordHash);
            em.persist(u);
            tx.commit();
            return u;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Usuario.class, id));
        } finally {
            em.close();
        }
    }
}