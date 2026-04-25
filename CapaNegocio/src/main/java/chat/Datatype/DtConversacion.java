package chat.Datatype;

import chat.Enum.TipoConversacion;
import chat.clases.Conversacion;
import chat.clases.Participante;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DtConversacion {

    private Long id;
    private String nombre;
    private TipoConversacion tipo;
    private LocalDateTime fechaCreacion;
    private List<Long> participanteIds;

    public DtConversacion() {
    }

    public DtConversacion(Long id, String nombre, TipoConversacion tipo, LocalDateTime fechaCreacion, List<Long> participanteIds) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.participanteIds = participanteIds;
    }

    public static DtConversacion from(Conversacion conversacion) {
        if (conversacion == null) {
            return null;
        }
        List<Long> participanteIds = conversacion.getParticipantes().stream()
                .map(p -> p.getUsuario().getId())
                .collect(Collectors.toList());
        return new DtConversacion(
                conversacion.getId(),
                conversacion.getNombre(),
                conversacion.getTipo(),
                conversacion.getFechaCreacion(),
                participanteIds
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoConversacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoConversacion tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<Long> getParticipanteIds() {
        return participanteIds;
    }

    public void setParticipanteIds(List<Long> participanteIds) {
        this.participanteIds = participanteIds;
    }
}