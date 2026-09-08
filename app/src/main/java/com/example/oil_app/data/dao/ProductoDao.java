package com.example.oil_app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.oil_app.data.entity.ProductoEntity;

import java.util.List;

@Dao
public interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodo(List<ProductoEntity> productos);

    @Query("DELETE FROM ProductoEntity")
    void eliminarTodo();

    @Query("SELECT * FROM ProductoEntity")
    List<ProductoEntity> obtenerTodos(); // <-- vuelve a agregar este metodo
}


