package chat.Enum;

public enum EventoTipo {
    // Eventos de Usuario
    USUARIO_CREADO,
    USUARIO_ACTUALIZADO,
    ESTADO_USUARIO_CAMBIADO,

    // Eventos de Conversación
    CONVERSACION_CREADA,
    CONVERSACION_ACTUALIZADA,

    // Eventos de Participante
    PARTICIPANTE_AGREGADO,
    PARTICIPANTE_ELIMINADO,
    ROL_PARTICIPANTE_CAMBIADO,

    // Eventos de Mensajería
    MENSAJE_ENVIADO,
    MENSAJE_LEIDO,
    MENSAJE_ELIMINADO
}