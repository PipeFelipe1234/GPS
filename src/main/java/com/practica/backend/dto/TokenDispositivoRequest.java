package com.practica.backend.dto;

public class TokenDispositivoRequest {

    private String token;
    private String tipoDispositivo; // Android, iOS, Web
    private String marca; // Samsung, iPhone, etc.
    private String modelo;

    public TokenDispositivoRequest() {
    }

    public TokenDispositivoRequest(String token, String tipoDispositivo, String marca, String modelo) {
        this.token = token;
        this.tipoDispositivo = tipoDispositivo;
        this.marca = marca;
        this.modelo = modelo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoDispositivo() {
        return tipoDispositivo;
    }

    public void setTipoDispositivo(String tipoDispositivo) {
        this.tipoDispositivo = tipoDispositivo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
}
