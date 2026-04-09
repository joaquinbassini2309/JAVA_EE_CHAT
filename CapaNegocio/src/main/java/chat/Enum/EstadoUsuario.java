package chat.Enums;

public enum EstadoUsuario {
    ONLINE("online"),
    OFFLINE("offline"),
    AUSENTE("ausente"),
    OCUPADO("ocupado"),
    NO_MOLESTAR("no_molestar");

    private final String valor;

    EstadoUsuario(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}