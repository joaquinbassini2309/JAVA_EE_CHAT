// java
package chat.clases;

import chat.Enum.EstadoUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore; // Importar la anotación
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    @JsonIgnore // No exponer nunca el hash de la contraseña
    private String passwordHash;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoUsuario estado = EstadoUsuario.OFFLINE;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // <-- ¡AÑADIDO! Rompe el bucle de serialización Usuario -> Participante
    private Set<Participante> participaciones = new HashSet<>();

    @OneToMany(mappedBy = "emisor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Buena práctica añadirlo aquí también para evitar otros bucles
    private Set<Mensaje> mensajesEnviados = new HashSet<>();

    @PrePersist
    private void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    // Configuración completa de Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Set<Participante> getParticipaciones() { return participaciones; }
    public void setParticipaciones(Set<Participante> participaciones) { this.participaciones = participaciones; }

    public Set<Mensaje> getMensajesEnviados() { return mensajesEnviados; }
    public void setMensajesEnviados(Set<Mensaje> mensajesEnviados) { this.mensajesEnviados = mensajesEnviados; }
}
