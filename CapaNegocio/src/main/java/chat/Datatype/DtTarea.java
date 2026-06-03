package chat.Datatype;

import chat.Enum.EstadoTarea;
import java.time.LocalDateTime;

public class DtTarea {
    private Long id;
    private String titulo;
    private String contenido;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaCreacion;
    private EstadoTarea estado;
    private Long creadorId;
    private String creadorUsername;
    private Long asignadoAId;
    private String asignadoAUsername;
    private Long grupoId;
    private String grupoNombre;

    public DtTarea() {}

    public DtTarea(Long id, String titulo, String contenido, LocalDateTime fechaVencimiento, LocalDateTime fechaCreacion, EstadoTarea estado, Long creadorId, String creadorUsername, Long asignadoAId, String asignadoAUsername, Long grupoId, String grupoNombre) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.creadorId = creadorId;
        this.creadorUsername = creadorUsername;
        this.asignadoAId = asignadoAId;
        this.asignadoAUsername = asignadoAUsername;
        this.grupoId = grupoId;
        this.grupoNombre = grupoNombre;
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

    public Long getCreadorId() { return creadorId; }
    public void setCreadorId(Long creadorId) { this.creadorId = creadorId; }

    public String getCreadorUsername() { return creadorUsername; }
    public void setCreadorUsername(String creadorUsername) { this.creadorUsername = creadorUsername; }

    public Long getAsignadoAId() { return asignadoAId; }
    public void setAsignadoAId(Long asignadoAId) { this.asignadoAId = asignadoAId; }

    public String getAsignadoAUsername() { return asignadoAUsername; }
    public void setAsignadoAUsername(String asignadoAUsername) { this.asignadoAUsername = asignadoAUsername; }

    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }

    public String getGrupoNombre() { return grupoNombre; }
    public void setGrupoNombre(String grupoNombre) { this.grupoNombre = grupoNombre; }
}
