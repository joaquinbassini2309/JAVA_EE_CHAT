package chat.Manejadores;

import chat.Enum.EstadoTarea;
import chat.clases.Tarea;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ManejadorTarea {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager em;

    @Transactional
    public Tarea crearTarea(Tarea tarea) {
        em.persist(tarea);
        return tarea;
    }

    public List<Tarea> obtenerTareasDeUsuario(Long usuarioId) {
        // Tareas donde el usuario es el creador O está asignado a él O pertenece al grupo asociado
        return em.createQuery("SELECT DISTINCT t FROM Tarea t LEFT JOIN t.grupo g LEFT JOIN g.participantes p " +
                "WHERE t.creador.id = :usuarioId OR t.asignadoA.id = :usuarioId OR p.usuario.id = :usuarioId", Tarea.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    public Optional<Tarea> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(Tarea.class, id));
    }

    @Transactional
    public Tarea actualizar(Tarea tarea) {
        return em.merge(tarea);
    }

    @Transactional
    public void eliminarTarea(Long id) {
        Tarea tarea = em.find(Tarea.class, id);
        if (tarea != null) {
            em.remove(tarea);
        }
    }
}
