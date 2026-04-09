package chat.Datatype;

import com.example.chat.model.Participante;
import java.time.LocalDateTime;

public record DtParticipante(
        Long id,
        Long conversacionId,
        Long usuarioId,
        String rol,
        LocalDateTime fechaUnion
) {
    public static DtParticipante from(Participante p) {
        if (p == null) return null;
        Long cid = p.getConversacion() != null ? p.getConversacion().getId() : null;
        Long uid = p.getUsuario() != null ? p.getUsuario().getId() : null;
        return new DtParticipante(p.getId(), cid, uid, p.getRol(), p.getFechaUnion());
    }
}