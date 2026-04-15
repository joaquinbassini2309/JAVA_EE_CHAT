package chat;

import chat.Datatype.DtMensaje;
import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Conversacion;
import chat.clases.Usuario;

import java.util.List;

public class TestEnviarMensaje {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de Enviar Mensaje...\n");

        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        try {
            String uniqueSuffix = String.valueOf(System.currentTimeMillis());

            // =====================================================
            // Preparación: Crear 2 usuarios y chat privado
            // =====================================================
            System.out.println("--- Preparación: Creando Usuarios ---");
            Usuario usuario1 = sistema.registrarUsuario(
                    "emisor_" + uniqueSuffix,
                    "emisor_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            System.out.println("✓ Usuario 1 (Emisor) creado: ID=" + usuario1.getId() + ", Username=" + usuario1.getUsername());

            Usuario usuario2 = sistema.registrarUsuario(
                    "receptor_" + uniqueSuffix,
                    "receptor_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            System.out.println("✓ Usuario 2 (Receptor) creado: ID=" + usuario2.getId() + ", Username=" + usuario2.getUsername());

            // =====================================================
            // Escenario 1: Enviar mensaje en chat privado
            // =====================================================
            System.out.println("\n--- Escenario 1: Enviar Mensaje en Chat Privado ---");
            Conversacion chatPrivado = sistema.iniciarChatPrivado(usuario1.getId(), usuario2.getId());
            System.out.println("✓ Chat privado creado: ID=" + chatPrivado.getId() + ", Nombre=" + chatPrivado.getNombre());

            String contenidoOriginal = "¡Hola! Este es mi primer mensaje.";
            System.out.println("Contenido original: \"" + contenidoOriginal + "\"");

            // Encriptar antes de enviar
            String contenidoEncriptado = sistema.encriptarMensaje(contenidoOriginal);
            System.out.println("Contenido encriptado: \"" + contenidoEncriptado + "\"");

            var mensaje1Obj = sistema.enviarMensajeTexto(chatPrivado.getId(), usuario1.getId(), contenidoEncriptado);
            DtMensaje mensaje1 = DtMensaje.from(mensaje1Obj);
            System.out.println("✓ Mensaje enviado exitosamente:");
            System.out.println("  - ID: " + mensaje1.id());
            System.out.println("  - Emisor ID: " + mensaje1.emisorId());
            System.out.println("  - Conversación ID: " + mensaje1.conversacionId());
            System.out.println("  - Estado: LEÍDO=" + mensaje1.leido());

            // Desencriptar para verificar
            String contenidoDesencriptado = sistema.desencriptarMensaje(mensaje1.contenido());
            System.out.println("  - Contenido desencriptado: \"" + contenidoDesencriptado + "\"");

            if (contenidoDesencriptado.equals(contenidoOriginal)) {
                System.out.println("✓ ÉXITO: La encriptación y desencriptación funcionan correctamente.");
            } else {
                System.err.println("✗ FALLO: El contenido no coincide después de desencriptar.");
            }

            // =====================================================
            // Escenario 2: Enviar múltiples mensajes en chat privado
            // =====================================================
            System.out.println("\n--- Escenario 2: Enviar Múltiples Mensajes ---");
            String[] contenidos = {
                    "¿Cómo estás?",
                    "Tengo que contarte algo importante.",
                    "Nos vemos luego."
            };

            for (int i = 0; i < contenidos.length; i++) {
                String contenido = contenidos[i];
                String encriptado = sistema.encriptarMensaje(contenido);
                var msgObj = sistema.enviarMensajeTexto(chatPrivado.getId(), usuario2.getId(), encriptado);
                DtMensaje msg = DtMensaje.from(msgObj);
                System.out.println((i + 1) + ". Mensaje enviado: \"" + contenido + "\" (ID=" + msg.id() + ")");
            }

            // =====================================================
            // Escenario 3: Obtener historial de mensajes
            // =====================================================
            System.out.println("\n--- Escenario 3: Obtener Historial de Mensajes ---");
            var historialObj = sistema.obtenerMensajesDeConversacion(chatPrivado.getId(), usuario1.getId(), 10);
            List<DtMensaje> historial = historialObj.stream().map(DtMensaje::from).toList();
            System.out.println("Total de mensajes en la conversación: " + historial.size());
            for (int i = 0; i < historial.size(); i++) {
                DtMensaje msg = historial.get(i);
                String desencriptado = sistema.desencriptarMensaje(msg.contenido());
                System.out.println((i + 1) + ". [" + msg.emisorId() + "] " + desencriptado);
            }

            // =====================================================
            // Escenario 4: Marcar mensaje como leído
            // =====================================================
            System.out.println("\n--- Escenario 4: Marcar Mensaje como Leído ---");
            if (!historial.isEmpty()) {
                DtMensaje primerMensaje = historial.get(0);
                System.out.println("Estado antes: Leído=" + primerMensaje.leido());
                sistema.marcarMensajeComoLeido(primerMensaje.id(), usuario1.getId());
                System.out.println("✓ Mensaje marcado como leído.");
            }

            // =====================================================
            // Escenario 5: Crear grupo y enviar mensaje
            // =====================================================
            System.out.println("\n--- Escenario 5: Enviar Mensaje en Grupo ---");
            List<Long> miembros = List.of(usuario2.getId());
            Conversacion grupo = sistema.crearGrupo(
                    "Grupo_Prueba_" + uniqueSuffix,
                    usuario1.getId(),
                    miembros
            );
            System.out.println("✓ Grupo creado: ID=" + grupo.getId() + ", Nombre=" + grupo.getNombre());

            String mensajeGrupo = "¡Hola a todos en el grupo!";
            String mensajeGrupoEncriptado = sistema.encriptarMensaje(mensajeGrupo);
            var msgGrupoObj = sistema.enviarMensajeTexto(grupo.getId(), usuario1.getId(), mensajeGrupoEncriptado);
            DtMensaje msgGrupo = DtMensaje.from(msgGrupoObj);
            System.out.println("✓ Mensaje enviado al grupo: \"" + mensajeGrupo + "\"");
            System.out.println("  - Desencriptado: \"" + sistema.desencriptarMensaje(msgGrupo.contenido()) + "\"");

            // =====================================================
            // Escenario 6: Marcar conversación como leída
            // =====================================================
            System.out.println("\n--- Escenario 6: Marcar Conversación como Leída ---");
            sistema.marcarConversacionComoLeida(chatPrivado.getId(), usuario1.getId());
            System.out.println("✓ Conversación marcada como completamente leída.");

            // =====================================================
            // Escenario 7: Error - Usuario no pertenece a conversación
            // =====================================================
            System.out.println("\n--- Escenario 7: Validación - Usuario No Pertenece a Conversación ---");
            Usuario usuario3 = sistema.registrarUsuario(
                    "externo_" + uniqueSuffix,
                    "externo_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            try {
                String mensajeNoAutorizado = "Intento no autorizado";
                String encriptado = sistema.encriptarMensaje(mensajeNoAutorizado);
                sistema.enviarMensajeTexto(chatPrivado.getId(), usuario3.getId(), encriptado);
                System.err.println("✗ FALLO: Permitió enviar mensaje a usuario no autorizado.");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ ÉXITO: Se lanzó excepción correctamente.");
                System.out.println("  Mensaje: \"" + e.getMessage() + "\"");
            }

            // =====================================================
            // Escenario 8: Error - Conversación no existe
            // =====================================================
            System.out.println("\n--- Escenario 8: Validación - Conversación No Existe ---");
            try {
                String contenido = "Mensaje a conversación fantasma";
                String encriptado = sistema.encriptarMensaje(contenido);
                sistema.enviarMensajeTexto(999999L, usuario1.getId(), encriptado);
                System.err.println("✗ FALLO: Permitió enviar a conversación inexistente.");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ ÉXITO: Se lanzó excepción correctamente.");
                System.out.println("  Mensaje: \"" + e.getMessage() + "\"");
            }

            System.out.println("\n✓ TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE.");

        } catch (Exception e) {
            System.err.println("\n✗ Error inesperado durante las pruebas:");
            e.printStackTrace();
        } finally {
            sistema.cerrar();
            System.out.println("\nPruebas finalizadas. Conexión cerrada.");
        }
    }
}