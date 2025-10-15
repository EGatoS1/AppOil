package com.example.oil_app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.oil_app.DAO.CostoDao;
import com.example.oil_app.DAO.CotizacionDao;
import com.example.oil_app.DAO.PresentacionDao;
import com.example.oil_app.DAO.ProductoDao;
import com.example.oil_app.ENTITY.CostoEntity;
import com.example.oil_app.ENTITY.CotizacionEntity;
import com.example.oil_app.ENTITY.PresentacionEntity;
import com.example.oil_app.ENTITY.ProductoEntity;

@Database(entities = {
        CotizacionEntity.class,
        ProductoEntity.class,
        PresentacionEntity.class,
        CostoEntity.class
}, version = 5) // <-- Incrementa la versión
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract CotizacionDao cotizacionDao();
    public abstract ProductoDao productoDao();
    public abstract PresentacionDao presentacionDao();
    public abstract CostoDao costoDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Solo para pruebas
                    .build();
        }
        return INSTANCE;
    }
}

