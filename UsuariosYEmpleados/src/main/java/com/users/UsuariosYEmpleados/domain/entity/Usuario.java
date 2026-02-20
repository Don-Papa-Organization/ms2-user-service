package com.users.UsuariosYEmpleados.domain.entity;

import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;
		
    @Column(name = "correo", nullable = false, unique = true, length = 255)
    private String correo;
    
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoUsuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo;
    
    // Constructores
    public Usuario() {}
    
    public Usuario(String correo, String contrasena, TipoUsuario tipoUsuario, Boolean activo) {
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