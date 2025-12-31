package main.java.com.users.UsuariosYEmpleados.dto;

import com.users.UsuariosYEmpleados.enums.TipoUsuario;

public class UserResponseDTO {
    private Integer idUsuario;
    private String correo;
    private TipoUsuario tipoUsuario;
    private Boolean activo;
    
    public UserResponseDTO() {
    }
    
    public UserResponseDTO(Integer idUsuario, String correo, TipoUsuario tipoUsuario, Boolean activo) {
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
        this.activo = activo;
    }
    
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
