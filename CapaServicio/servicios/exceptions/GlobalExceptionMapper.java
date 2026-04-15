package chat.servicios.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapeo global de excepciones en la API REST.
 * Convierte cualquier excepción no controlada en una respuesta ErrorResponse estándar.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        // Log de la excepción (en producción, usar un logger real)
        exception.printStackTrace();

        // Determinar el tipo de error y el código de estado
        ErrorResponse errorResponse;
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String tipoError = "INTERNAL_SERVER_ERROR";

        // Manejo específico de excepciones conocidas
        if (exception instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            tipoError = "VALIDATION_ERROR";
            errorResponse = new ErrorResponse(
                    status,
                    "Solicitud inválida",
                    exception.getMessage(),
                    tipoError
            );
        } else if (exception instanceof IllegalStateException) {
            status = Response.Status.CONFLICT.getStatusCode();
            tipoError = "STATE_ERROR";
            errorResponse = new ErrorResponse(
                    status,
                    "Estado inválido",
                    exception.getMessage(),
                    tipoError
            );
        } else if (exception instanceof NullPointerException) {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            tipoError = "NULL_POINTER_ERROR";
            errorResponse = new ErrorResponse(
                    status,
                    "Error interno del servidor",
                    "Referencia nula no esperada",
                    tipoError
            );
        } else if (exception instanceof SecurityException) {
            status = Response.Status.FORBIDDEN.getStatusCode();
            tipoError = "SECURITY_ERROR";
            errorResponse = new ErrorResponse(
                    status,
                    "Acceso denegado",
                    exception.getMessage(),
                    tipoError
            );
        } else {
            // Error genérico
            errorResponse = new ErrorResponse(
                    status,
                    "Error interno del servidor",
                    exception.getClass().getSimpleName() + ": " + exception.getMessage(),
                    tipoError
            );
        }

        return Response.status(status).entity(errorResponse).build();
    }
}
