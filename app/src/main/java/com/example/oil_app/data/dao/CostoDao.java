package com.example.oil_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.oil_app.data.entity.CostoEntity;

import java.util.List;

@Dao
public interface CostoDao {
    @Insert
    void insertar(CostoEntity costo);

    @Query("SELECT * FROM CostoEntity WHERE fechaRegistro >= :limite ORDER BY fechaRegistro DESC")
    List<CostoEntity> obtenerRecientes(long limite);

    @Query("DELETE FROM CostoEntity WHERE id = :id")
    void eliminarPorId(int id);

    @Query("DELETE FROM CostoEntity WHERE fechaRegistro < :limite")
    void eliminarAntiguos(long limite);
    @Query("SELECT * FROM CostoEntity WHERE fechaRegistro >= :limite AND productoId = :productoId AND presentacionId = :presentacionId ORDER BY fechaRegistro DESC")
    List<CostoEntity> obtenerRecientesPorProductoPresentacion(long limite, int productoId, int presentacionId);
}