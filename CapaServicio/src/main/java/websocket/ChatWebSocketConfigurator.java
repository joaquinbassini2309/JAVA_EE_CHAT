package websocket;

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
            // Extraer token del header Sec-WebSocket-Protocol
            java.util.List<String> protocolos = solicitudApretonde.getHeaders()
                    .get("Sec-WebSocket-Protocol");

            if (protocolos != null && !protocolos.isEmpty()) {
                String token = protocolos.get(0).trim();
                // Pasar el token a la sesión para validarlo después
                configuacion.getUserProperties().put("token", token);
                // Establecer el protocolo de respuesta para evitar desconexiones por parte del navegador
                respuestaApretonde.getHeaders().put("Sec-WebSocket-Protocol", java.util.List.of(token));
            }
        } catch (Exception e) {
            System.err.println("Error al extraer token del WebSocket: " + e.getMessage());
        }

        super.modifyHandshake(configuacion, solicitudApretonde, respuestaApretonde);
    }
}
