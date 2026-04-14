package chat.Factory;

import chat.Enum.TipoMensaje;
import chat.clases.Conversacion;
import chat.clases.Mensaje;
import chat.clases.Usuario;

public class MensajeFactory {

    public static Mensaje crearMensaje(Conversacion conversacion, Usuario emisor, String contenido, TipoMensaje tipo, String urlAdjunto) {
        if (conversacion == null) {
            throw new IllegalArgumentException("La conversación no puede ser nula");
        }
        if (emisor == null) {
            throw new IllegalArgumentException("El emisor no puede ser nulo");
        }
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío");
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setConversacion(conversacion);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(contenido);
        mensaje.setTipoMensaje(tipo != null ? tipo : TipoMensaje.TEXTO);
        mensaje.setUrlAdjunto(urlAdjunto);
        mensaje.setLeido(false);
        return mensaje;
    }

    public static Mensaje crearMensajeTexto(Conversacion conversacion, Usuario emisor, String contenido) {
        return crearMensaje(conversacion, emisor, contenido, TipoMensaje.TEXTO, null);
    }

    public static Mensaje crearMensajeImagen(Conversacion conversacion, Usuario emisor, String contenido, String urlImagen) {
        return crearMensaje(conversacion, emisor, contenido, TipoMensaje.IMAGEN, urlImagen);
    }

    public static Mensaje crearMensajeAudio(Conversacion conversacion, Usuario emisor, String contenido, String urlAudio) {
        return crearMensaje(conversacion, emisor, contenido, TipoMensaje.AUDIO, urlAudio);
    }

    public static Mensaje crearMensajeVideo(Conversacion conversacion, Usuario emisor, String contenido, String urlVideo) {
        return crearMensaje(conversacion, emisor, contenido, TipoMensaje.VIDEO, urlVideo);
    }

    public static Mensaje crearMensajeDocumento(Conversacion conversacion, Usuario emisor, String contenido, String urlDocumento) {
        return crearMensaje(conversacion, emisor, contenido, TipoMensaje.DOCUMENTO, urlDocumento);
    }
}
