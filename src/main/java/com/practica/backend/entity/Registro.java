package com.practica.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "registros")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    // 📍 GPS ENTRADA (Check-in)
    private Double latitudCheckin;
    private Double longitudCheckin;
    private Double precisionMetrosCheckin;

    // 📍 GPS SALIDA (Check-out)
    private Double latitudCheckout;
    private Double longitudCheckout;
    private Double precisionMetrosCheckout;

    // 📝 Reporte del día
    @Column(columnDefinition = "TEXT")
    private String reporte;

    // 📷 Foto/URL de imagen
    private String picture;

    public Registro() {
    }

    public Registro(Long id, Usuario usuario, LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida,
            Double latitudCheckin, Double longitudCheckin, Double precisionMetrosCheckin,
            Double latitudCheckout, Double longitudCheckout, Double precisionMetrosCheckout,
            String reporte, String picture) {
        this.id = id;
        this.usuario = usuario;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.latitudCheckin = latitudCheckin;
        this.longitudCheckin = longitudCheckin;
        this.precisionMetrosCheckin = precisionMetrosCheckin;
        this.latitudCheckout = latitudCheckout;
        this.longitudCheckout = longitudCheckout;
        this.precisionMetrosCheckout = precisionMetrosCheckout;
        this.reporte = reporte;
        this.picture = picture;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Double getLatitudCheckin() {
        return latitudCheckin;
    }

    public void setLatitudCheckin(Double latitudCheckin) {
        this.latitudCheckin = latitudCheckin;
    }

    public Double getLongitudCheckin() {
        return longitudCheckin;
    }

    public void setLongitudCheckin(Double longitudCheckin) {
        this.longitudCheckin = longitudCheckin;
    }

    public Double getPrecisionMetrosCheckin() {
        return precisionMetrosCheckin;
    }

    public void setPrecisionMetrosCheckin(Double precisionMetrosCheckin) {
        this.precisionMetrosCheckin = precisionMetrosCheckin;
    }

    public Double getLatitudCheckout() {
        return latitudCheckout;
    }

    public void setLatitudCheckout(Double latitudCheckout) {
        this.latitudCheckout = latitudCheckout;
    }

    public Double getLongitudCheckout() {
        return longitudCheckout;
    }

    public void setLongitudCheckout(Double longitudCheckout) {
        this.longitudCheckout = longitudCheckout;
    }

    public Double getPrecisionMetrosCheckout() {
        return precisionMetrosCheckout;
    }

    public void setPrecisionMetrosCheckout(Double precisionMetrosCheckout) {
        this.precisionMetrosCheckout = precisionMetrosCheckout;
    }

    public String getReporte() {
        return reporte;
    }

    public void setReporte(String reporte) {
        this.reporte = reporte;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
