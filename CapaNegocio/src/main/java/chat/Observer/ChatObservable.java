package chat.Observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatObservable {
    private final List<ChatObserver> observadores = new CopyOnWriteArrayList<>();

    public void registrarObservador(ChatObserver observer) {
        if (observer != null && !observadores.contains(observer)) {
            observadores.add(observer);
        }
    }

    public void eliminarObservador(ChatObserver observer) {
        observadores.remove(observer);
    }

    public void notificar(EventoChat evento) {
        for (ChatObserver observer : observadores) {
            try {
                observer.onEvento(evento);
            } catch (Exception e) {
                // Log error pero continuar notificando a otros observadores
                System.err.println("Error notificando observador: " + e.getMessage());
            }
        }
    }

    public int cantidadObservadores() {
        return observadores.size();
    }

    public void limpiarObservadores() {
        observadores.clear();
    }
}
