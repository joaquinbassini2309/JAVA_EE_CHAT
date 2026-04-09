package chat.Sistema;

import chat.Manejadores.ManejadorConversacion;
import chat.Manejadores.ManejadorMensaje;
import chat.Manejadores.ManejadorParticipante;
import chat.Manejadores.ManejadorUsuario;
import com.example.chat.model.Conversacion;
import com.example.chat.model.Mensaje;
import com.example.chat.model.Participante;
import com.example.chat.model.Usuario;
import jakarta.persistence.Persistence;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sistema implements ISistema {

    private static final Sistema INSTANCE = new Sistema();

    private final List<ISistema.Observador> observadores = new CopyOnWriteArrayList<>();
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
        notificar(new ISistema.Evento(ISistema.EventoTipo.USUARIO_CREADO, u));
        return u;
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioHandler().buscarUsuarioPorId(id);
    }

    @Override
    public Conversacion crearConversacion(String nombre, boolean esGrupo) {
        Conversacion c = conversacionHandler().crearConversacion(nombre, esGrupo);
        notificar(new ISistema.Evento(ISistema.EventoTipo.CONVERSACION_CREADA, c));
        return c;
    }

    @Override
    public Optional<Conversacion> buscarConversacionPorId(Long id) {
        return conversacionHandler().buscarConversacionPorId(id);
    }

    @Override
    public Participante agregarParticipante(Long conversacionId, Long usuarioId, String rol) {
        Participante p = participanteHandler().agregarParticipante(conversacionId, usuarioId, rol);
        notificar(new ISistema.Evento(ISistema.EventoTipo.PARTICIPANTE_AGREGADO, p));
        return p;
    }

    @Override
    public void removerParticipante(Long conversacionId, Long usuarioId) {
        List<Participante> removed = participanteHandler().removerParticipante(conversacionId, usuarioId);
        for (Participante p : removed) {
            notificar(new ISistema.Evento(ISistema.EventoTipo.PARTICIPANTE_ELIMINADO, p));
        }
    }

    @Override
    public Mensaje enviarMensaje(Long conversacionId, Long emisorId, String contenido, String tipoMensaje, String urlAdjunto) {
        Mensaje m = mensajeHandler().enviarMensaje(conversacionId, emisorId, contenido, tipoMensaje, urlAdjunto);
        notificar(new ISistema.Evento(ISistema.EventoTipo.MENSAJE_ENVIADO, m));
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
    public void registrarObservador(ISistema.Observador o) {
        if (o != null) observadores.add(o);
    }

    @Override
    public void eliminarObservador(ISistema.Observador o) {
        observadores.remove(o);
    }

    private void notificar(ISistema.Evento evento) {
        for (ISistema.Observador o : observadores) {
            try { o.onEvento(evento); } catch (Exception ignored) { }
        }
    }
}