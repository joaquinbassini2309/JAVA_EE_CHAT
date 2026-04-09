package chat.Datatype;

import com.example.chat.model.Conversacion;
import com.example.chat.model.Participante;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record DtConversacion(
        Long id,
        String nombre,
        Boolean esGrupo,
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
        return new DtConversacion(c.getId(), c.getNombre(), c.getEsGrupo(), c.getFechaCreacion(), ids);
    }
}