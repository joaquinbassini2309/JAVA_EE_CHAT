package websocket;

import chat.Datatype.DtMensaje;
import chat.Enum.EstadoUsuario;
import chat.Sistema.ISistema;
import chat.clases.Mensaje;
import seguridad.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket + REST híbrido:
 * REST para operaciones CRUD (crear conversación, buscar usuarios)
 * WebSocket SOLO para mensajes en tiempo real y notificaciones
 * 
 * Estados de conexión: Cuando un usuario se conecta por WebSocket, actualiza su
 * EstadoUsuario a ONLINE
 */
@ApplicationScoped
@ServerEndpoint(value = "/ws/conversacion/{conversacionId}/usuario/{usuarioId}", configurator = websocket.ChatWebSocketConfigurator.class)
public class ChatWebSocketEndpoint {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService servicioAutenticacion;

    private static final ObjectMapper mapeador = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Almacena sesiones activas: clave interna = usuarioId, clave externa = conversacionId
    private static final Map<Long, Map<Long, Set<Session>>> sesionesActivas = new ConcurrentHashMap<>();

    @OnOpen
    public void alAbrirConexion(Session sesion,
            @PathParam("conversacionId") Long idConversacion,
            @PathParam("usuarioId") Long idUsuario) {
        try {
            // Obtener token JWT únicamente desde el header (ChatWebSocketConfigurator).
            // NO aceptar token como query param — quedaría expuesto en logs de proxy/servidor.
            String token = (String) sesion.getUserProperties().get("token");

            if (token == null || !servicioAutenticacion.esTokenValido(token)) {
                sesion.close();
                return;
            }

            // Validar que el subject del JWT coincide con el idUsuario del path param.
            Long idDelToken = servicioAutenticacion.validarTokenYExtraerIdUsuario(token);
            if (idDelToken == null || !idDelToken.equals(idUsuario)) {
                sesion.close(new jakarta.websocket.CloseReason(
                    jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY,
                    "Identidad inválida"));
                return;
            }

            // Verificar que el usuario está en la conversación.
            if (!sistema.usuarioEstaEnConversacion(idUsuario, idConversacion)) {
                sesion.close();
                return;
            }

            sesionesActivas
                .computeIfAbsent(idConversacion, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(idUsuario, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(sesion);

            notificarConexionDesconexion(idConversacion, idUsuario, true);

        } catch (IOException e) {
            System.err.println("Error al abrir WebSocket: " + e.getMessage());
        }
    }

    @OnMessage
    public void alRecibirMensaje(String datosJson, Session sesion,
            @PathParam("conversacionId") Long idConversacion,
            @PathParam("usuarioId") Long idUsuario) {
        try {
            // Parsear DTO del mensaje
            MensajeWebSocketDTO mensajeDTO = mapeador.readValue(datosJson, MensajeWebSocketDTO.class);

            if (mensajeDTO.getContenido() == null || mensajeDTO.getContenido().isBlank()) {
                enviarError(sesion, "El contenido del mensaje es requerido");
                return;
            }

            // Verificar que el usuario está en la conversación
            if (!sistema.usuarioEstaEnConversacion(idUsuario, idConversacion)) {
                enviarError(sesion, "No tienes acceso a esta conversación");
                return;
            }

            // Guardar el mensaje en BD
            Mensaje mensajeGuardado = sistema.enviarMensajeConAdjunto(
                    idConversacion,
                    idUsuario,
                    mensajeDTO.getContenido(),
                    mensajeDTO.obtenerTipoMensaje(),
                    mensajeDTO.getUrlAdjunto());

            // Convertir a DTO para broadcast
            DtMensaje dtMensaje = DtMensaje.from(mensajeGuardado);

            // Enviar a todos los usuarios conectados en la conversación
            difundirMensaje(idConversacion, dtMensaje);

            // Notificar globalmente a todos los participantes usando PresenciaWS para actualizar el sidebar
            for (chat.clases.Participante participante : mensajeGuardado.getConversacion().getParticipantes()) {
                if (participante.getUsuario() != null) {
                    websocket.PresenciaWebSocketEndpoint.notificarAUsuario(
                        participante.getUsuario().getId(), 
                        "NUEVO_MENSAJE_GLOBAL", 
                        dtMensaje
                    );
                }
            }

        } catch (Exception e) {
            enviarError(sesion, "Error al procesar mensaje: " + e.getMessage());
        }
    }

    @OnClose
    public void alCerrarConexion(Session sesion,
            @PathParam("conversacionId") Long idConversacion,
            @PathParam("usuarioId") Long idUsuario) {
        try {
            Map<Long, Set<Session>> usuariosMap = sesionesActivas.get(idConversacion);
            if (usuariosMap != null) {
                Set<Session> sesiones = usuariosMap.get(idUsuario);
                if (sesiones != null) {
                    sesiones.remove(sesion);
                    if (sesiones.isEmpty()) {
                        usuariosMap.remove(idUsuario);
                        if (usuariosMap.isEmpty()) {
                            sesionesActivas.remove(idConversacion);
                        }

                        // Notificar desconexión
                        notificarConexionDesconexion(idConversacion, idUsuario, false);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar WebSocket: " + e.getMessage());
        }
    }

    @OnError
    public void alOcurrirError(Throwable excepcion) {
        System.err.println("Error en WebSocket: " + excepcion.getMessage());
        excepcion.printStackTrace();
    }

    public static void difundirMensaje(Long idConversacion, DtMensaje mensaje) {
        try {
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensaje", mensaje));
            difundir(idConversacion, datosJson);
        } catch (Exception e) {
            System.err.println("Error al difundir mensaje: " + e.getMessage());
        }
    }

    /**
     * Difunde un mensaje resaltado a todos los usuarios conectados en una conversación
     */
    public static void difundirMensajeResaltado(Long idConversacion, DtMensaje mensaje) {
        try {
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensaje_resaltado", mensaje));
            difundir(idConversacion, datosJson);
        } catch (Exception e) {
            System.err.println("Error al difundir mensaje resaltado: " + e.getMessage());
        }
    }

    /**
     * Difunde la eliminación de un mensaje a todos los usuarios conectados en una conversación
     */
    public static void difundirMensajeEliminado(Long idConversacion, DtMensaje mensaje) {
        try {
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensaje_eliminado", mensaje));
            difundir(idConversacion, datosJson);
        } catch (Exception e) {
            System.err.println("Error al difundir mensaje eliminado por WebSocket: " + e.getMessage());
        }
    }

    /**
     * Notifica a todos los participantes conectados que los mensajes de una conversación
     * fueron leídos por un usuario.
     */
    public static void difundirMensajesLeidos(Long idConversacion, Long idUsuario) {
        try {
            java.util.Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("conversacionId", idConversacion);
            datos.put("usuarioId", idUsuario);
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensajes_leidos", datos));
            difundir(idConversacion, datosJson);
        } catch (Exception e) {
            System.err.println("Error al difundir mensajes leídos por WebSocket: " + e.getMessage());
        }
    }

    /**
     * Notifica a todos los participantes conectados que un mensaje fue fijado
     */
    public static void notificarMensajeFijado(Long idConversacion, DtMensaje mensajeFijado) {
        try {
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensajeFijado", mensajeFijado));
            difundir(idConversacion, datosJson);
        } catch (Exception e) {
            System.err.println("Error al difundir mensaje fijado por WebSocket: " + e.getMessage());
        }
    }

    /**
     * Notifica a todos los participantes conectados que el mensaje fijado fue quitado
     */
    public static void notificarMensajeDesfijado(Long idConversacion) {
        try {
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("idConversacion", idConversacion);
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensajeDesfijado", notificacion));
            difundir(idConversacion, datosJson);
        } catch (Exception e) {
            System.err.println("Error al difundir mensaje desfijado por WebSocket: " + e.getMessage());
        }
    }

    /**
     * Notifica cuando un usuario se conecta o desconecta
     */
    private void notificarConexionDesconexion(Long idConversacion, Long idUsuario, boolean conectado) {
        try {
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", conectado ? "usuarioConectado" : "usuarioDesconectado");
            notificacion.put("idUsuario", idUsuario);

            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta(
                    conectado ? "usuarioConectado" : "usuarioDesconectado",
                    notificacion));

            difundir(idConversacion, datosJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error al notificar conexión/desconexión: " + e.getMessage());
        }
    }

    /**
     * Envía datos a todos los usuarios de una conversación
     */
    private static void difundir(Long idConversacion, String datosJson) {
        Map<Long, Set<Session>> usuariosMap = sesionesActivas.get(idConversacion);
        if (usuariosMap != null) {
            for (Set<Session> sesiones : usuariosMap.values()) {
                for (Session sesion : sesiones) {
                    if (sesion.isOpen()) {
                        sesion.getAsyncRemote().sendText(datosJson);
                    }
                }
            }
        }
    }

    /**
     * Envía datos a todas las sesiones activas de un usuario específico
     */
    public static void notificarAUsuario(Long idUsuario, String tipo, Object datos) {
        try {
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta(tipo, datos));
            for (Map<Long, Set<Session>> usuariosMap : sesionesActivas.values()) {
                Set<Session> sesiones = usuariosMap.get(idUsuario);
                if (sesiones != null) {
                    for (Session sesion : sesiones) {
                        if (sesion.isOpen()) {
                            sesion.getAsyncRemote().sendText(datosJson);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al notificar a usuario por WebSocket: " + e.getMessage());
        }
    }

    /**
     * Envía un mensaje de error al cliente
     */
    private void enviarError(Session sesion, String mensaje) {
        try {
            String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("error", mensaje));
            if (sesion.isOpen()) {
                sesion.getAsyncRemote().sendText(datosJson);
            }
        } catch (Exception e) {
            System.err.println("Error al enviar error: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario tiene otras sesiones activas en otras conversaciones
     */
    private boolean tieneOtrasSesionesActivas(Long idUsuario) {
        return sesionesActivas.values().stream()
                .anyMatch(usuariosMap -> {
                    Set<Session> sesiones = usuariosMap.get(idUsuario);
                    return sesiones != null && !sesiones.isEmpty();
                });
    }

    /**
     * DTO para recibir mensajes del cliente
     */
    public static class MensajeWebSocketDTO {
        private String contenido;
        private String tipoMensaje;
        private String urlAdjunto;

        public String getContenido() {
            return contenido;
        }

        public void setContenido(String contenido) {
            this.contenido = contenido;
        }

        public String getTipoMensaje() {
            return tipoMensaje;
        }

        public void setTipoMensaje(String tipoMensaje) {
            this.tipoMensaje = tipoMensaje;
        }

        public String getUrlAdjunto() {
            return urlAdjunto;
        }

        public void setUrlAdjunto(String urlAdjunto) {
            this.urlAdjunto = urlAdjunto;
        }

        public chat.Enum.TipoMensaje obtenerTipoMensaje() {
            if (tipoMensaje == null)
                return chat.Enum.TipoMensaje.TEXTO;
            try {
                return chat.Enum.TipoMensaje.valueOf(tipoMensaje.toUpperCase());
            } catch (IllegalArgumentException e) {
                return chat.Enum.TipoMensaje.TEXTO;
            }
        }
    }

    /**
     * DTO para enviar respuestas al cliente
     */
    public static class MensajeWebSocketRespuesta {
        private final String tipo;
        private final Object datos;

        public MensajeWebSocketRespuesta(String tipo, Object datos) {
            this.tipo = tipo;
            this.datos = datos;
        }

        public String getTipo() {
            return tipo;
        }

        public Object getDatos() {
            return datos;
        }
    }
}