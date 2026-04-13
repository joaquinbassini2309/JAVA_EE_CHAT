package chat.Sistema;

import chat.Enums.EventoTipo;
import chat.Enums.RolParticipante;
import chat.Enums.TipoConversacion;
import chat.Enums.TipoMensaje;
import chat.Factory.ConversacionFactory;
import chat.Factory.MensajeFactory;
import chat.Factory.ParticipanteFactory;
import chat.Factory.UsuarioFactory;
import chat.Manejadores.ManejadorConversacion;
import chat.Manejadores.ManejadorMensaje;
import chat.Manejadores.ManejadorParticipante;
import chat.Manejadores.ManejadorUsuario;
import chat.Observer.ChatObservable;
import chat.Observer.ChatObserver;
import chat.Observer.EventoChat;
import com.example.chat.model.Conversacion;
import com.example.chat.model.Mensaje;
import com.example.chat.model.Participante;
import com.example.chat.model.Usuario;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Optional;

public class Sistema implements ISistema {

    private static final Sistema INSTANCE = new Sistema();

    private final ChatObservable observable = new ChatObservable();
    private EntityManagerFactory emf;

    // manejadores concretos
    private ManejadorUsuario usuarioHandler;
    private ManejadorConversacion conversacionHandler;
    private ManejadorParticipante participanteHandler;
    private ManejadorMensaje mensajeHandler;

    private Sistema() { /* singleton */ }

    public static Sistema getInstance() {
        return INSTANCE;
    }

    @Override
    public void iniciar() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("chatPU");
            usuarioHandler = new ManejadorUsuario(emf);
            conversacionHandler = new ManejadorConversacion(emf);
            participanteHandler = new ManejadorParticipante(emf);
            mensajeHandler = new ManejadorMensaje(emf);
        }
    }

    @Override
    public void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
        usuarioHandler = null;
        conversacionHandler = null;
        participanteHandler = null;
        mensajeHandler = null;
    }

    @Override
    public ManejadorUsuario usuarioHandler() { return usuarioHandler; }

    @Override
    public ManejadorConversacion conversacionHandler() { return conversacionHandler; }

    @Override
    public ManejadorParticipante participanteHandler() { return participanteHandler; }

    @Override
    public ManejadorMensaje mensajeHandler() { return mensajeHandler; }

    @Override
    public Usuario crearUsuario(String username, String email, String passwordHash) {
        Usuario u = usuarioHandler().crearUsuario(username, email, passwordHash);
        observable.notificar(new EventoChat(EventoTipo.USUARIO_CREADO, u));
        return u;
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioHandler().buscarUsuarioPorId(id);
    }

    @Override
    public Conversacion crearConversacion(String nombre, TipoConversacion tipo) {
        Conversacion c = conversacionHandler().crearConversacion(nombre, tipo);
        observable.notificar(new EventoChat(EventoTipo.CONVERSACION_CREADA, c));
        return c;
    }

    @Override
    public Optional<Conversacion> buscarConversacionPorId(Long id) {
        return conversacionHandler().buscarConversacionPorId(id);
    }

    @Override
    public Participante agregarParticipante(Long conversacionId, Long usuarioId, RolParticipante rol) {
        Participante p = participanteHandler().agregarParticipante(conversacionId, usuarioId, rol);
        observable.notificar(new EventoChat(EventoTipo.PARTICIPANTE_AGREGADO, p));
        return p;
    }

    @Override
    public void removerParticipante(Long conversacionId, Long usuarioId) {
        List<Participante> removed = participanteHandler().removerParticipante(conversacionId, usuarioId);
        for (Participante p : removed) {
            observable.notificar(new EventoChat(EventoTipo.PARTICIPANTE_ELIMINADO, p));
        }
    }

    @Override
    public Mensaje enviarMensaje(Long conversacionId, Long emisorId, String contenido, TipoMensaje tipoMensaje, String urlAdjunto) {
        Mensaje m = mensajeHandler().enviarMensaje(conversacionId, emisorId, contenido, tipoMensaje, urlAdjunto);
        observable.notificar(new EventoChat(EventoTipo.MENSAJE_ENVIADO, m));
        return m;
    }

    @Override
    public void marcarMensajeLeido(Long mensajeId) {
        mensajeHandler().marcarMensajeLeido(mensajeId);
    }

    @Override
    public List<Conversacion> obtenerConversacionesDeUsuario(Long usuarioId) {
        return conversacionHandler().obtenerConversacionesDeUsuario(usuarioId);
    }

    @Override
    public void registrarObservador(ChatObserver observer) {
        observable.registrarObservador(observer);
    }

    @Override
    public void eliminarObservador(ChatObserver observer) {
        observable.eliminarObservador(observer);
    }
}