package chat;

import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Usuario;

import java.util.Scanner;

public class TestIniciarSesion {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de Iniciar Sesión...\n");

        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        Scanner scanner = new Scanner(System.in);
        
        try {
            // Generamos un usuario de prueba para poder hacer login
            String emailPrueba = "usuario@test.com";
            String passwordPrueba = "123456";
            try {
                sistema.registrarUsuario("testuser", emailPrueba, passwordPrueba);
                System.out.println("Nota: Se ha registrado un usuario de prueba automáticamente para facilitar el testeo:");
                System.out.println("Email: " + emailPrueba);
                System.out.println("Contraseña: " + passwordPrueba);
                System.out.println("--------------------------------------------------\n");
            } catch (Exception e) {
                // El usuario probablemente ya existe, ignoramos.
            }

            boolean sesionIniciada = false;

            while (!sesionIniciada) {
                System.out.println("=== Iniciar Sesión ===");
                System.out.print("Ingrese correo electrónico: ");
                String email = scanner.nextLine();
                
                System.out.print("Ingrese contraseña: ");
                String password = scanner.nextLine();

                try {
                    Usuario usuarioLogueado = sistema.iniciarSesion(email, password);
                    System.out.println("\nÉXITO: ¡Bienvenido " + usuarioLogueado.getUsername() + "!");
                    System.out.println("Estado actual de la sesión: " + usuarioLogueado.getEstado());
                    sesionIniciada = true; // Salimos del bucle
                } catch (IllegalArgumentException e) {
                    System.err.println("\nERROR: " + e.getMessage());
                    System.out.print("¿Desea reintentar el acceso? (S/N): ");
                    String opcion = scanner.nextLine();
                    if (opcion.trim().equalsIgnoreCase("N")) {
                        System.out.println("Has cancelado el inicio de sesión. Saliendo...");
                        break;
                    }
                    System.out.println(); // Salto de línea antes del próximo intento
                }
            }

        } catch (Exception e) {
            System.err.println("\nOcurrió un error inesperado:");
            e.printStackTrace();
        } finally {
            scanner.close();
            sistema.cerrar();
            System.out.println("\nPruebas finalizadas. Conexión a la Base de Datos cerrada de manera segura.");
        }
    }
}
