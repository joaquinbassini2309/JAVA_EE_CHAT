// java
package chat.clases;

import chat.Enum.RolParticipante;
import com.fasterxml.jackson.annotation.JsonIgnore; // Importar la anotación
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "participantes",
        uniqueConstraints = @UniqueConstraint(name = "uk_participantes_conv_user", columnNames = {"id_conversacion", "id_usuario"}))
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conversacion", nullable = false)
    @JsonIgnore // <-- ¡AÑADIDO! Rompe el bucle de serialización
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20)
    private RolParticipante rol = RolParticipante.MIEMBRO;

    @Column(name = "fecha_union")
    private LocalDateTime fechaUnion;

    @PrePersist
    private void prePersist() {
        if (fechaUnion == null) fechaUnion = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversacion getConversacion() { return conversacion; }
    public void setConversacion(Conversacion conversacion) { this.conversacion = conversacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public RolParticipante getRol() { return rol; }
    public void setRol(RolParticipante rol) { this.rol = rol; }

    public LocalDateTime getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(LocalDateTime fechaUnion) { this.fechaUnion = fechaUnion; }

    // equals/hashCode basados en id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participante)) return false;
        Participante that = (Participante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
