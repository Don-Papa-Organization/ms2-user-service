package com.users.UsuariosYEmpleados.domain.entity;

import jakarta.persistence.*;
import java.util.Date;


@Entity
@Table(name = "manejadorTokens")
public class ManejadorTokens {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idManejadorTokens")
    private Integer idManejadorTokens;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creadoEn", nullable = false)
    private Date creadoEn = new Date();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiraEn", nullable = false)
    private Date expiraEn;
    
    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;
    
    @Column(name = "idUsuario", nullable = false)
    private Integer idUsuario;
    
    // Constructores
    public ManejadorTokens() {}
    
    public ManejadorTokens(Date expiraEn, String token, Integer idUsuario) {
        this.expiraEn = expiraEn;
        this.token = token;
        this.idUsuario = idUsuario;
    }
    
    // Getters y Setters
    public Integer getIdManejadorTokens() {
        return idManejadorTokens;
    }
    
    public void setIdManejadorTokens(Integer idManejadorTokens) {
        this.idManejadorTokens = idManejadorTokens;
    }
    
    public Date getCreadoEn() {
        return creadoEn;
    }
    
    public void setCreadoEn(Date creadoEn) {
        this.creadoEn = creadoEn;
    }
    
    public Date getExpiraEn() {
        return expiraEn;
    }
    
    public void setExpiraEn(Date expiraEn) {
        this.expiraEn = expiraEn;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}