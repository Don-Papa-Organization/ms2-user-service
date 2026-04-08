package com.users.UsuariosYEmpleados.domain.dto;


public class CrearEmpleadoDTO {
    private String nombre;
    private String documento;
    private String telefono;
    private String cargo;
    private String correo;
    private String contrasena;

    public CrearEmpleadoDTO() {}

    public CrearEmpleadoDTO(String nombre, String documento, String telefono, String cargo,
                            String correo, String contrasena) {
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.cargo = cargo;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}