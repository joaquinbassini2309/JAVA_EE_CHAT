package chat.Sistema;

import chat.Manejadores.ManejadorConversacion;
import chat.Manejadores.ManejadorMensaje;
import chat.Manejadores.ManejadorParticipante;
import chat.Manejadores.ManejadorUsuario;
import com.example.chat.model.Conversacion;
import com.example.chat.model.Mensaje;
import com.example.chat.model.Participante;
import com.example.chat.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface ISistema {

    enum EventoTipo {
        USUARIO_CREADO,
        CONVERSACION_CREADA,
        PARTICIPANTE_AGREGADO,
        PARTICIPANTE_ELIMINADO,
        MENSAJE_ENVIADO
    }

    final class Evento {
        private final EventoTipo tipo;
        private final Object payload;

        public Evento(EventoTipo tipo, Object payload) {
            this.tipo = tipo;
            this.payload = payload;
        }

        public EventoTipo getTipo() { return tipo; }
        public Object getPayload() { return payload; }
    }

    interface Observador {
        void onEvento(Evento evento);
    }

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

    Conversacion crearConversacion(String nombre, boolean esGrupo);
    Optional<Conversacion> buscarConversacionPorId(Long id);

    Participante agregarParticipante(Long conversacionId, Long usuarioId, String rol);
    void removerParticipante(Long conversacionId, Long usuarioId);

    Mensaje enviarMensaje(Long conversacionId, Long emisorId, String contenido, String tipoMensaje, String urlAdjunto);
    void marcarMensajeLeido(Long mensajeId);

    List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId);

    // Observadores
    void registrarObservador(Observador o);
    void eliminarObservador(Observador o);
}