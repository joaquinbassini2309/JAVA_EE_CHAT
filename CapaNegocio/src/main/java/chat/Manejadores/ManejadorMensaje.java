package chat.Manejadores;

import chat.Enums.TipoMensaje;
import com.example.chat.model.Conversacion;
import com.example.chat.model.Mensaje;
import com.example.chat.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

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
}