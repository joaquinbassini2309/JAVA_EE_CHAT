package chat.Enum;

public enum TipoConversacion {
    PRIVADA("privada"),
    GRUPO("grupo");

    private final String valor;

    TipoConversacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}