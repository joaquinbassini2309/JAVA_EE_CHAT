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
    private List<DtUsuario.UsuarioResponseDTO> participantes;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private Integer noLeidos;

    public DtConversacion() {
    }

    public DtConversacion(Long id, String nombre, TipoConversacion tipo, LocalDateTime fechaCreacion, List<Long> participanteIds, List<DtUsuario.UsuarioResponseDTO> participantes, String ultimoMensaje, LocalDateTime fechaUltimoMensaje, Integer noLeidos) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.participanteIds = participanteIds;
        this.participantes = participantes;
        this.ultimoMensaje = ultimoMensaje;
        this.fechaUltimoMensaje = fechaUltimoMensaje;
        this.noLeidos = noLeidos;
    }

    public static DtConversacion from(Conversacion conversacion) {
        if (conversacion == null) {
            return null;
        }
        List<Long> participanteIds = conversacion.getParticipantes().stream()
                .map(p -> p.getUsuario().getId())
                .collect(Collectors.toList());

        List<DtUsuario.UsuarioResponseDTO> participantesDtos = conversacion.getParticipantes().stream()
                .map(p -> DtUsuario.UsuarioResponseDTO.fromEntity(p.getUsuario()))
                .collect(Collectors.toList());

        String ultimoContenido = null;
        LocalDateTime ultimaFecha = null;
        
        if (conversacion.getMensajes() != null && !conversacion.getMensajes().isEmpty()) {
            chat.clases.Mensaje m = conversacion.getMensajes().stream()
                    .sorted((m1, m2) -> m2.getFechaEnvio().compareTo(m1.getFechaEnvio()))
                    .findFirst()
                    .orElse(null);
            if (m != null) {
                ultimoContenido = m.getContenido();
                ultimaFecha = m.getFechaEnvio();
            }
        }

        return new DtConversacion(
                conversacion.getId(),
                conversacion.getNombre(),
                conversacion.getTipo(),
                conversacion.getFechaCreacion(),
                participanteIds,
                participantesDtos,
                ultimoContenido,
                ultimaFecha,
                0 // Default no leidos
        );
    }

    public List<DtUsuario.UsuarioResponseDTO> getParticipantes() { return participantes; }
    public void setParticipantes(List<DtUsuario.UsuarioResponseDTO> participantes) { this.participantes = participantes; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoConversacion getTipo() { return tipo; }
    public void setTipo(TipoConversacion tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<Long> getParticipanteIds() { return participanteIds; }
    public void setParticipanteIds(List<Long> participanteIds) { this.participanteIds = participanteIds; }

    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }

    public LocalDateTime getFechaUltimoMensaje() { return fechaUltimoMensaje; }
    public void setFechaUltimoMensaje(LocalDateTime fechaUltimoMensaje) { this.fechaUltimoMensaje = fechaUltimoMensaje; }

    public Integer getNoLeidos() { return noLeidos; }
    public void setNoLeidos(Integer noLeidos) { this.noLeidos = noLeidos; }
}