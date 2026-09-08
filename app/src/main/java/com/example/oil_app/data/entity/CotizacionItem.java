package com.example.oil_app.data.entity;

import java.io.Serializable;

public class CotizacionItem implements Serializable {
    String producto;
    String presentacion;
    int cantidad;
    double precioUnitario;
    double precioTotal;

    public CotizacionItem(String producto, String presentacion, int cantidad, double precioUnitario, double precioTotal) {
        this.producto = producto;
        this.presentacion = presentacion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.precioTotal = precioTotal;
    }

    public String getProducto() {
        return producto;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }
}

