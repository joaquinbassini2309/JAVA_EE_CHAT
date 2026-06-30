package seguridad;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Filtro CORS para añadir las cabeceras necesarias.
 * (Mitigación del hallazgo #21 - Sin CORS configurado explícitamente en el backend).
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin != null && !origin.isBlank()) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
        } else {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, Sec-WebSocket-Protocol");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
