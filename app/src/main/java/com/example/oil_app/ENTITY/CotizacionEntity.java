package com.example.oil_app.ENTITY;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CotizacionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String producto;
    public String presentacion;
    public int cantidad;
    public double precioUnitario;
    public double precioTotal;
    public String codigoGenerado;

}

