package chat.Factory;

import chat.Enums.RolParticipante;
import chat.clases.Conversacion;
import chat.clases.Participante;
import chat.clases.Usuario;

public class ParticipanteFactory {

    public static Participante crearParticipante(Conversacion conversacion, Usuario usuario, RolParticipante rol) {
        if (conversacion == null) {
            throw new IllegalArgumentException("La conversación no puede ser nula");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        Participante participante = new Participante();
        participante.setConversacion(conversacion);
        participante.setUsuario(usuario);
        participante.setRol(rol != null ? rol : RolParticipante.MIEMBRO);
        return participante;
    }

    public static Participante crearParticipanteMiembro(Conversacion conversacion, Usuario usuario) {
        return crearParticipante(conversacion, usuario, RolParticipante.MIEMBRO);
    }

    public static Participante crearParticipanteAdmin(Conversacion conversacion, Usuario usuario) {
        return crearParticipante(conversacion, usuario, RolParticipante.ADMIN);
    }

    public static Participante crearParticipanteModerador(Conversacion conversacion, Usuario usuario) {
        return crearParticipante(conversacion, usuario, RolParticipante.MODERADOR);
    }
}
