package chat.Sistema;

import chat.Enum.RolParticipante;
import chat.Enum.TipoConversacion;
import chat.Enum.TipoMensaje;
import chat.Manejadores.ManejadorConversacion;
import chat.Manejadores.ManejadorMensaje;
import chat.Manejadores.ManejadorParticipante;
import chat.Manejadores.ManejadorUsuario;
import chat.Observer.ChatObserver;
import chat.clases.Conversacion;
import chat.clases.Mensaje;
import chat.clases.Participante;
import chat.clases.Usuario;

import java.util.List;
import java.util.Optional;

public interface ISistema {

    // Inicio / lifecycle
    void iniciar();
    void cerrar();

    // Exposición de manejadores concretos
    ManejadorUsuario usuarioHandler();
    ManejadorConversacion conversacionHandler();
    ManejadorParticipante participanteHandler();
    ManejadorMensaje mensajeHandler();

    // ========== CASOS DE USO: GESTIÓN DE USUARIOS ==========

    //Registra un nuevo usuario en el sistema
    Usuario registrarUsuario(String username, String email, String password);
    
    //Busca un usuario por ID
    Optional<Usuario> buscarUsuarioPorId(Long id);
    
    //Busca un usuario por username
    Optional<Usuario> buscarUsuarioPorUsername(String username);

    //Actualiza el estado de conexión del usuario
    void actualizarEstadoUsuario(Long usuarioId, chat.Enum.EstadoUsuario estado);

    // ========== CASOS DE USO: CONVERSACIONES ==========
    
    /**
     * Inicia una conversación privada entre dos usuarios
     * - Crea la conversación
     * - Agrega ambos usuarios como participantes
     * - Notifica a los observadores
     */
    Conversacion iniciarChatPrivado(Long usuario1Id, Long usuario2Id);
    
    /**
     * Crea un grupo con un creador y lista de miembros
     * - Crea la conversación tipo GRUPO
     * - Agrega al creador como ADMIN
     * - Agrega a los miembros como MIEMBRO
     * - Notifica a los observadores
     */
    Conversacion crearGrupo(String nombre, Long creadorId, List<Long> miembrosIds);

    //Obtiene todas las conversaciones de un usuario
    List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId);

    //Busca una conversación por ID
    Optional<Conversacion> buscarConversacionPorId(Long id);

    //Verifica si un usuario pertenece a una conversación
    boolean usuarioEstaEnConversacion(Long usuarioId, Long conversacionId);

    // ========== CASOS DE USO: PARTICIPANTES ==========
    
    /**
     * Agrega un usuario a un grupo existente
     * - Valida que sea un grupo
     * - Valida que el usuario no esté ya en el grupo
     * - Agrega como participante
     * - Notifica a los observadores
     */
    Participante agregarMiembroAGrupo(Long grupoId, Long usuarioId, Long adminId);
    
    /**
     * Remueve un usuario de un grupo
     * - Valida permisos
     * - Remueve participante
     * - Notifica a los observadores
     */
    void removerMiembroDeGrupo(Long grupoId, Long usuarioId, Long adminId);
    
    //Usuario abandona un grupo voluntariamente
    void abandonarGrupo(Long grupoId, Long usuarioId);
    
    //Cambia el rol de un participante en un grupo
    void cambiarRolParticipante(Long grupoId, Long usuarioId, RolParticipante nuevoRol, Long adminId);

    // ========== CASOS DE USO: MENSAJERÍA ==========
    
    /**
     * Envía un mensaje de texto a una conversación
     * - Valida que el usuario pertenezca a la conversación
     * - Crea y persiste el mensaje
     * - Notifica a los observadores
     */
    Mensaje enviarMensajeTexto(Long conversacionId, Long emisorId, String contenido);
    
    /**
     * Envía un mensaje con adjunto (imagen, video, documento, etc.)
     * - Valida que el usuario pertenezca a la conversación
     * - Valida el tipo de mensaje y URL del adjunto
     * - Crea y persiste el mensaje
     * - Notifica a los observadores
     */
    Mensaje enviarMensajeConAdjunto(Long conversacionId, Long emisorId, String contenido, 
                                     TipoMensaje tipo, String urlAdjunto);
    
    //Obtiene el historial de mensajes de una conversación
    List<Mensaje> obtenerMensajesDeConversacion(Long conversacionId, Long usuarioId, int limite);
    
    //Marca un mensaje como leído
    void marcarMensajeComoLeido(Long mensajeId, Long usuarioId);
    
    //Marca todos los mensajes de una conversación como leídos
    void marcarConversacionComoLeida(Long conversacionId, Long usuarioId);

    // Observadores
    void registrarObservador(ChatObserver observer);
    void eliminarObservador(ChatObserver observer);
}