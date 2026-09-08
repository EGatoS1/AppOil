package com.example.oil_app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"presentacion"}, unique = true)})
public class PresentacionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String presentacion;
}
