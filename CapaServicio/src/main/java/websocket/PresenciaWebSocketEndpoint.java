package websocket;

import chat.Enum.EstadoUsuario;
import chat.Sistema.ISistema;
import seguridad.AuthService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

/**
 * WebSocket Endpoint global para gestionar el estado de presencia ONLINE/OFFLINE
 * del usuario mientras esté navegando en la aplicación.
 */
@ApplicationScoped
@ServerEndpoint(value = "/ws/presencia/{usuarioId}", configurator = websocket.ChatWebSocketConfigurator.class)
public class PresenciaWebSocketEndpoint {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService servicioAutenticacion;

    private static final Map<Long, Set<Session>> sesionesPresencia = new ConcurrentHashMap<>();

    @OnOpen
    public void alAbrirConexion(Session sesion, @PathParam("usuarioId") Long idUsuario) {
        try {
            String token = (String) sesion.getUserProperties().get("token");
            if (token == null && sesion.getRequestParameterMap().containsKey("token")) {
                token = sesion.getRequestParameterMap().get("token").get(0);
            }

            if (token == null || !servicioAutenticacion.esTokenValido(token)) {
                sesion.close();
                return;
            }

            sesionesPresencia
                .computeIfAbsent(idUsuario, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(sesion);

            // Actualizar estado del usuario a ONLINE
            sistema.actualizarEstadoUsuario(idUsuario, EstadoUsuario.ONLINE);

        } catch (IOException e) {
            System.err.println("Error al abrir WebSocket de presencia: " + e.getMessage());
        }
    }

    @OnClose
    public void alCerrarConexion(Session sesion, @PathParam("usuarioId") Long idUsuario) {
        Set<Session> sesiones = sesionesPresencia.get(idUsuario);
        if (sesiones != null) {
            sesiones.remove(sesion);
            if (sesiones.isEmpty()) {
                sesionesPresencia.remove(idUsuario);
                // Si no quedan sesiones de presencia activas, marcar como OFFLINE
                sistema.actualizarEstadoUsuario(idUsuario, EstadoUsuario.OFFLINE);
            }
        }
    }

    @OnError
    public void alOcurrirError(Throwable excepcion) {
        System.err.println("Error en WebSocket de presencia: " + excepcion.getMessage());
    }
}
