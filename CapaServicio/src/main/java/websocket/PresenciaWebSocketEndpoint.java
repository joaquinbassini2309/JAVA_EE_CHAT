package websocket;

import chat.Enum.EstadoUsuario;
import chat.Sistema.ISistema;
import seguridad.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private static final ObjectMapper mapeador = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private final ScheduledExecutorService programador = Executors.newSingleThreadScheduledExecutor();

    // Abrir conexion WebSocket de presencia.
    @OnOpen
    public void alAbrirConexion(Session sesion, @PathParam("usuarioId") Long idUsuario) {
        try {
            String token = (String) sesion.getUserProperties().get("token");

            if (token == null || !servicioAutenticacion.esTokenValido(token)) {
                sesion.close();
                return;
            }

            Long idDelToken = servicioAutenticacion.validarTokenYExtraerIdUsuario(token);
            if (idDelToken == null || !idDelToken.equals(idUsuario)) {
                sesion.close(new jakarta.websocket.CloseReason(
                    jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY,
                    "Identidad inválida"));
                return;
            }

            sesionesPresencia
                .computeIfAbsent(idUsuario, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(sesion);

            sistema.actualizarEstadoUsuario(idUsuario, EstadoUsuario.ONLINE);
            difundirEstadoGlobal(idUsuario, true);

        } catch (IOException e) {
            System.err.println("Error al abrir WebSocket de presencia: " + e.getMessage());
        }
    }

    // Cerrar conexion WebSocket de presencia.
    @OnClose
    public void alCerrarConexion(Session sesion, @PathParam("usuarioId") Long idUsuario) {
        Set<Session> sesiones = sesionesPresencia.get(idUsuario);
        if (sesiones != null) {
            sesiones.remove(sesion);
            if (sesiones.isEmpty()) {
                programador.schedule(() -> {
                    Set<Session> activas = sesionesPresencia.get(idUsuario);
                    if (activas == null || activas.isEmpty()) {
                        sesionesPresencia.remove(idUsuario);
                        sistema.actualizarEstadoUsuario(idUsuario, EstadoUsuario.OFFLINE);
                        difundirEstadoGlobal(idUsuario, false);
                    }
                }, 5, TimeUnit.SECONDS);
            }
        }
    }

    @OnError
    public void alOcurrirError(Throwable excepcion) {
        System.err.println("Error en WebSocket de presencia: " + excepcion.getMessage());
    }

    // Recibir pings para mantener la conexion abierta.
    @jakarta.websocket.OnMessage
    public void alRecibirMensaje(String mensaje, Session sesion) {
        if ("ping".equals(mensaje)) {
            try {
                if (sesion.isOpen()) {
                    sesion.getAsyncRemote().sendText("pong");
                }
            } catch (Exception e) {
                // Silenciar
            }
        }
    }

    // Difundir estado global de presencia a todos los usuarios conectados.
    private void difundirEstadoGlobal(Long idUsuario, boolean conectado) {
        try {
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "PRESENCIA");
            notificacion.put("usuarioId", idUsuario);
            notificacion.put("estado", conectado ? "ONLINE" : "OFFLINE");
            
            String msg = mapeador.writeValueAsString(notificacion);

            for (Set<Session> sesiones : sesionesPresencia.values()) {
                for (Session s : sesiones) {
                    if (s.isOpen()) {
                        s.getAsyncRemote().sendText(msg);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error al difundir estado global de presencia: " + e.getMessage());
        }
    }

    // Nuevo método para notificar eventos globales a un usuario específico
    public static void notificarAUsuario(Long idUsuario, String tipo, Object datos) {
        try {
            Set<Session> sesiones = sesionesPresencia.get(idUsuario);
            if (sesiones != null) {
                Map<String, Object> notificacion = new HashMap<>();
                notificacion.put("tipo", tipo);
                notificacion.put("datos", datos);
                String msg = mapeador.writeValueAsString(notificacion);
                for (Session s : sesiones) {
                    if (s.isOpen()) {
                        s.getAsyncRemote().sendText(msg);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al notificar a usuario en presencia: " + e.getMessage());
        }
    }
}
