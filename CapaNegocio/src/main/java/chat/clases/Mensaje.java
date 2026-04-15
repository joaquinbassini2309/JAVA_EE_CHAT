// java
package chat.clases;

import chat.Enum.TipoMensaje;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes", indexes = {
        @Index(name = "idx_mensajes_conversacion", columnList = "id_conversacion")
})
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", nullable = false)
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor", nullable = false)
    private Usuario emisor;

    @Column(name = "contenido", columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mensaje", length = 20)
    private TipoMensaje tipoMensaje = TipoMensaje.TEXTO;

    @Column(name = "url_adjunto", length = 255)
    private String urlAdjunto;

    @Column(name = "leido")
    private Boolean leido = false;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @PrePersist
    private void prePersist() {
        if (fechaEnvio == null) fechaEnvio = LocalDateTime.now();
    }

    // Configuración completa de Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversacion getConversacion() { return conversacion; }
    public void setConversacion(Conversacion conversacion) { this.conversacion = conversacion; }

    public Usuario getEmisor() { return emisor; }
    public void setEmisor(Usuario emisor) { this.emisor = emisor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public TipoMensaje getTipoMensaje() { return tipoMensaje; }
    public void setTipoMensaje(TipoMensaje tipoMensaje) { this.tipoMensaje = tipoMensaje; }

    public String getUrlAdjunto() { return urlAdjunto; }
    public void setUrlAdjunto(String urlAdjunto) { this.urlAdjunto = urlAdjunto; }

    public Boolean getLeido() { return leido; }
    public void setLeido(Boolean leido) { this.leido = leido; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}