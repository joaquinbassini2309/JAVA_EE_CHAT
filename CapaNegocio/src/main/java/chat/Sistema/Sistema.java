package chat.Sistema;

import chat.Enum.EstadoUsuario;
import chat.Enum.EventoTipo;
import chat.Enum.RolParticipante;
import chat.Enum.TipoConversacion;
import chat.Enum.TipoMensaje;
import chat.Manejadores.ManejadorConversacion;
import chat.Manejadores.ManejadorMensaje;
import chat.Manejadores.ManejadorParticipante;
import chat.Manejadores.ManejadorUsuario;
import chat.Observer.ChatObservable;
import chat.Observer.ChatObserver;
import chat.Observer.EventoChat;
import chat.clases.Conversacion;
import chat.clases.Mensaje;
import chat.clases.Participante;
import chat.clases.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class Sistema implements ISistema {

    private final ChatObservable observable = new ChatObservable();

    @Inject
    private ManejadorUsuario usuarioHandler;

    @Inject
    private ManejadorConversacion conversacionHandler;

    @Inject
    private ManejadorParticipante participanteHandler;

    @Inject
    private ManejadorMensaje mensajeHandler;

    @Inject
    private ISistema sistema;

    @Override
    public ManejadorUsuario usuarioHandler() { return usuarioHandler; }

    @Override
    public ManejadorConversacion conversacionHandler() { return conversacionHandler; }

    @Override
    public ManejadorParticipante participanteHandler() { return participanteHandler; }

    @Override
    public ManejadorMensaje mensajeHandler() { return mensajeHandler; }

    // ========== IMPLEMENTACIÓN: GESTIÓN DE USUARIOS ==========

    @Override
    public Usuario registrarUsuario(String username, String email, String password) {
        // Validar que el correo no esté en uso
        if (usuarioHandler().buscarUsuarioPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado");
        }

        // Hashear password con BCrypt
        String passwordHash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        
        Usuario u = usuarioHandler().crearUsuario(username, email, passwordHash);
        observable.notificar(new EventoChat(EventoTipo.USUARIO_CREADO, u));
        return u;
    }

    @Override
    public Usuario iniciarSesion(String email, String password) {
        // Buscar el usuario por email
        Usuario usuario = usuarioHandler().buscarUsuarioPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        // Verificar que el usuario no esté borrado/inactivo (mediante el atributo activo)
        if (usuario.isActivo() != null && !usuario.isActivo()) {
            throw new IllegalArgumentException("El usuario esta eliminado, pongase en contacto con un administrador.");
        }

        // Verificar la contraseña proporcionada contra el hash almacenado
        if (!org.mindrot.jbcrypt.BCrypt.checkpw(password, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        // Generar sesión activa (cambiando el estado de presencia a ONLINE)
        actualizarEstadoUsuario(usuario.getId(), EstadoUsuario.ONLINE);

        return usuario;
    }

    @Override
    public void cerrarSesion(String email) {
        Usuario usuario = usuarioHandler().buscarUsuarioPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado, no se puede cerrar sesión."));
        
        // Aquí el sistema simula invalidar token/sesión asignando el estado OFFLINE
        actualizarEstadoUsuario(usuario.getId(), EstadoUsuario.OFFLINE);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioHandler().buscarUsuarioPorId(id);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorUsername(String username) {
        return usuarioHandler().buscarUsuarioPorUsername(username);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        return usuarioHandler().buscarUsuarioPorEmail(email);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioHandler().listarUsuarios();
    }

    @Override
    public void actualizarEstadoUsuario(Long usuarioId, EstadoUsuario estado) {
        usuarioHandler().actualizarEstado(usuarioId, estado);
    }

    @Override
    public void actualizarPerfilUsuario(Long usuarioId, String fotoUrl, EstadoUsuario estado) {
        usuarioHandler().actualizarPerfil(usuarioId, fotoUrl, estado);
    }

    // ========== IMPLEMENTACIÓN: CONVERSACIONES ==========

    @Override
    public Conversacion iniciarChatPrivado(Long usuario1Id, Long usuario2Id) {
        if (usuario1Id.equals(usuario2Id)) {
            throw new IllegalArgumentException("No se puede crear un chat privado con uno mismo");
        }

        // Validar que ambos usuarios existen
        Usuario u1 = buscarUsuarioPorId(usuario1Id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario 1 no encontrado"));
        Usuario u2 = buscarUsuarioPorId(usuario2Id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario 2 no encontrado"));

        // Verificar si ya existe un chat privado entre estos usuarios
        Optional<Conversacion> chatExistente = conversacionHandler().buscarChatPrivadoEntre(usuario1Id, usuario2Id);
        if (chatExistente.isPresent()) {
            return chatExistente.get();
        }

        // Crear conversación privada
        String nombreChat = u1.getUsername() + " - " + u2.getUsername();
        Conversacion conversacion = conversacionHandler().crearConversacion(nombreChat, TipoConversacion.PRIVADA);

        // Agregar ambos usuarios como participantes
        participanteHandler().agregarParticipante(conversacion.getId(), usuario1Id, RolParticipante.MIEMBRO);
        participanteHandler().agregarParticipante(conversacion.getId(), usuario2Id, RolParticipante.MIEMBRO);

        observable.notificar(new EventoChat(EventoTipo.CONVERSACION_CREADA, conversacion));
        return conversacion;
    }

    @Override
    public Conversacion crearGrupo(String nombre, Long creadorId, List<Long> miembrosIds) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del grupo no puede estar vacío");
        }

        // Validar que el creador existe
        Usuario creador = buscarUsuarioPorId(creadorId)
                .orElseThrow(() -> new IllegalArgumentException("Creador no encontrado"));

        // Crear grupo
        Conversacion grupo = conversacionHandler().crearConversacion(nombre, TipoConversacion.GRUPO);

        // Agregar al creador como ADMIN
        Participante pCreador = participanteHandler().agregarParticipante(grupo.getId(), creadorId, RolParticipante.ADMIN);
        grupo.getParticipantes().add(pCreador);

        // Agregar miembros como MIEMBRO
        if (miembrosIds != null) {
            for (Long miembroId : miembrosIds) {
                if (!miembroId.equals(creadorId)) { // Evitar duplicar al creador
                    try {
                        Participante pMiembro = participanteHandler().agregarParticipante(grupo.getId(), miembroId, RolParticipante.MIEMBRO);
                        grupo.getParticipantes().add(pMiembro);
                    } catch (Exception e) {
                        // Log y continuar con el siguiente
                        System.err.println("No se pudo agregar miembro " + miembroId + ": " + e.getMessage());
                    }
                }
            }
        }

        observable.notificar(new EventoChat(EventoTipo.CONVERSACION_CREADA, grupo));
        return grupo;
    }

    @Override
    public List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId) {
        return conversacionHandler().obtenerConversacionesDeUsuario(usuarioId);
    }

    @Override
    public Optional<Conversacion> buscarConversacionPorId(Long id) {
        return conversacionHandler().buscarConversacionPorId(id);
    }

    @Override
    public boolean usuarioEstaEnConversacion(Long usuarioId, Long conversacionId) {
        return participanteHandler().existeParticipante(conversacionId, usuarioId);
    }

    // ========== IMPLEMENTACIÓN: PARTICIPANTES ==========

    @Override
    public Participante agregarMiembroAGrupo(Long grupoId, Long usuarioId, Long adminId) {
        // Validar que es un grupo
        Conversacion grupo = buscarConversacionPorId(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        if (grupo.getTipo() != TipoConversacion.GRUPO) {
            throw new IllegalArgumentException("Solo se pueden agregar miembros a grupos");
        }

        // Validar que quien agrega es admin
        Participante admin = participanteHandler().buscarParticipante(grupoId, adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin no es parte del grupo"));
        
        if (admin.getRol() != RolParticipante.ADMIN && admin.getRol() != RolParticipante.MODERADOR) {
            throw new IllegalArgumentException("Solo admins o moderadores pueden agregar miembros");
        }

        // Validar que el usuario no esté ya en el grupo
        if (usuarioEstaEnConversacion(usuarioId, grupoId)) {
            throw new IllegalArgumentException("El usuario ya es miembro del grupo");
        }

        // Agregar participante
        Participante p = participanteHandler().agregarParticipante(grupoId, usuarioId, RolParticipante.MIEMBRO);
        observable.notificar(new EventoChat(EventoTipo.PARTICIPANTE_AGREGADO, p));
        return p;
    }

    @Override
    public void removerMiembroDeGrupo(Long grupoId, Long usuarioId, Long adminId) {
        // Validar permisos del admin
        Participante admin = participanteHandler().buscarParticipante(grupoId, adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin no es parte del grupo"));
        
        if (admin.getRol() != RolParticipante.ADMIN && admin.getRol() != RolParticipante.MODERADOR) {
            throw new IllegalArgumentException("Solo admins o moderadores pueden remover miembros");
        }

        // No permitir que el admin se remueva a sí mismo si es el único admin
        if (adminId.equals(usuarioId)) {
            long cantidadAdmins = participanteHandler().contarAdmins(grupoId);
            if (cantidadAdmins <= 1) {
                throw new IllegalArgumentException("No se puede remover el único admin del grupo");
            }
        }

        List<Participante> removidos = participanteHandler().removerParticipante(grupoId, usuarioId);
        for (Participante p : removidos) {
            observable.notificar(new EventoChat(EventoTipo.PARTICIPANTE_ELIMINADO, p));
        }
    }

    @Override
    public void abandonarGrupo(Long grupoId, Long usuarioId) {
        // Validar que el usuario está en el grupo
        if (!usuarioEstaEnConversacion(usuarioId, grupoId)) {
            throw new IllegalArgumentException("El usuario no es miembro del grupo");
        }

        // Si es admin, validar que no sea el único
        Participante participante = participanteHandler().buscarParticipante(grupoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Participante no encontrado"));
        
        if (participante.getRol() == RolParticipante.ADMIN) {
            long cantidadAdmins = participanteHandler().contarAdmins(grupoId);
            if (cantidadAdmins <= 1) {
                throw new IllegalArgumentException("Debe asignar otro admin antes de abandonar el grupo");
            }
        }

        List<Participante> removidos = participanteHandler().removerParticipante(grupoId, usuarioId);
        for (Participante p : removidos) {
            observable.notificar(new EventoChat(EventoTipo.PARTICIPANTE_ELIMINADO, p));
        }
    }

    @Override
    public void cambiarRolParticipante(Long grupoId, Long usuarioId, RolParticipante nuevoRol, Long adminId) {
        // Validar permisos del admin
        Participante admin = participanteHandler().buscarParticipante(grupoId, adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin no es parte del grupo"));
        
        if (admin.getRol() != RolParticipante.ADMIN) {
            throw new IllegalArgumentException("Solo admins pueden cambiar roles");
        }

        // Cambiar rol
        participanteHandler().actualizarRol(grupoId, usuarioId, nuevoRol);
    }

    // ========== IMPLEMENTACIÓN: MENSAJERÍA ==========

    @Override
    public Mensaje enviarMensajeTexto(Long conversacionId, Long emisorId, String contenido) {
        return enviarMensajeConAdjunto(conversacionId, emisorId, contenido, TipoMensaje.TEXTO, null);
    }

    @Override
    public Mensaje enviarMensajeConAdjunto(Long conversacionId, Long emisorId, String contenido,
                                            TipoMensaje tipo, String urlAdjunto) {
        // Validar que el usuario pertenece a la conversación
        if (!usuarioEstaEnConversacion(emisorId, conversacionId)) {
            throw new IllegalArgumentException("El usuario no pertenece a esta conversación");
        }

        // Validar que la conversación existe
        buscarConversacionPorId(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada"));

        // Enviar mensaje
        Mensaje m = mensajeHandler().enviarMensaje(conversacionId, emisorId, contenido, tipo, urlAdjunto);
        observable.notificar(new EventoChat(EventoTipo.MENSAJE_ENVIADO, m));
        return m;
    }

    @Override
    public List<Mensaje> obtenerMensajesDeConversacion(Long conversacionId, Long usuarioId, int limite) {
        // Validar que el usuario pertenece a la conversación
        if (!usuarioEstaEnConversacion(usuarioId, conversacionId)) {
            throw new IllegalArgumentException("No tienes acceso a esta conversación");
        }

        return mensajeHandler().obtenerMensajes(conversacionId, limite);
    }

    @Override
    public void marcarMensajeComoLeido(Long mensajeId, Long usuarioId) {
        // TODO: Validar que el mensaje pertenece a una conversación donde el usuario participa
        mensajeHandler().marcarMensajeLeido(mensajeId);
    }

    @Override
    public void marcarConversacionComoLeida(Long conversacionId, Long usuarioId) {
        // Validar que el usuario pertenece a la conversación
        if (!usuarioEstaEnConversacion(usuarioId, conversacionId)) {
            throw new IllegalArgumentException("No tienes acceso a esta conversación");
        }

        mensajeHandler().marcarTodosComoLeidos(conversacionId, usuarioId);
    }

    @Override
    public void registrarObservador(ChatObserver observer) {
        observable.registrarObservador(observer);
    }

    @Override
    public void eliminarObservador(ChatObserver observer) {
        observable.eliminarObservador(observer);
    }

    @Override
    public String encriptarMensaje(String contenido) {
        // Implementación simple con Base64; idealmente usar AES
        return java.util.Base64.getEncoder().encodeToString(contenido.getBytes());
    }

    @Override
    public String desencriptarMensaje(String contenidoEncriptado) {
        return new String(java.util.Base64.getDecoder().decode(contenidoEncriptado));
    }
}