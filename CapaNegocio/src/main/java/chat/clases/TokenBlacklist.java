package chat.clases;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar tokens JWT que han sido invalidados (por logout).
 */
@Entity
@Table(name = "token_blacklist", indexes = {
    @Index(name = "idx_token_blacklist_token", columnList = "token")
})
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 1000, nullable = false, unique = true)
    private String token;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
}
