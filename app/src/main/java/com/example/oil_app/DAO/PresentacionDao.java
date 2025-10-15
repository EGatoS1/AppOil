package com.example.oil_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.oil_app.ENTITY.PresentacionEntity;

import java.util.List;


@Dao
public interface PresentacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodo(List<PresentacionEntity> presentaciones);

    @Query("DELETE FROM PresentacionEntity")
    void eliminarTodo();

    @Query("SELECT * FROM PresentacionEntity")
    List<PresentacionEntity> obtenerTodos();
}
