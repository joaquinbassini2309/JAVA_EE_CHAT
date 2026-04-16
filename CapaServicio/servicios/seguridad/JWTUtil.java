package chat.servicios.seguridad;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utilidad para generar y validar JWT tokens.
 * Usa una clave secreta configurada para firmar tokens con HS256.
 */
@ApplicationScoped
public class JWTUtil {

    private static final String SECRET_KEY = "tu-clave-secreta-super-segura-cambiar-en-produccion";
    private static final String ISSUER = "chat-empresarial";
    private static final long EXPIRATION_HOURS = 24;

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private final JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build();

    /**
     * Genera un token JWT para un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param username Nombre de usuario
     * @return Token JWT firmado
     */
    public String generarToken(Long usuarioId, String username) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(EXPIRATION_HOURS, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuarioId.toString())
                .withClaim("username", username)
                .withIssuedAt(Date.from(ahora))
                .withExpiresAt(Date.from(expiracion))
                .sign(algorithm);
    }

    /**
     * Valida y decodifica un token JWT.
     * 
     * @param token Token a validar
     * @return DecodedJWT si es válido
     * @throws JWTVerificationException si el token no es válido
     */
    public DecodedJWT validarToken(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    /**
     * Extrae el ID del usuario desde un token JWT.
     * 
     * @param token Token JWT
     * @return ID del usuario (null si es inválido)
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
     * 
     * @param token Token JWT
     * @return Username (null si es inválido)
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
     * Verifica si un token JWT es válido y no ha expirado.
     * 
     * @param token Token a verificar
     * @return true si es válido, false en caso contrario
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
