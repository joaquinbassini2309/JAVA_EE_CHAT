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
        // Query 1: tareas asignadas explícitamente a este usuario
        List<Tarea> asignadas = em.createQuery(
                "SELECT t FROM Tarea t WHERE t.asignadoA.id = :uid", Tarea.class)
                .setParameter("uid", usuarioId)
                .getResultList();

        // Query 2: tareas propias sin asignar (el creador las creó para sí mismo)
        List<Tarea> propias = em.createQuery(
                "SELECT t FROM Tarea t WHERE t.creador.id = :uid AND t.asignadoA IS NULL", Tarea.class)
                .setParameter("uid", usuarioId)
                .getResultList();

        List<Tarea> resultado = new java.util.ArrayList<>(asignadas);
        for (Tarea t : propias) {
            if (resultado.stream().noneMatch(r -> r.getId().equals(t.getId()))) {
                resultado.add(t);
            }
        }
        return resultado;
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
