package chat.Sistema;

import chat.Enum.EstadoUsuario;
import chat.Manejadores.ManejadorUsuario;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

/**
 * Inicializador al arrancar el servidor para restablecer el estado de presencia
 * de todos los usuarios a OFFLINE, evitando estados inconsistentes tras reinicios.
 */
@Singleton
@Startup
public class StartupInitializer {

    @Inject
    private ManejadorUsuario usuarioHandler;

    @PostConstruct
    public void alIniciar() {
        System.out.println(">>> StartupInitializer: Restableciendo presencia a OFFLINE...");
        try {
            for (var u : usuarioHandler.listarUsuarios()) {
                if (u.getEstado() == EstadoUsuario.ONLINE) {
                    usuarioHandler.actualizarEstado(u.getId(), EstadoUsuario.OFFLINE);
                }
            }
            System.out.println(">>> StartupInitializer: Presencia restablecida con éxito.");
        } catch (Exception e) {
            System.err.println(">>> StartupInitializer ERROR: " + e.getMessage());
        }
    }
}
