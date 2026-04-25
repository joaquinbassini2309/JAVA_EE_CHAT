package websocket;

import chat.Datatype.DtMensaje;
import chat.Enum.EstadoUsuario;
import chat.Sistema.ISistema;
import chat.clases.Mensaje;
import chat.servicios.seguridad.AuthService;
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
 * Estados de conexión: Cuando un usuario se conecta por WebSocket, actualiza su EstadoUsuario a ONLINE
 */
@ApplicationScoped
@ServerEndpoint(value = "/api/v1/websocket/conversacion/{conversacionId}/usuario/{usuarioId}", configurator = websocket.ChatWebSocketConfigurator.class)
public class ChatWebSocketEndpoint {

    @Inject
    private ISistema sistema;

    @Inject
    private AuthService servicioAutenticacion;

    private static final ObjectMapper mapeador = new ObjectMapper();

    // Almacena sesiones activas: clave = "conversacionId:usuarioId"
    private static final Map<String, Set<Session>> sesionesActivas = new ConcurrentHashMap<>();

    @OnOpen
    public void alAbrirConexion(Session sesion,
                                @PathParam("conversacionId") Long idConversacion,
                                @PathParam("usuarioId") Long idUsuario) {
        try {
            // Validar token JWT desde los parámetros de la sesión
            String token = (String) sesion.getUserProperties().get("token");
            if (token == null || !servicioAutenticacion.esTokenValido(token)) {
                sesion.close();
                return;
            }

            // Verificar que el usuario existe y está en la conversación
            if (!sistema.usuarioEstaEnConversacion(idUsuario, idConversacion)) {
                sesion.close();
                return;
            }

            String claveSesion = idConversacion + ":" + idUsuario;
            sesionesActivas.computeIfAbsent(claveSesion, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(sesion);

            // Actualizar estado del usuario a ONLINE
            sistema.actualizarEstadoUsuario(idUsuario, EstadoUsuario.ONLINE);

            // Notificar a otros usuarios en la conversación
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
                    mensajeDTO.getUrlAdjunto()
            );

            // Convertir a DTO para broadcast
            DtMensaje dtMensaje = DtMensaje.from(mensajeGuardado);

            // Enviar a todos los usuarios conectados en la conversación
            difundirMensaje(idConversacion, dtMensaje);

        } catch (Exception e) {
            enviarError(sesion, "Error al procesar mensaje: " + e.getMessage());
        }
    }

    @OnClose
    public void alCerrarConexion(Session sesion,
                                @PathParam("conversacionId") Long idConversacion,
                                @PathParam("usuarioId") Long idUsuario) {
        try {
            String claveSesion = idConversacion + ":" + idUsuario;
            Set<Session> sesiones = sesionesActivas.get(claveSesion);
            
            if (sesiones != null) {
                sesiones.remove(sesion);
                if (sesiones.isEmpty()) {
                    sesionesActivas.remove(claveSesion);
                    
                    // Si no hay más sesiones del usuario, actualizar estado a OFFLINE
                    if (!tieneOtrasSesionesActivas(idUsuario)) {
                        sistema.actualizarEstadoUsuario(idUsuario, EstadoUsuario.OFFLINE);
                    }
                    
                    // Notificar desconexión
                    notificarConexionDesconexion(idConversacion, idUsuario, false);
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

    /**
     * Difunde un mensaje a todos los usuarios conectados en una conversación
     */
    private void difundirMensaje(Long idConversacion, DtMensaje mensaje) throws IOException {
        String datosJson = mapeador.writeValueAsString(new MensajeWebSocketRespuesta("mensaje", mensaje));
        difundir(idConversacion, datosJson);
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
                    notificacion
            ));

            difundir(idConversacion, datosJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error al notificar conexión/desconexión: " + e.getMessage());
        }
    }

    /**
     * Envía datos a todos los usuarios de una conversación
     */
    private void difundir(Long idConversacion, String datosJson) {
        for (Map.Entry<String, Set<Session>> entrada : sesionesActivas.entrySet()) {
            String[] partes = entrada.getKey().split(":");
            if (Long.parseLong(partes[0]) == idConversacion) {
                for (Session sesion : entrada.getValue()) {
                    if (sesion.isOpen()) {
                        sesion.getAsyncRemote().sendText(datosJson);
                    }
                }
            }
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
        return sesionesActivas.entrySet().stream()
                .anyMatch(entrada -> {
                    String[] partes = entrada.getKey().split(":");
                    return partes.length == 2 && Long.parseLong(partes[1]) == idUsuario && !entrada.getValue().isEmpty();
                });
    }

    /**
     * DTO para recibir mensajes del cliente
     */
    public static class MensajeWebSocketDTO {
        private String contenido;
        private String tipoMensaje;
        private String urlAdjunto;

        public String getContenido() { return contenido; }
        public void setContenido(String contenido) { this.contenido = contenido; }

        public String getTipoMensaje() { return tipoMensaje; }
        public void setTipoMensaje(String tipoMensaje) { this.tipoMensaje = tipoMensaje; }

        public String getUrlAdjunto() { return urlAdjunto; }
        public void setUrlAdjunto(String urlAdjunto) { this.urlAdjunto = urlAdjunto; }

        public chat.Enum.TipoMensaje obtenerTipoMensaje() {
            if (tipoMensaje == null) return chat.Enum.TipoMensaje.TEXTO;
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

        public String getTipo() { return tipo; }
        public Object getDatos() { return datos; }
    }
}