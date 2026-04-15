package chat;

import chat.Datatype.DtConversacion;
import chat.Datatype.DtParticipante;
import chat.Sistema.ISistema;
import chat.Sistema.Sistema;
import chat.clases.Conversacion;

import java.util.List;

public class TestCrearGrupo {

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de Crear Grupo de Chat...\n");

        ISistema sistema = Sistema.getInstance();
        sistema.iniciar();

        try {
            String uniqueSuffix = String.valueOf(System.currentTimeMillis());

            // =====================================================
            // Preparación: Crear usuarios disponibles
            // =====================================================
            System.out.println("--- Preparación: Creando Usuarios Disponibles ---");
            var creador = sistema.registrarUsuario(
                    "creador_" + uniqueSuffix,
                    "creador_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            System.out.println("✓ Creador registrado: ID=" + creador.getId() + ", Username=" + creador.getUsername());

            var usuario1 = sistema.registrarUsuario(
                    "usuario1_" + uniqueSuffix,
                    "usuario1_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            System.out.println("✓ Usuario 1 registrado: ID=" + usuario1.getId() + ", Username=" + usuario1.getUsername());

            var usuario2 = sistema.registrarUsuario(
                    "usuario2_" + uniqueSuffix,
                    "usuario2_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            System.out.println("✓ Usuario 2 registrado: ID=" + usuario2.getId() + ", Username=" + usuario2.getUsername());

            var usuario3 = sistema.registrarUsuario(
                    "usuario3_" + uniqueSuffix,
                    "usuario3_" + uniqueSuffix + "@test.com",
                    "Password123!"
            );
            System.out.println("✓ Usuario 3 registrado: ID=" + usuario3.getId() + ", Username=" + usuario3.getUsername());

            // =====================================================
            // Escenario 1: Crear grupo básico con 2 miembros
            // =====================================================
            System.out.println("\n--- Escenario 1: Crear Grupo Básico ---");
            String nombreGrupo1 = "Grupo_Trabajo_" + uniqueSuffix;
            List<Long> miembros1 = List.of(usuario1.getId(), usuario2.getId());

            System.out.println("Nombre del grupo: \"" + nombreGrupo1 + "\"");
            System.out.println("Miembros seleccionados: " + miembros1.size());

            var grupo1Obj = sistema.crearGrupo(nombreGrupo1, creador.getId(), miembros1);
            DtConversacion grupo1 = DtConversacion.from(grupo1Obj);

            System.out.println("✓ Grupo creado exitosamente:");
            System.out.println("  - ID: " + grupo1.id());
            System.out.println("  - Nombre: " + grupo1.nombre());
            System.out.println("  - Tipo: " + grupo1.tipo());
            System.out.println("  - Fecha Creación: " + grupo1.fechaCreacion());
            System.out.println("  - Participantes: " + grupo1.participanteIds().size());

            // =====================================================
            // Escenario 2: Verificar que los miembros están en el grupo
            // =====================================================
            System.out.println("\n--- Escenario 2: Verificar Participantes ---");
            boolean creadorEnGrupo = sistema.usuarioEstaEnConversacion(creador.getId(), grupo1.id());
            boolean usuario1EnGrupo = sistema.usuarioEstaEnConversacion(usuario1.getId(), grupo1.id());
            boolean usuario2EnGrupo = sistema.usuarioEstaEnConversacion(usuario2.getId(), grupo1.id());

            System.out.println("✓ Creador en grupo: " + creadorEnGrupo);
            System.out.println("✓ Usuario 1 en grupo: " + usuario1EnGrupo);
            System.out.println("✓ Usuario 2 en grupo: " + usuario2EnGrupo);

            if (creadorEnGrupo && usuario1EnGrupo && usuario2EnGrupo) {
                System.out.println("✓ ÉXITO: Todos los participantes están correctamente en el grupo.");
            } else {
                System.err.println("✗ FALLO: Algunos participantes no están en el grupo.");
            }

            // =====================================================
            // Escenario 3: Crear grupo con más miembros
            // =====================================================
            System.out.println("\n--- Escenario 3: Crear Grupo con Múltiples Miembros ---");
            String nombreGrupo2 = "Grupo_Proyecto_" + uniqueSuffix;
            List<Long> miembros2 = List.of(usuario1.getId(), usuario2.getId(), usuario3.getId());

            System.out.println("Nombre del grupo: \"" + nombreGrupo2 + "\"");
            System.out.println("Miembros seleccionados: " + miembros2.size());

            var grupo2Obj = sistema.crearGrupo(nombreGrupo2, creador.getId(), miembros2);
            DtConversacion grupo2 = DtConversacion.from(grupo2Obj);

            System.out.println("✓ Grupo creado exitosamente:");
            System.out.println("  - ID: " + grupo2.id());
            System.out.println("  - Nombre: " + grupo2.nombre());
            System.out.println("  - Participantes Totales: " + grupo2.participanteIds().size());

            // =====================================================
            // Escenario 4: Verificar roles de participantes
            // =====================================================
            System.out.println("\n--- Escenario 4: Verificar Roles de Participantes ---");
            var participantesObj = sistema.participanteHandler().buscarParticipantesPorConversacion(grupo1.id());
            List<DtParticipante> participantes = ((List<?>) participantesObj).stream()
                    .map(p -> DtParticipante.from((chat.clases.Participante) p))
                    .toList();

            System.out.println("Total de participantes en grupo 1: " + participantes.size());
            for (DtParticipante p : participantes) {
                String tipoUsuario = p.usuarioId().equals(creador.getId()) ? "(CREADOR)" : "(MIEMBRO)";
                System.out.println("  - Usuario ID: " + p.usuarioId() + " - Rol: " + p.rol() + " " + tipoUsuario);
            }

            // =====================================================
            // Escenario 5: Obtener conversaciones del creador
            // =====================================================
            System.out.println("\n--- Escenario 5: Obtener Conversaciones del Creador ---");
            List<Conversacion> conversacionesCreadorObj = sistema.obtenerConversacionesDeUsuario(creador.getId());

            System.out.println("Total de conversaciones del creador: " + conversacionesCreadorObj.size());
            for (Conversacion c : conversacionesCreadorObj) {
                System.out.println("  - [" + c.getTipo() + "] " + c.getNombre() + " (ID: " + c.getId() + ")");
            }

            // =====================================================
            // Escenario 6: Error - Nombre vacío
            // =====================================================
            System.out.println("\n--- Escenario 6: Validación - Nombre Vacío ---");
            try {
                sistema.crearGrupo("", creador.getId(), List.of(usuario1.getId()));
                System.err.println("✗ FALLO: Permitió crear grupo con nombre vacío.");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ ÉXITO: Se lanzó excepción correctamente.");
                System.out.println("  Mensaje: \"" + e.getMessage() + "\"");
            }

            // =====================================================
            // Escenario 7: Error - Nombre solo con espacios
            // =====================================================
            System.out.println("\n--- Escenario 7: Validación - Nombre Solo Espacios ---");
            try {
                sistema.crearGrupo("   ", creador.getId(), List.of(usuario1.getId()));
                System.err.println("✗ FALLO: Permitió crear grupo con nombre solo espacios.");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ ÉXITO: Se lanzó excepción correctamente.");
                System.out.println("  Mensaje: \"" + e.getMessage() + "\"");
            }

            // =====================================================
            // Escenario 8: Error - Creador no existe
            // =====================================================
            System.out.println("\n--- Escenario 8: Validación - Creador No Existe ---");
            try {
                sistema.crearGrupo("Grupo Fantasma", 999999L, List.of(usuario1.getId()));
                System.err.println("✗ FALLO: Permitió crear grupo con creador inexistente.");
            } catch (IllegalArgumentException e) {
                System.out.println("✓ ÉXITO: Se lanzó excepción correctamente.");
                System.out.println("  Mensaje: \"" + e.getMessage() + "\"");
            }

            // =====================================================
            // Escenario 9: Crear grupo sin miembros adicionales
            // =====================================================
            System.out.println("\n--- Escenario 9: Crear Grupo Sin Miembros Adicionales ---");
            String nombreGrupo3 = "Grupo_Solo_Creador_" + uniqueSuffix;
            var grupo3Obj = sistema.crearGrupo(nombreGrupo3, creador.getId(), List.of());
            DtConversacion grupo3 = DtConversacion.from(grupo3Obj);

            System.out.println("✓ Grupo creado (solo creador):");
            System.out.println("  - Nombre: " + grupo3.nombre());
            System.out.println("  - Participantes: " + grupo3.participanteIds().size());
            System.out.println("  - Solo el creador está: " + grupo3.participanteIds().contains(creador.getId()));

            // =====================================================
            // Escenario 10: Validación - Sin Duplicar Creador
            // =====================================================
            System.out.println("\n--- Escenario 10: Validación - Sin Duplicar Creador ---");
            String nombreGrupo4 = "Grupo_No_Duplicado_" + uniqueSuffix;
            List<Long> miembrosConDuplicado = List.of(creador.getId(), usuario1.getId(), usuario2.getId());

            var grupo4Obj = sistema.crearGrupo(nombreGrupo4, creador.getId(), miembrosConDuplicado);
            DtConversacion grupo4 = DtConversacion.from(grupo4Obj);

            System.out.println("✓ Grupo creado con intento de duplicar creador:");
            System.out.println("  - Participantes solicitados (incluye creador): " + miembrosConDuplicado.size());
            System.out.println("  - Participantes reales en grupo: " + grupo4.participanteIds().size());
            System.out.println("  - Creador solo una vez: " +
                    (grupo4.participanteIds().stream().filter(id -> id.equals(creador.getId())).count() == 1));

            // =====================================================
            // Escenario 11: Verificar que Usuarios Ven el Grupo
            // =====================================================
            System.out.println("\n--- Escenario 11: Verificar que Usuarios Ven el Grupo ---");
            List<Conversacion> conversacionesUser1Obj = sistema.obtenerConversacionesDeUsuario(usuario1.getId());
            List<DtConversacion> gruposUser1 = conversacionesUser1Obj.stream()
                    .map(DtConversacion::from)
                    .filter(c -> c.nombre().equals(nombreGrupo1) || c.nombre().equals(nombreGrupo2))
                    .toList();

            System.out.println("Usuario 1 puede ver " + gruposUser1.size() + " grupos creados:");
            for (DtConversacion c : gruposUser1) {
                System.out.println("  - " + c.nombre());
            }

            // =====================================================
            // Escenario 12: Crear Múltiples Grupos del Mismo Creador
            // =====================================================
            System.out.println("\n--- Escenario 12: Crear Múltiples Grupos del Mismo Creador ---");
            int cantidadGrupos = 3;
            for (int i = 1; i <= cantidadGrupos; i++) {
                String nombreGrupoMulti = "Grupo_" + i + "_" + uniqueSuffix;
                sistema.crearGrupo(nombreGrupoMulti, creador.getId(), List.of(usuario1.getId()));
                System.out.println(i + ". Grupo \"" + nombreGrupoMulti + "\" creado.");
            }

            List<Conversacion> conversacionesCreadorFinal = sistema.obtenerConversacionesDeUsuario(creador.getId());
            System.out.println("✓ Total de conversaciones del creador: " + conversacionesCreadorFinal.size());

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
