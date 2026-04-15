package chat;

import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Usuario;

import java.util.Scanner;

public class TestRegistroUsuario {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba interactiva de Registro de Usuario...\n");

        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        Scanner scanner = new Scanner(System.in);

        try {
            boolean registrado = false;

            while (!registrado) {
                System.out.println("=== Registrar Nuevo Usuario ===");
                System.out.print("Ingrese nombre de usuario (username): ");
                String username = scanner.nextLine();

                System.out.print("Ingrese correo electrónico (email): ");
                String email = scanner.nextLine();

                System.out.print("Ingrese contraseña: ");
                String password = scanner.nextLine();

                try {
                    Usuario nuevoUsuario = sistema.registrarUsuario(username, email, password);
                    System.out.println("\nÉXITO: ¡Usuario registrado correctamente!");
                    System.out.println("ID Asignado: " + nuevoUsuario.getId());
                    System.out.println("Username: " + nuevoUsuario.getUsername());
                    System.out.println("Email: " + nuevoUsuario.getEmail());
                    System.out.println("Contraseña (Hash): " + nuevoUsuario.getPasswordHash());
                    System.out.println("Estado Inicial: " + nuevoUsuario.getEstado());
                    registrado = true; // Salimos del bucle
                } catch (IllegalArgumentException e) {
                    System.err.println("\nERROR: " + e.getMessage());
                    System.out.print("¿Desea reintentar el registro? (S/N): ");
                    String opcion = scanner.nextLine();
                    if (opcion.trim().equalsIgnoreCase("N")) {
                        System.out.println("Has cancelado el registro. Saliendo...");
                        break;
                    }
                    System.out.println(); // Salto de línea para el reintento
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
