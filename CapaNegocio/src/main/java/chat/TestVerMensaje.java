package chat;

import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Mensaje;

import java.util.List;
import java.util.Scanner;

public class TestVerMensaje {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba interactiva de Ver Mensajes...\n");

        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Nota: En un entorno de simulación, utilizaremos los datos");
            System.out.println("insertados por 'init_db.sql' (ej. usuario 1 o 2, conversación 1 o 2).");
            System.out.println("--------------------------------------------------\n");

            boolean visualizado = false;

            while (!visualizado) {
                System.out.println("=== Ver Mensajes ===");
                
                System.out.print("Ingrese su ID de Usuario (Ej. 2 para juanperez): ");
                long usuarioId;
                try {
                    usuarioId = Long.parseLong(scanner.nextLine());
                } catch(NumberFormatException e) {
                    System.err.println("Por favor ingrese un número válido.");
                    continue;
                }

                System.out.print("Ingrese el ID de la Conversación que desea abrir (Ej. 1 o 2): ");
                long conversacionId;
                try {
                     conversacionId = Long.parseLong(scanner.nextLine());
                } catch(NumberFormatException e) {
                     System.err.println("Por favor ingrese un número válido.");
                     continue;
                }

                try {
                    // 1. Obtener mensajes (Esto validará internamente si el usuario pertenece a la conversación)
                    List<Mensaje> mensajes = sistema.obtenerMensajesDeConversacion(conversacionId, usuarioId, 50);
                    
                    // 2. Marcar como leídos
                    sistema.marcarConversacionComoLeida(conversacionId, usuarioId);

                    // 3. Visualizar en pantalla
                    System.out.println("\n--- Mensajes Cargados Exitosamente ---");
                    if (mensajes.isEmpty()) {
                        System.out.println("(La conversación no tiene mensajes aún)");
                    } else {
                        // Iteramos de abajo hacia arriba si queremos simular historial, 
                        // aunque 'obtenerMensajes' los trae ordenados por fecha de envío descendente,
                        // por lo general en chat se muestran cronológicamente.
                        for (int i = mensajes.size() - 1; i >= 0; i--) {
                            Mensaje m = mensajes.get(i);
                            String leidoStatus = (m.getLeido() != null && m.getLeido()) ? "[Leído]" : "[No Leído]";
                            System.out.println(String.format("De: %s | %s %s : %s", 
                                m.getEmisor().getUsername(), 
                                m.getFechaEnvio().toString().substring(0, 16).replace("T", " "), 
                                leidoStatus,
                                m.getContenido()));
                        }
                    }
                    System.out.println("--------------------------------------\n");

                    visualizado = true; // Salimos del bucle
                    
                } catch (IllegalArgumentException e) {
                    System.err.println("\nERROR: " + e.getMessage());
                    System.out.print("¿Desea reintentar o prefiere cancelar? (R para reintentar / C para cancelar): ");
                    String opcion = scanner.nextLine();
                    if (opcion.trim().equalsIgnoreCase("C")) {
                        System.out.println("Operación de visualización cancelada. Saliendo...");
                        break;
                    }
                    System.out.println(); // Salto de línea para un nuevo intento
                }
            }

        } catch (Exception e) {
            System.err.println("\nOcurrió un error inesperado al correr las pruebas:");
            e.printStackTrace();
        } finally {
            scanner.close();
            sistema.cerrar();
            System.out.println("\nPruebas finalizadas. Conexión a la Base de Datos cerrada de manera segura.");
        }
    }
}
