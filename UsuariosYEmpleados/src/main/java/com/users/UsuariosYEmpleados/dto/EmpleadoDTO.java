package com.users.UsuariosYEmpleados.dto;


public class EmpleadoDTO {
    private Integer idUsuario;
    private String cargo;
    private String nombre;
    private String documento;
    private String telefono;
    private String correo;
    private Boolean activo;

    public EmpleadoDTO() {}

    public EmpleadoDTO(Integer idUsuario, String cargo, String nombre, String documento,
                       String telefono, String correo, Boolean activo) {
        this.idUsuario = idUsuario;
        this.cargo = cargo;
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}