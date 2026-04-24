package chat.servicios.websocket;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * Configurador para WebSocket que extrae el token JWT del header Authorization
 */
public class ChatWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig configuacion,
                                HandshakeRequest solicitudApretonde,
                                HandshakeResponse respuestaApretonde) {
        try {
            // Extraer token del header Authorization
            java.util.List<String> encabezadosAuth = solicitudApretonde.getHeaders()
                    .get("Authorization");

            if (encabezadosAuth != null && !encabezadosAuth.isEmpty()) {
                String encabezado = encabezadosAuth.get(0);
                if (encabezado.startsWith("Bearer ")) {
                    String token = encabezado.substring("Bearer ".length());
                    // Pasar el token a la sesión para validarlo después
                    configuacion.getUserProperties().put("token", token);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al extraer token del WebSocket: " + e.getMessage());
        }

        super.modifyHandshake(configuacion, solicitudApretonde, respuestaApretonde);
    }
}
