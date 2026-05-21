package chat.Enum;

public enum TipoConversacion {
    PRIVADA("privada"),
    GRUPO("grupo"),
    AVISO("aviso");

    private final String valor;

    TipoConversacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}