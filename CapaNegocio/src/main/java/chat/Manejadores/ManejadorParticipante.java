package chat.Manejadores;

import chat.Enum.RolParticipante;
import chat.clases.Conversacion;
import chat.clases.Participante;
import chat.clases.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManejadorParticipante {

    private final EntityManagerFactory emf;

    public ManejadorParticipante(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() { return emf.createEntityManager(); }

    public Participante agregarParticipante(Long conversacionId, Long usuarioId, RolParticipante rol) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Conversacion c = em.find(Conversacion.class, conversacionId);
            Usuario u = em.find(Usuario.class, usuarioId);
            if (c == null || u == null) throw new IllegalArgumentException("Conversacion o Usuario no encontrado");
            Participante p = new Participante();
            p.setConversacion(c);
            p.setUsuario(u);
            p.setRol(rol == null ? RolParticipante.MIEMBRO : rol);
            em.persist(p);
            tx.commit();
            return p;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Participante> removerParticipante(Long conversacionId, Long usuarioId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        List<Participante> removed = new ArrayList<>();
        try {
            tx.begin();
            TypedQuery<Participante> q = em.createQuery(
                    "SELECT p FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                    Participante.class);
            q.setParameter("cid", conversacionId);
            q.setParameter("uid", usuarioId);
            List<Participante> encontrados = q.getResultList();
            for (Participante p : new ArrayList<>(encontrados)) {
                Participante managed = em.find(Participante.class, p.getId());
                if (managed != null) {
                    removed.add(managed);
                    em.remove(managed);
                }
            }
            tx.commit();
            return removed;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public Optional<Participante> buscarPorId(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Participante.class, id));
        } finally {
            em.close();
        }
    }

    public boolean existeParticipante(Long conversacionId, Long usuarioId) {
        EntityManager em = em();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                    Long.class);
            q.setParameter("cid", conversacionId);
            q.setParameter("uid", usuarioId);
            return q.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public Optional<Participante> buscarParticipante(Long conversacionId, Long usuarioId) {
        EntityManager em = em();
        try {
            TypedQuery<Participante> q = em.createQuery(
                    "SELECT p FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                    Participante.class);
            q.setParameter("cid", conversacionId);
            q.setParameter("uid", usuarioId);
            return q.getResultList().stream().findFirst();
        } finally {
            em.close();
        }
    }

    public long contarAdmins(Long conversacionId) {
        EntityManager em = em();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(p) FROM Participante p WHERE p.conversacion.id = :cid AND p.rol = :rol",
                    Long.class);
            q.setParameter("cid", conversacionId);
            q.setParameter("rol", RolParticipante.ADMIN);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    public void actualizarRol(Long conversacionId, Long usuarioId, RolParticipante nuevoRol) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TypedQuery<Participante> q = em.createQuery(
                    "SELECT p FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                    Participante.class);
            q.setParameter("cid", conversacionId);
            q.setParameter("uid", usuarioId);
            Participante p = q.getResultList().stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Participante no encontrado"));
            p.setRol(nuevoRol);
            em.merge(p);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}