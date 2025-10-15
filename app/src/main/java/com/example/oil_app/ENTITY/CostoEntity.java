package com.example.oil_app.ENTITY;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CostoEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productoId;
    public int presentacionId;
    public double costo;
    public long fechaRegistro; // Usamos timestamp (System.currentTimeMillis())
    public String moneda;
}
