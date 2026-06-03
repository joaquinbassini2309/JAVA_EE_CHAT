package chat.clases;

import chat.Enum.EstadoTarea;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", length = 100, nullable = false)
    private String titulo;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creador", nullable = false)
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignado_a")
    private Usuario asignadoA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    private Conversacion grupo;

    @PrePersist
    private void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }

    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }

    public Usuario getAsignadoA() { return asignadoA; }
    public void setAsignadoA(Usuario asignadoA) { this.asignadoA = asignadoA; }

    public Conversacion getGrupo() { return grupo; }
    public void setGrupo(Conversacion grupo) { this.grupo = grupo; }
}
