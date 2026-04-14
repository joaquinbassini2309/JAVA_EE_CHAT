package chat.Datatype;

import chat.Enum.TipoMensaje;
import chat.clases.Mensaje;
import java.time.LocalDateTime;

public record DtMensaje(
        Long id,
        Long conversacionId,
        Long emisorId,
        String contenido,
        TipoMensaje tipoMensaje,
        String urlAdjunto,
        Boolean leido,
        LocalDateTime fechaEnvio
) {
    public static DtMensaje from(Mensaje m) {
        if (m == null) return null;
        Long cid = m.getConversacion() != null ? m.getConversacion().getId() : null;
        Long eid = m.getEmisor() != null ? m.getEmisor().getId() : null;
        return new DtMensaje(
                m.getId(),
                cid,
                eid,
                m.getContenido(),
                m.getTipoMensaje(),
                m.getUrlAdjunto(),
                m.getLeido(),
                m.getFechaEnvio()
        );
    }
}