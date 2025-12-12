package com.users.UsuariosYEmpleados.dto;

import com.users.UsuariosYEmpleados.enums.TipoUsuario;


public class UserDTO {
    private Integer idUsuario;
    private String correo;
    private String contrasena;
    private TipoUsuario tipoUsuario;
    private Boolean activo;
    
    // Constructor vacío
    public UserDTO() {
    }
    
    // Constructor con todos los campos
    public UserDTO(Integer idUsuario, String correo, String contrasena, TipoUsuario tipoUsuario, Boolean activo) {
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.contrasena = contrasena;
        this.tipoUsuario = tipoUsuario;
        this.activo = activo;
    }
    
    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getCorreo() {
        return correo;
    }
    
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
    public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}