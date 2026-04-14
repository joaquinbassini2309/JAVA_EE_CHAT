package chat.Manejadores;

import chat.Enum.EstadoUsuario;
import chat.clases.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

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

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        EntityManager em = em();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email",
                    Usuario.class);
            q.setParameter("email", email);
            return q.getResultList().stream().findFirst();
        } finally {
            em.close();
        }
    }

    public Optional<Usuario> buscarUsuarioPorUsername(String username) {
        EntityManager em = em();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :username",
                    Usuario.class);
            q.setParameter("username", username);
            return q.getResultList().stream().findFirst();
        } finally {
            em.close();
        }
    }

    public void actualizarEstado(Long usuarioId, EstadoUsuario estado) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario u = em.find(Usuario.class, usuarioId);
            if (u != null) {
                u.setEstado(estado);
                em.merge(u);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}