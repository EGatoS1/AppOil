package com.example.oil_app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.oil_app.data.dao.CostoDao;
import com.example.oil_app.data.dao.CotizacionDao;
import com.example.oil_app.data.dao.PresentacionDao;
import com.example.oil_app.data.dao.ProductoDao;
import com.example.oil_app.data.entity.CostoEntity;
import com.example.oil_app.data.entity.CotizacionEntity;
import com.example.oil_app.data.entity.PresentacionEntity;
import com.example.oil_app.data.entity.ProductoEntity;

@Database(entities = {
        CotizacionEntity.class,
        ProductoEntity.class,
        PresentacionEntity.class,
        CostoEntity.class
}, version = 7, exportSchema = true)
public abstract class   AppDatabase extends RoomDatabase {

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
                    .addMigrations(
                            Migrations.MIGRATION_5_6,
                            Migrations.MIGRATION_6_7
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onOpen(@androidx.annotation.NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            db.execSQL("PRAGMA foreign_keys=ON");
                        }
                    })
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build();
        }
        return INSTANCE;
    }
}
