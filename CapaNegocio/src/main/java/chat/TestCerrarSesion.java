package chat;

import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Usuario;

import java.util.Scanner;

public class TestCerrarSesion {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba interactiva de Cerrar Sesión...\n");

        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        Scanner scanner = new Scanner(System.in);

        try {
            // Setup opcional: creamos un usuario de prueba para asegurar que haya uno ONLINE
            String emailPrueba = "logout@test.com";
            try {
                sistema.registrarUsuario("logout_user", emailPrueba, "clave123");
                sistema.iniciarSesion(emailPrueba, "clave123"); // Forzamos ONLINE
                System.out.println("Nota: Se ha registrado y/o logueado un usuario de prueba automáticamente para testear:");
                System.out.println("Email: " + emailPrueba);
                System.out.println("--------------------------------------------------\n");
            } catch (Exception e) {
                // Si el usuario ya existe, lo logueamos directamente por si estaba OFFLINE
                try {
                    sistema.iniciarSesion(emailPrueba, "clave123");
                } catch (Exception ignore) {}
            }

            boolean sesionCerrada = false;

            while (!sesionCerrada) {
                System.out.println("=== Menú: Cerrar Sesión ===");
                System.out.print("Ingrese el correo electrónico de la sesión que desea cerrar: ");
                String email = scanner.nextLine();

                try {
                    sistema.cerrarSesion(email);
                    
                    System.out.println("\nÉXITO: Se ha cerrado la sesión y se ha invalidado el token de autenticación asociado.");
                    System.out.println("El usuario ha pasado al estado de presencia: OFFLINE");
                    
                    sesionCerrada = true; // Salimos de la comprobación
                    System.out.println("\n--- Redirigiendo a la pantalla de Inicio de Sesión... ---");
                    
                } catch (IllegalArgumentException e) {
                    System.err.println("\nERROR: " + e.getMessage());
                    System.out.print("¿Desea reintentar o prefiere cancelar y permanecer en la sesión actual? (R para reintentar / C para cancelar): ");
                    String opcion = scanner.nextLine();
                    if (opcion.trim().equalsIgnoreCase("C")) {
                        System.out.println("Operación cancelada. Permaneciendo en la sesión actual...");
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
