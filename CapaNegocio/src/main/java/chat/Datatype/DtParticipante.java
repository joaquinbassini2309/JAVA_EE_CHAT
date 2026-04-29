package chat.Datatype;

import chat.Enum.RolParticipante;
import chat.clases.Participante;
import java.time.LocalDateTime;

public class DtParticipante {
    private Long id;
    private DtUsuario.UsuarioResponseDTO usuario;
    private RolParticipante rol;
    private LocalDateTime fechaUnion;

    public DtParticipante(Long id, DtUsuario.UsuarioResponseDTO usuario, RolParticipante rol, LocalDateTime fechaUnion) {
        this.id = id;
        this.usuario = usuario;
        this.rol = rol;
        this.fechaUnion = fechaUnion;
    }

    public static DtParticipante from(Participante p) {
        if (p == null) return null;
        DtUsuario.UsuarioResponseDTO usuarioDto = DtUsuario.UsuarioResponseDTO.fromEntity(p.getUsuario());
        return new DtParticipante(p.getId(), usuarioDto, p.getRol(), p.getFechaUnion());
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DtUsuario.UsuarioResponseDTO getUsuario() { return usuario; }
    public void setUsuario(DtUsuario.UsuarioResponseDTO usuario) { this.usuario = usuario; }
    public RolParticipante getRol() { return rol; }
    public void setRol(RolParticipante rol) { this.rol = rol; }
    public LocalDateTime getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(LocalDateTime fechaUnion) { this.fechaUnion = fechaUnion; }
}
