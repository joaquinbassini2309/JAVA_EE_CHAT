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
        
        // Registrar los recursos de la API
        classes.add(UsuarioResource.class);
        classes.add(ConversacionResource.class);
        classes.add(MensajeResource.class);
        
        // Registrar el manejador de excepciones
        classes.add(exceptions.GlobalExceptionMapper.class);
        
        // Registrar el filtro de seguridad MANUALMENTE
        // Como el filtro ya no tiene @Provider, solo se aplicará
        // a esta aplicación JAX-RS, es decir, a todo bajo /api/v1
        classes.add(seguridad.JWTFilter.class); 

        return classes;
    }
}
