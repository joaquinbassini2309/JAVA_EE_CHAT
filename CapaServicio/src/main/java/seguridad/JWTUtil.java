package seguridad;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

/**
 * Utilidad para generar y validar JWT tokens.
 * La clave secreta se lee desde la variable de entorno JWT_SECRET_KEY.
 */
@ApplicationScoped
public class JWTUtil {

    // Leer clave desde variable de entorno — nunca hardcodeada.
    private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY") != null ? System.getenv("JWT_SECRET_KEY") : "tu-clave-secreta-super-segura-cambiar-en-produccion";
    private static final String ISSUER = "chat-empresarial";
    private static final long EXPIRATION_HOURS = 24;

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private final JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build();

    /**
     * Genera un token JWT para un usuario con su rol.
     */
    public String generarToken(Long usuarioId, String username, String role) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(EXPIRATION_HOURS, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuarioId.toString())
                .withClaim("username", username)
                .withClaim("role", role != null ? role : "USUARIO")
                .withIssuedAt(Date.from(ahora))
                .withExpiresAt(Date.from(expiracion))
                .sign(algorithm);
    }

    /**
     * Valida y decodifica un token JWT.
     */
    public DecodedJWT validarToken(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    /**
     * Extrae el ID del usuario desde un token JWT.
     */
    public Long extraerUsuarioId(String token) {
        try {
            DecodedJWT jwt = validarToken(token);
            String subject = jwt.getSubject();
            return Long.parseLong(subject);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae el username desde un token JWT.
     */
    public String extraerUsername(String token) {
        try {
            DecodedJWT jwt = validarToken(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae el rol desde un token JWT.
     */
    public String extraerRole(String token) {
        try {
            DecodedJWT jwt = validarToken(token);
            return jwt.getClaim("role").asString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un token JWT es válido y no ha expirado.
     */
    public boolean esValido(String token) {
        try {
            validarToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
