package com.users.UsuariosYEmpleados.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    // Constructores
    public Cliente() {
    }

    public Cliente(Integer idUsuario, String direccion, String nombre, String telefono) {
        this.idUsuario = idUsuario;
        this.direccion = direccion;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}