package chat.Sistema;

import chat.Enums.RolParticipante;
import chat.Enums.TipoConversacion;
import chat.Enums.TipoMensaje;
import chat.Manejadores.ManejadorConversacion;
import chat.Manejadores.ManejadorMensaje;
import chat.Manejadores.ManejadorParticipante;
import chat.Manejadores.ManejadorUsuario;
import chat.Observer.ChatObserver;
import com.example.chat.model.Conversacion;
import com.example.chat.model.Mensaje;
import com.example.chat.model.Participante;
import com.example.chat.model.Usuario;

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

    // CRUD básicos
    Usuario crearUsuario(String username, String email, String passwordHash);
    Optional<Usuario> buscarUsuarioPorId(Long id);

    Conversacion crearConversacion(String nombre, TipoConversacion tipo);
    Optional<Conversacion> buscarConversacionPorId(Long id);

    Participante agregarParticipante(Long conversacionId, Long usuarioId, RolParticipante rol);
    void removerParticipante(Long conversacionId, Long usuarioId);

    Mensaje enviarMensaje(Long conversacionId, Long emisorId, String contenido, TipoMensaje tipoMensaje, String urlAdjunto);
    void marcarMensajeLeido(Long mensajeId);

    List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId);

    // Observadores
    void registrarObservador(ChatObserver observer);
    void eliminarObservador(ChatObserver observer);
}