package chat.Factory;

import chat.Enums.TipoConversacion;
import chat.clases.Conversacion;

public class ConversacionFactory {

    public static Conversacion crearConversacion(String nombre, TipoConversacion tipo) {
        Conversacion conversacion = new Conversacion();
        conversacion.setNombre(nombre);
        conversacion.setTipo(tipo != null ? tipo : TipoConversacion.PRIVADA);
        return conversacion;
    }

    public static Conversacion crearConversacionPrivada(String nombre) {
        return crearConversacion(nombre, TipoConversacion.PRIVADA);
    }

    public static Conversacion crearConversacionGrupo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del grupo no puede estar vacío");
        }
        return crearConversacion(nombre, TipoConversacion.GRUPO);
    }
}
