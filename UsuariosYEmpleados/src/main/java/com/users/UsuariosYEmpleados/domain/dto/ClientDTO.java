package com.users.UsuariosYEmpleados.domain.dto;


public class ClientDTO {
    private Integer idUsuario;
    private String direccion;
    private String nombre;
    private String telefono;
    private String correo;
    private Boolean activo;

    public ClientDTO() {}

    public ClientDTO(Integer idUsuario, String direccion, String nombre, String telefono,
                     String correo, Boolean activo) {
        this.idUsuario = idUsuario;
        this.direccion = direccion;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}