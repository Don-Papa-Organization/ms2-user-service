package com.users.UsuariosYEmpleados.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(name = "cargo", nullable = false, length = 50)
    private String cargo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "documento", nullable = false, length = 15)
    private String documento;

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;

    // Constructores
    public Empleado() {
    }

    public Empleado(Integer idUsuario, String cargo, String nombre, String documento, String telefono) {
        this.idUsuario = idUsuario;
        this.cargo = cargo;
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
    }

    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}