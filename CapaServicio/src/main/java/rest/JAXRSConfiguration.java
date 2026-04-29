package rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class JAXRSConfiguration extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(UsuarioResource.class);
        classes.add(ConversacionResource.class);
        classes.add(MensajeResource.class);
        classes.add(exceptions.GlobalExceptionMapper.class);
        classes.add(seguridad.CorsFilter.class);
        classes.add(seguridad.JWTFilter.class);
        return classes;
    }
}