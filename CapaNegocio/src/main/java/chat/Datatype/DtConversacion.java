package chat.Datatype;

import chat.Enum.TipoConversacion;
import chat.clases.Conversacion;
import chat.clases.Participante;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record DtConversacion(
        Long id,
        String nombre,
        TipoConversacion tipo,
        LocalDateTime fechaCreacion,
        List<Long> participanteIds
) {
    public static DtConversacion from(Conversacion c) {
        if (c == null) return null;
        List<Long> ids = c.getParticipantes() == null ? List.of() :
                c.getParticipantes().stream()
                        .map(Participante::getUsuario)
                        .filter(u -> u != null)
                        .map(u -> u.getId())
                        .collect(Collectors.toList());
        return new DtConversacion(c.getId(), c.getNombre(), c.getTipo(), c.getFechaCreacion(), ids);
    }
}