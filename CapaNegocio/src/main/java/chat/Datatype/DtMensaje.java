package chat.Datatype;

import chat.Enum.TipoMensaje;
import chat.clases.Mensaje;

import java.time.LocalDateTime;

public class DtMensaje {
    private Long id;
    private Long conversacionId;
    private Long emisorId;
    private String contenido;
    private TipoMensaje tipo;
    private String urlAdjunto;
    private Boolean leido;
    private LocalDateTime fechaEnvio;
    private String emisorNombre; // Added for convenience in DTO

    public DtMensaje() {
    }

    public static DtMensaje from(Mensaje mensaje) {
        if (mensaje == null) {
            return null;
        }
        DtMensaje dto = new DtMensaje();
        dto.setId(mensaje.getId());
        dto.setConversacionId(mensaje.getConversacion() != null ? mensaje.getConversacion().getId() : null);
        dto.setEmisorId(mensaje.getEmisor() != null ? mensaje.getEmisor().getId() : null);
        dto.setContenido(mensaje.getContenido());
        dto.setTipo(mensaje.getTipoMensaje());
        dto.setUrlAdjunto(mensaje.getUrlAdjunto());
        dto.setLeido(mensaje.getLeido());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        dto.setEmisorNombre(mensaje.getEmisor() != null ? mensaje.getEmisor().getUsername() : null);
        return dto;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversacionId() { return conversacionId; }
    public void setConversacionId(Long conversacionId) { this.conversacionId = conversacionId; }
    public Long getEmisorId() { return emisorId; }
    public void setEmisorId(Long emisorId) { this.emisorId = emisorId; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public TipoMensaje getTipo() { return tipo; }
    public void setTipo(TipoMensaje tipo) { this.tipo = tipo; }
    public String getUrlAdjunto() { return urlAdjunto; }
    public void setUrlAdjunto(String urlAdjunto) { this.urlAdjunto = urlAdjunto; }
    public Boolean getLeido() { return leido; }
    public void setLeido(Boolean leido) { this.leido = leido; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public String getEmisorNombre() { return emisorNombre; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }
}
