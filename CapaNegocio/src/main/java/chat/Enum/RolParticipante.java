package chat.Enums;

public enum RolParticipante {
    ADMIN("admin"),
    MIEMBRO("miembro"),
    MODERADOR("moderador"),
    SILENCIADO("silenciado");

    private final String valor;

    RolParticipante(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}