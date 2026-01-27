package com.practica.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String identificacion;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String rol; // ADMIN / USER

    @Column
    private String foto;

    @Column
    private Integer telefono;

    public Usuario() {
    }

    public Usuario(Long id, String identificacion, String nombre, String email, String rol, String foto,
            Integer telefono) {
        this.id = id;
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.foto = foto;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Integer getTelefono() {
        return telefono;
    }

    public void setTelefono(Integer telefono) {
        this.telefono = telefono;
    }
}
