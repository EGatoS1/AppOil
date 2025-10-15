package com.example.oil_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.oil_app.ENTITY.ProductoEntity;

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


