package com.example.oil_app.ENTITY;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"nombre"}, unique = true)})
public class ProductoEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
}
