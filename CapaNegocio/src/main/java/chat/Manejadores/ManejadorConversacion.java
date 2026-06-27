package chat.Manejadores;

import chat.Enum.TipoConversacion;
import chat.clases.Conversacion;
import chat.clases.Participante;
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
                "SELECT DISTINCT p.conversacion FROM Participante p " +
                "WHERE p.usuario.id = :uid " +
                "AND (p.conversacion.tipo <> :tipoPrivada OR EXISTS (SELECT m FROM Mensaje m WHERE m.conversacion = p.conversacion))",
                Conversacion.class);
        q.setParameter("uid", usuarioId);
        q.setParameter("tipoPrivada", TipoConversacion.PRIVADA);
        return q.getResultList();
    }

    public Optional<Conversacion> buscarChatPrivadoEntre(Long usuario1Id, Long usuario2Id) {
        TypedQuery<Conversacion> q = em.createQuery(
                "SELECT DISTINCT p.conversacion FROM Participante p " +
                "WHERE p.usuario.id = :u1 " +
                "AND p.conversacion.tipo = :tipo",
                Conversacion.class);
        q.setParameter("u1", usuario1Id);
        q.setParameter("tipo", TipoConversacion.PRIVADA);
        List<Conversacion> privadas = q.getResultList();

        for (Conversacion c : privadas) {
            boolean tieneU2 = false;
            for (Participante p : c.getParticipantes()) {
                if (p.getUsuario().getId().equals(usuario2Id)) {
                    tieneU2 = true;
                    break;
                }
            }
            if (tieneU2 && c.getParticipantes().size() == 2) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public void actualizarInfo(Long conversacionId, String nuevoNombre, String fotoUrl, String imagenBanner) {
        Conversacion c = em.find(Conversacion.class, conversacionId);
        if (c != null) {
            if (nuevoNombre != null) c.setNombre(nuevoNombre);
            if (fotoUrl != null) c.setFotoUrl(fotoUrl);
            if (imagenBanner != null) c.setImagenBanner(imagenBanner);
            em.merge(c);
        }
    }

    public List<Conversacion> listarCanalesAvisos() {
        TypedQuery<Conversacion> q = em.createQuery(
                "SELECT c FROM Conversacion c WHERE c.tipo = :tipo",
                Conversacion.class);
        q.setParameter("tipo", TipoConversacion.AVISO);
        return q.getResultList();
    }

    public void eliminarConversacion(Long id) {
        Conversacion c = em.find(Conversacion.class, id);
        if (c != null) {
            c.setMensajeFijado(null);
            em.flush();
            em.remove(c);
        }
    }
}