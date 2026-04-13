package chat.Observer;

import chat.Enums.EventoTipo;

public class EventoChat {
    private final EventoTipo tipo;
    private final Object payload;

    public EventoChat(EventoTipo tipo, Object payload) {
        this.tipo = tipo;
        this.payload = payload;
    }

    public EventoTipo getTipo() {
        return tipo;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "EventoChat{" +
                "tipo=" + tipo +
                ", payload=" + payload +
                '}';
    }
}
