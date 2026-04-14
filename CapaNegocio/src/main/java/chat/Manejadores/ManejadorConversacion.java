package chat.Manejadores;

import chat.Enum.TipoConversacion;
import chat.clases.Conversacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ManejadorConversacion {

    private final EntityManagerFactory emf;

    public ManejadorConversacion(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() { return emf.createEntityManager(); }

    public Conversacion crearConversacion(String nombre, TipoConversacion tipo) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Conversacion c = new Conversacion();
            c.setNombre(nombre);
            c.setTipo(tipo == null ? TipoConversacion.PRIVADA : tipo);
            em.persist(c);
            tx.commit();
            return c;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public Optional<Conversacion> buscarConversacionPorId(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Conversacion.class, id));
        } finally {
            em.close();
        }
    }

    public List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId) {
        EntityManager em = em();
        try {
            TypedQuery<Conversacion> q = em.createQuery(
                    "SELECT DISTINCT p.conversacion FROM Participante p WHERE p.usuario.id = :uid",
                    Conversacion.class);
            q.setParameter("uid", usuarioId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Conversacion> buscarChatPrivadoEntre(Long usuario1Id, Long usuario2Id) {
        EntityManager em = em();
        try {
            TypedQuery<Conversacion> q = em.createQuery(
                    "SELECT c FROM Conversacion c " +
                    "WHERE c.tipo = :tipo " +
                    "AND EXISTS (SELECT p1 FROM Participante p1 WHERE p1.conversacion = c AND p1.usuario.id = :u1) " +
                    "AND EXISTS (SELECT p2 FROM Participante p2 WHERE p2.conversacion = c AND p2.usuario.id = :u2) " +
                    "AND (SELECT COUNT(p) FROM Participante p WHERE p.conversacion = c) = 2",
                    Conversacion.class);
            q.setParameter("tipo", TipoConversacion.PRIVADA);
            q.setParameter("u1", usuario1Id);
            q.setParameter("u2", usuario2Id);
            return q.getResultList().stream().findFirst();
        } finally {
            em.close();
        }
    }
}