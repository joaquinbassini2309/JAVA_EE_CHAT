package chat.Manejadores;

import chat.Enum.TipoMensaje;
import chat.clases.Conversacion;
import chat.clases.Mensaje;
import chat.clases.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ManejadorMensaje {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager em;

    public Mensaje enviarMensaje(Long conversacionId, Long emisorId, String contenido, TipoMensaje tipoMensaje, String urlAdjunto) {
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
        return m;
    }

    public void marcarMensajeLeido(Long mensajeId) {
        Mensaje m = em.find(Mensaje.class, mensajeId);
        if (m != null) {
            m.setLeido(true);
            em.merge(m);
        }
    }

    public Optional<Mensaje> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(Mensaje.class, id));
    }

    public List<Mensaje> obtenerMensajes(Long conversacionId, int limite) {
        TypedQuery<Mensaje> q = em.createQuery(
                "SELECT m FROM Mensaje m WHERE m.conversacion.id = :cid ORDER BY m.fechaEnvio ASC",
                Mensaje.class);
        q.setParameter("cid", conversacionId);
        q.setMaxResults(limite > 0 ? limite : 50);
        return q.getResultList();
    }

    public void marcarTodosComoLeidos(Long conversacionId, Long usuarioId) {
        // Marcar como leídos todos los mensajes de la conversación que NO fueron enviados por el usuario
        em.createQuery(
                "UPDATE Mensaje m SET m.leido = true " +
                "WHERE m.conversacion.id = :cid AND m.emisor.id != :uid AND m.leido = false")
                .setParameter("cid", conversacionId)
                .setParameter("uid", usuarioId)
                .executeUpdate();
    }

    public void eliminarMensaje(Long mensajeId, Long usuarioId) {
        Mensaje m = em.find(Mensaje.class, mensajeId);
        if (m != null && m.getEmisor().getId().equals(usuarioId)) {
            m.setEliminado(true);
            m.setContenido("Mensaje eliminado");
            em.merge(m);
        } else {
            throw new IllegalArgumentException("Mensaje no encontrado o no autorizado");
        }
    }

    public Optional<Mensaje> obtenerMensajeConInfo(Long mensajeId) {
        return Optional.ofNullable(em.find(Mensaje.class, mensajeId));
    }
}