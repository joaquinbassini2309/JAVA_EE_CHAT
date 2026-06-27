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

        TypedQuery<Long> partQuery = em.createQuery(
                "SELECT COUNT(p) FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id != :emisorId",
                Long.class);
        partQuery.setParameter("cid", conversacionId);
        partQuery.setParameter("emisorId", emisorId);
        long otherParticipants = partQuery.getSingleResult();
        
        m.setLecturasEsperadas((int) otherParticipants);
        if (otherParticipants == 0) {
            m.setLeido(true);
        }

        em.persist(m);
        return m;
    }

    public void marcarMensajeLeido(Long mensajeId, Long usuarioId) {
        Mensaje m = em.find(Mensaje.class, mensajeId);
        Usuario u = em.find(Usuario.class, usuarioId);
        if (m != null && u != null) {
            TypedQuery<Long> checkQuery = em.createQuery(
                    "SELECT COUNT(ml) FROM MensajeLectura ml WHERE ml.mensaje.id = :mid AND ml.usuario.id = :uid",
                    Long.class);
            checkQuery.setParameter("mid", mensajeId);
            checkQuery.setParameter("uid", usuarioId);
            if (checkQuery.getSingleResult() == 0) {
                chat.clases.MensajeLectura ml = new chat.clases.MensajeLectura();
                ml.setMensaje(m);
                ml.setUsuario(u);
                em.persist(ml);

                actualizarEstadoLeidoMensaje(m);
            }
        }
    }

    private void actualizarEstadoLeidoMensaje(Mensaje m) {
        long targetReads = 0;
        
        if (m.getLecturasEsperadas() != null) {
            targetReads = m.getLecturasEsperadas();
        } else {
            // Fallback para mensajes antiguos sin la columna
            TypedQuery<Long> partQuery = em.createQuery(
                    "SELECT COUNT(p) FROM Participante p WHERE p.conversacion.id = :cid AND p.usuario.id != :emisorId",
                    Long.class);
            partQuery.setParameter("cid", m.getConversacion().getId());
            partQuery.setParameter("emisorId", m.getEmisor().getId());
            targetReads = partQuery.getSingleResult();
        }

        TypedQuery<Long> readQuery = em.createQuery(
                "SELECT COUNT(ml) FROM MensajeLectura ml WHERE ml.mensaje.id = :mid",
                Long.class);
        readQuery.setParameter("mid", m.getId());
        long readers = readQuery.getSingleResult();

        if (readers >= targetReads && targetReads > 0) {
            if (!m.getLeido()) {
                m.setLeido(true);
                em.merge(m);
            }
        } else if (targetReads == 0 && !m.getLeido()) {
            m.setLeido(true);
            em.merge(m);
        }
    }

    public Optional<Mensaje> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(Mensaje.class, id));
    }

    public List<Mensaje> obtenerMensajes(Long conversacionId, int limite, int offset) {
        TypedQuery<Mensaje> q = em.createQuery(
                "SELECT m FROM Mensaje m WHERE m.conversacion.id = :cid ORDER BY m.fechaEnvio DESC",
                Mensaje.class);
        q.setParameter("cid", conversacionId);
        q.setFirstResult(offset);
        q.setMaxResults(limite > 0 ? limite : 50);
        List<Mensaje> res = q.getResultList();
        // Invertir para que el frontend los reciba en orden cronológico (viejo -> nuevo)
        java.util.Collections.reverse(res);
        return res;
    }

    public Optional<Mensaje> buscarUltimoMensaje(Long conversacionId) {
        TypedQuery<Mensaje> q = em.createQuery(
                "SELECT m FROM Mensaje m WHERE m.conversacion.id = :cid ORDER BY m.fechaEnvio DESC",
                Mensaje.class);
        q.setParameter("cid", conversacionId);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }

    public void marcarTodosComoLeidos(Long conversacionId, Long usuarioId) {
        Usuario u = em.find(Usuario.class, usuarioId);
        if (u == null) return;

        TypedQuery<Mensaje> query = em.createQuery(
                "SELECT m FROM Mensaje m WHERE m.conversacion.id = :cid AND m.emisor.id != :uid " +
                "AND NOT EXISTS (SELECT 1 FROM MensajeLectura ml WHERE ml.mensaje.id = m.id AND ml.usuario.id = :uid)",
                Mensaje.class);
        query.setParameter("cid", conversacionId);
        query.setParameter("uid", usuarioId);
        List<Mensaje> noLeidos = query.getResultList();

        for (Mensaje m : noLeidos) {
            chat.clases.MensajeLectura ml = new chat.clases.MensajeLectura();
            ml.setMensaje(m);
            ml.setUsuario(u);
            em.persist(ml);

            actualizarEstadoLeidoMensaje(m);
        }
    }

    public Long contarMensajesSinLeer(Long conversacionId, Long usuarioId) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.id = :cid AND m.emisor.id != :uid " +
                "AND NOT EXISTS (SELECT 1 FROM MensajeLectura ml WHERE ml.mensaje.id = m.id AND ml.usuario.id = :uid)",
                Long.class);
        q.setParameter("cid", conversacionId);
        q.setParameter("uid", usuarioId);
        return q.getSingleResult();
    }

    public void eliminarMensaje(Long mensajeId) {
        Mensaje m = em.find(Mensaje.class, mensajeId);
        if (m != null) {
            m.setEliminado(true);
            m.setContenido("Mensaje eliminado");
            m.setUrlAdjunto(null);
            m.setTipoMensaje(TipoMensaje.TEXTO);
            em.merge(m);
        } else {
            throw new IllegalArgumentException("Mensaje no encontrado");
        }
    }

    public Optional<Mensaje> obtenerMensajeConInfo(Long mensajeId) {
        return Optional.ofNullable(em.find(Mensaje.class, mensajeId));
    }

    public void actualizarColorResaltado(Long mensajeId, String color) {
        Mensaje m = em.find(Mensaje.class, mensajeId);
        if (m != null) {
            m.setColorResaltado(color);
            em.merge(m);
        }
    }

    public Long contarMencionesSinLeer(Long conversacionId, Long usuarioId, String username) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.id = :cid " +
                "AND m.emisor.id != :uid " +
                "AND NOT EXISTS (SELECT 1 FROM MensajeLectura ml WHERE ml.mensaje.id = m.id AND ml.usuario.id = :uid) " +
                "AND (m.contenido LIKE :mention OR m.contenido LIKE :todos)",
                Long.class);
        q.setParameter("cid", conversacionId);
        q.setParameter("uid", usuarioId);
        q.setParameter("mention", "%@" + username + "%");
        q.setParameter("todos", "%@todos%");
        return q.getSingleResult();
    }
}