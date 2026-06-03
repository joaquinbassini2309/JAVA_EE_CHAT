package chat.Datatype;

import chat.Enum.TipoConversacion;
import chat.clases.Conversacion;
import chat.clases.Participante;
import chat.clases.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DtConversacion {

    private Long id;
    private String nombre;
    private TipoConversacion tipo;
    private LocalDateTime fechaCreacion;
    private String fotoUrl; // Para el avatar en la lista de chats o de grupo
    private String imagenBanner;
    private List<DtParticipante> participantes;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private Integer noLeidos;
    private Long ultimoMensajeEmisorId;

    public DtConversacion() {}

    public DtConversacion(Long id, String nombre, TipoConversacion tipo, LocalDateTime fechaCreacion, List<DtParticipante> participantes, String ultimoMensaje, LocalDateTime fechaUltimoMensaje, Integer noLeidos, String fotoUrl, String imagenBanner, Long ultimoMensajeEmisorId) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.participantes = participantes;
        this.ultimoMensaje = ultimoMensaje;
        this.fechaUltimoMensaje = fechaUltimoMensaje;
        this.noLeidos = noLeidos;
        this.fotoUrl = fotoUrl;
        this.imagenBanner = imagenBanner;
        this.ultimoMensajeEmisorId = ultimoMensajeEmisorId;
    }

    public static DtConversacion from(Conversacion conversacion) {
        return from(conversacion, null, null);
    }

    public static DtConversacion from(Conversacion conversacion, Long usuarioActualId) {
        return from(conversacion, usuarioActualId, null);
    }

    public static DtConversacion from(Conversacion conversacion, Long usuarioActualId, chat.clases.Mensaje ultimoM) {
        if (conversacion == null) {
            return null;
        }

        List<DtParticipante> participantesDtos = conversacion.getParticipantes().stream()
                .map(DtParticipante::from)
                .collect(Collectors.toList());

        String ultimoContenido = "..."; 
        LocalDateTime ultimaFecha = conversacion.getFechaCreacion();
        Long ultimoEmisorId = null;
        
        if (ultimoM != null) {
            ultimoContenido = ultimoM.getContenido();
            ultimaFecha = ultimoM.getFechaEnvio();
            if (ultimoM.getEmisor() != null) {
                ultimoEmisorId = ultimoM.getEmisor().getId();
            }
        }

        String nombreVisible = conversacion.getNombre();
        String fotoVisible = conversacion.getFotoUrl();
        String bannerVisible = conversacion.getImagenBanner();

        if (conversacion.getTipo() == TipoConversacion.PRIVADA && usuarioActualId != null) {
            Usuario otro = conversacion.getParticipantes().stream()
                    .map(Participante::getUsuario)
                    .filter(u -> u != null && !usuarioActualId.equals(u.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (otro != null) {
                nombreVisible = otro.getUsername();
                fotoVisible = otro.getFotoUrl();
                bannerVisible = otro.getImagenBanner();
            }
        }

        return new DtConversacion(
                conversacion.getId(),
                nombreVisible,
                conversacion.getTipo(),
                conversacion.getFechaCreacion(),
                participantesDtos,
                ultimoContenido,
                ultimaFecha,
                0, // Default no leidos
                fotoVisible,
                bannerVisible,
                ultimoEmisorId
        );
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoConversacion getTipo() { return tipo; }
    public void setTipo(TipoConversacion tipo) { this.tipo = tipo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<DtParticipante> getParticipantes() { return participantes; }
    public void setParticipantes(List<DtParticipante> participantes) { this.participantes = participantes; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }
    public LocalDateTime getFechaUltimoMensaje() { return fechaUltimoMensaje; }
    public void setFechaUltimoMensaje(LocalDateTime fechaUltimoMensaje) { this.fechaUltimoMensaje = fechaUltimoMensaje; }
    public Integer getNoLeidos() { return noLeidos; }
    public void setNoLeidos(Integer noLeidos) { this.noLeidos = noLeidos; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public Long getUltimoMensajeEmisorId() { return ultimoMensajeEmisorId; }
    public void setUltimoMensajeEmisorId(Long ultimoMensajeEmisorId) { this.ultimoMensajeEmisorId = ultimoMensajeEmisorId; }
}