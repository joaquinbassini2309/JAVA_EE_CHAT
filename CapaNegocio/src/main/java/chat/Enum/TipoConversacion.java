package chat.enums;

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