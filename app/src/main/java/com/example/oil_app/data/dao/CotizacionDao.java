package com.example.oil_app.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.oil_app.data.entity.CotizacionEntity;

import java.util.List;

@Dao
public interface CotizacionDao {
    @Insert
    void insertar(CotizacionEntity item);
    @Query("SELECT * FROM CotizacionEntity")
    List<CotizacionEntity> obtenerTodo();
    @Delete
    void eliminar(CotizacionEntity item);
}
