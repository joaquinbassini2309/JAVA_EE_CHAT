package chat.Manejadores;

import chat.Enum.TipoMensaje;
import chat.clases.Conversacion;
import chat.clases.Mensaje;
import chat.clases.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ManejadorMensaje {

    private final EntityManagerFactory emf;

    public ManejadorMensaje(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() { return emf.createEntityManager(); }

    public Mensaje enviarMensaje(Long conversacionId, Long emisorId, String contenido, TipoMensaje tipoMensaje, String urlAdjunto) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Conversacion c = em.find(Conversacion.class, conversacionId);
            Usuario u = em.find(Usuario.class, emisorId);
            if (c == null || u == null) throw new IllegalArgumentException("Conversacion o Emisor no encontrado");
            Mensaje m = new Mensaje();
            m.setConversacion(c);
            m.setEmisor(u);
            m.setContenido(contenido);
            m.setTipoMensaje(tipoMensaje == null ? TipoMensaje.TEXTO : tipoMensaje);
            m.setUrlAdjunto(urlAdjunto);
            em.persist(m);
            tx.commit();
            return m;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void marcarMensajeLeido(Long mensajeId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Mensaje m = em.find(Mensaje.class, mensajeId);
            if (m != null) {
                m.setLeido(true);
                em.merge(m);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public Optional<Mensaje> buscarPorId(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Mensaje.class, id));
        } finally {
            em.close();
        }
    }

    public List<Mensaje> obtenerMensajes(Long conversacionId, int limite) {
        EntityManager em = em();
        try {
            TypedQuery<Mensaje> q = em.createQuery(
                    "SELECT m FROM Mensaje m WHERE m.conversacion.id = :cid ORDER BY m.fechaEnvio DESC",
                    Mensaje.class);
            q.setParameter("cid", conversacionId);
            q.setMaxResults(limite > 0 ? limite : 50);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public void marcarTodosComoLeidos(Long conversacionId, Long usuarioId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Marcar como leídos todos los mensajes de la conversación que NO fueron enviados por el usuario
            em.createQuery(
                    "UPDATE Mensaje m SET m.leido = true " +
                    "WHERE m.conversacion.id = :cid AND m.emisor.id != :uid AND m.leido = false")
                    .setParameter("cid", conversacionId)
                    .setParameter("uid", usuarioId)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}