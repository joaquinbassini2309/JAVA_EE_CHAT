package chat;

import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Usuario;

public class TestRegistroUsuario {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de Registro de Usuario...\n");

        // Inicializar el sistema (Configura EntityManagerFactory y conectividad BD)
        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        try {
            // Generamos datos dinámicos para evitar conflictos si se corre múltiples veces
            String uniqueSuffix = String.valueOf(System.currentTimeMillis());
            String username = "testuser_" + uniqueSuffix;
            String email = "test_" + uniqueSuffix + "@example.com";
            String passwordPlain = "MiPasswordSeguro123!";

            // -------------------------------------------------------------
            // Escenario 1: Registro exitoso
            // -------------------------------------------------------------
            System.out.println("--- Escenario 1: Registro Exitoso ---");
            Usuario nuevoUsuario = sistema.registrarUsuario(username, email, passwordPlain);
            System.out.println("ÉXITO: Usuario registrado correctamente.");
            System.out.println("ID Asignado: " + nuevoUsuario.getId());

            // -------------------------------------------------------------
            // Escenario 3: Verificación de seguridad
            // -------------------------------------------------------------
            System.out.println("\n--- Escenario 3: Verificación de Seguridad ---");
            System.out.println("Email: " + nuevoUsuario.getEmail());
            System.out.println("Password en texto plano ingresado: " + passwordPlain);
            System.out.println("Hash guardado en el modelo: " + nuevoUsuario.getPasswordHash());
            
            if (nuevoUsuario.getPasswordHash() != null && !nuevoUsuario.getPasswordHash().equals(passwordPlain)) {
                System.out.println("ÉXITO: La contraseña fue encriptada correctamente y NO está en texto plano.");
            } else {
                System.err.println("FALLO: La contraseña parece estar almacenada en texto plano.");
            }

            // -------------------------------------------------------------
            // Escenario 2: Validación de duplicados
            // -------------------------------------------------------------
            System.out.println("\n--- Escenario 2: Validación de Duplicados ---");
            System.out.println("Intentando registrar otro usuario con el EMAIL duplicado: " + email);
            try {
                // Intentamos registrar usando el mismo email
                sistema.registrarUsuario("otro_username", email, "OtraClave");
                System.err.println("FALLO: No se lanzó la excepción esperada. Permitió registrar el duplicado.");
            } catch (IllegalArgumentException e) {
                System.out.println("ÉXITO: Excepción atrapada correctamente. La validación funciona.");
                System.out.println("Mensaje de la excepción: \"" + e.getMessage() + "\"");
            } catch (Exception e) {
                System.err.println("FALLO: Se lanzó una excepción distinta a la esperada: " + e.getClass().getName());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("\nOcurrió un error inesperado al correr las pruebas:");
            e.printStackTrace();
        } finally {
            // Asegurarnos de limpiar recursos
            sistema.cerrar();
            System.out.println("\nPruebas finalizadas. Conexión a la Base de Datos cerrada de manera segura.");
        }
    }
}
