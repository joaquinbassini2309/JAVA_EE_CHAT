package chat.Manejadores;

import chat.Enum.TipoConversacion;
import chat.clases.Conversacion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ManejadorConversacion {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager em;

    public Conversacion crearConversacion(String nombre, TipoConversacion tipo) {
        Conversacion c = new Conversacion();
        c.setNombre(nombre);
        c.setTipo(tipo == null ? TipoConversacion.PRIVADA : tipo);
        em.persist(c);
        return c;
    }

    public Optional<Conversacion> buscarConversacionPorId(Long id) {
        return Optional.ofNullable(em.find(Conversacion.class, id));
    }

    public List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId) {
        TypedQuery<Conversacion> q = em.createQuery(
                "SELECT DISTINCT p.conversacion FROM Participante p WHERE p.usuario.id = :uid",
                Conversacion.class);
        q.setParameter("uid", usuarioId);
        return q.getResultList();
    }

    public Optional<Conversacion> buscarChatPrivadoEntre(Long usuario1Id, Long usuario2Id) {
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
    }
}