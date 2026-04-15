// java
package chat.clases;

import chat.Enum.TipoConversacion;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversaciones")
public class Conversacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoConversacion tipo = TipoConversacion.PRIVADA;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Participante> participantes = new HashSet<>();

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Mensaje> mensajes = new HashSet<>();

    @PrePersist
    private void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    // Configuración completa de Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoConversacion getTipo() { return tipo; }
    public void setTipo(TipoConversacion tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Set<Participante> getParticipantes() { return participantes; }
    public void setParticipantes(Set<Participante> participantes) { this.participantes = participantes; }

    public Set<Mensaje> getMensajes() { return mensajes; }
    public void setMensajes(Set<Mensaje> mensajes) { this.mensajes = mensajes; }
}