// java
package chat.clases;

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

    @Lob
    @Column(name = "contenido", nullable = false)
    private String contenido;

    @Column(name = "tipo_mensaje", length = 20)
    private TipoMensaje = "TEXTO";

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

    // getters y setters
}