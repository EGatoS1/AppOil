package com.example.oil_app.ENTITY;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PresentacionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String presentacion;
}
