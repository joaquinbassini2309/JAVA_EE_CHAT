package chat.Manejadores;

import chat.Enum.EstadoUsuario;
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
public class ManejadorUsuario {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager em;

    public Usuario crearUsuario(String username, String email, String passwordHash) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        em.persist(u);
        return u;
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email",
                Usuario.class);
        q.setParameter("email", email);
        return q.getResultList().stream().findFirst();
    }

    public Optional<Usuario> buscarUsuarioPorUsername(String username) {
        TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :username",
                Usuario.class);
        q.setParameter("username", username);
        return q.getResultList().stream().findFirst();
    }

    public List<Usuario> listarUsuarios() {
        TypedQuery<Usuario> q = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
        return q.getResultList();
    }

    public void actualizarEstado(Long usuarioId, EstadoUsuario estado) {
        Usuario u = em.find(Usuario.class, usuarioId);
        if (u != null) {
            u.setEstado(estado);
            em.merge(u);
            em.flush();
            try {
                em.getEntityManagerFactory().getCache().evict(Usuario.class, usuarioId);
            } catch (Exception e) {
                System.err.println("Error al evadir cache L2: " + e.getMessage());
            }
        }
    }

    public void actualizarPerfil(Long usuarioId, chat.Datatype.DtUsuario.ActualizarUsuarioDTO dto) {
        Usuario u = em.find(Usuario.class, usuarioId);
        if (u != null && dto != null) {
            if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
                u.setUsername(dto.getUsername());
            }
            if (dto.getFotoUrl() != null && !dto.getFotoUrl().isBlank()) {
                u.setFotoUrl(dto.getFotoUrl());
            }
            if (dto.getDescripcion() != null) {
                u.setDescripcion(dto.getDescripcion());
            }
            if (dto.getImagenBanner() != null && !dto.getImagenBanner().isBlank()) {
                u.setImagenBanner(dto.getImagenBanner());
            }
            if (dto.getEstado() != null) {
                u.setEstado(dto.getEstado());
            }
            if (dto.getFavoritos() != null) {
                u.setFavoritos(dto.getFavoritos());
            }
            em.merge(u);
            em.flush();
            try {
                em.getEntityManagerFactory().getCache().evict(Usuario.class, usuarioId);
            } catch (Exception e) {
                System.err.println("Error al evadir cache L2: " + e.getMessage());
            }
        }
    }
}