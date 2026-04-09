// java
package chat.clases;

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
    private String passwordHash;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "estado", length = 20)
    private String estado = "offline";

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participante> participaciones = new HashSet<>();

    @OneToMany(mappedBy = "emisor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Mensaje> mensajesEnviados = new HashSet<>();

    @PrePersist
    private void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    // getters y setters
    // (omitir aquí por brevedad; generar en IDE)
}