package com.users.UsuariosYEmpleados.enums;

public enum TipoUsuario {
    cliente("cliente"),
    empleado("empleado"),
    administrador("administrador");

    private final String valor;

    TipoUsuario(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    // Para convertir de String a Enum
    public static TipoUsuario fromString(String texto) {
        for (TipoUsuario tipo : TipoUsuario.values()) {
            if (tipo.valor.equalsIgnoreCase(texto)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de usuario no válido: " + texto);
    }
}