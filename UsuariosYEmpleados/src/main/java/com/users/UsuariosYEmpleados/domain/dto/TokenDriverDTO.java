package com.users.UsuariosYEmpleados.domain.dto;

import java.util.Date;

public class TokenDriverDTO {
    private Integer idManejadorTokens;
    private Date creadoEn;
    private Date expiraEn;
    private String token;
    private Integer idUsuario;

    public TokenDriverDTO() {}

    public TokenDriverDTO(Integer idManejadorTokens, Date creadoEn, Date expiraEn,
                          String token, Integer idUsuario) {
        this.idManejadorTokens = idManejadorTokens;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
        this.token = token;
        this.idUsuario = idUsuario;
    }

    public Integer getIdManejadorTokens() { return idManejadorTokens; }
    public void setIdManejadorTokens(Integer idManejadorTokens) { this.idManejadorTokens = idManejadorTokens; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }

    public Date getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Date expiraEn) { this.expiraEn = expiraEn; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}