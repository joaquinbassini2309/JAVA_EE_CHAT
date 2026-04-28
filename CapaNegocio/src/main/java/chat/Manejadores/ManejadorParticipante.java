package chat.Manejadores;

import chat.Enum.RolParticipante;
import chat.clases.Conversacion;
import chat.clases.Participante;
import chat.clases.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ManejadorParticipante {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager em;

    public Participante agregarParticipante(Long conversacionId, Long usuarioId, RolParticipante rol) {
        Conversacion c = em.find(Conversacion.class, conversacionId);
        Usuario u = em.find(Usuario.class, usuarioId);
        if (c == null || u == null) throw new IllegalArgumentException("Conversacion o Usuario no encontrado");
        Participante p = new Participante();
        p.setConversacion(c);
        p.setUsuario(u);
        p.setRol(rol == null ? RolParticipante.MIEMBRO : rol);
        em.persist(p);
        return p;
    }

    public List<Participante> removerParticipante(Long conversacionId, Long usuarioId) {
        List<Participante> removed = new ArrayList<>();
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
        return removed;
    }

    public Optional<Participante> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(Participante.class, id));
    }

    public boolean existeParticipante(Long conversacionId, Long usuarioId) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(p) FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                Long.class);
        q.setParameter("cid", conversacionId);
        q.setParameter("uid", usuarioId);
        long count = q.getSingleResult();
        System.out.println("DEBUG: existeParticipante - cid: " + conversacionId + ", uid: " + usuarioId + ", count: " + count);
        return count > 0;
    }

    public Optional<Participante> buscarParticipante(Long conversacionId, Long usuarioId) {
        TypedQuery<Participante> q = em.createQuery(
                "SELECT p FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                Participante.class);
        q.setParameter("cid", conversacionId);
        q.setParameter("uid", usuarioId);
        return q.getResultList().stream().findFirst();
    }

    public long contarAdmins(Long conversacionId) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(p) FROM Participante p WHERE p.conversacion.id = :cid AND p.rol = :rol",
                Long.class);
        q.setParameter("cid", conversacionId);
        q.setParameter("rol", RolParticipante.ADMIN);
        return q.getSingleResult();
    }

    public void actualizarRol(Long conversacionId, Long usuarioId, RolParticipante nuevoRol) {
        TypedQuery<Participante> q = em.createQuery(
                "SELECT p FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id = :uid",
                Participante.class);
        q.setParameter("cid", conversacionId);
        q.setParameter("uid", usuarioId);
        Participante p = q.getResultList().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participante no encontrado"));
        p.setRol(nuevoRol);
        em.merge(p);
    }

    public Object buscarParticipantesPorConversacion(Long id) {
        TypedQuery<Participante> q = em.createQuery(
                "SELECT p FROM Participante p WHERE p.conversacion.id = :cid",
                Participante.class);
        q.setParameter("cid", id);
        return q.getResultList();
    }
}