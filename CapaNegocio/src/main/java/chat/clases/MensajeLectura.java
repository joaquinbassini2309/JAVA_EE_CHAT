package chat.clases;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje_lecturas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_mensaje", "id_usuario"})
})
public class MensajeLectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensaje", nullable = false)
    private Mensaje mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @PrePersist
    protected void onCreate() {
        fechaLectura = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Mensaje getMensaje() { return mensaje; }
    public void setMensaje(Mensaje mensaje) { this.mensaje = mensaje; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(LocalDateTime fechaLectura) { this.fechaLectura = fechaLectura; }
}
