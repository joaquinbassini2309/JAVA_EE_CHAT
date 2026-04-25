package exceptions;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase estándar para respuestas de error en la API REST.
 * Se utiliza en GlobalExceptionMapper para todas las excepciones.
 */
public class ErrorResponse {
    private int status;
    private String mensaje;
    private String detalle;
    private List<String> errores;
    private LocalDateTime timestamp;
    private String path;
    private String tipoError;

    // Constructores
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String mensaje) {
        this();
        this.status = status;
        this.mensaje = mensaje;
    }

    public ErrorResponse(int status, String mensaje, String detalle) {
        this();
        this.status = status;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }

    public ErrorResponse(int status, String mensaje, String detalle, String tipoError) {
        this();
        this.status = status;
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.tipoError = tipoError;
    }

    public ErrorResponse(int status, String mensaje, List<String> errores) {
        this();
        this.status = status;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    // Getters y Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTipoError() {
        return tipoError;
    }

    public void setTipoError(String tipoError) {
        this.tipoError = tipoError;
    }
}