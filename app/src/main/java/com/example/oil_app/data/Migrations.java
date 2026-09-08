package com.example.oil_app.data;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Todas las migraciones reales del proyecto viven aquí, una constante por salto de versión.
 * Nunca se edita una migración ya publicada: si algo quedó mal, se corrige con una
 * migración NUEVA (ej. 7->8), nunca modificando esta clase después de que esa versión
 * ya esté en manos de usuarios reales.
 */
public class Migrations {

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL(
                    "UPDATE CostoEntity " +
                    "SET presentacionId = (" +
                    "   SELECT MIN(p2.id) FROM PresentacionEntity p2 " +
                    "   WHERE p2.presentacion = (" +
                    "       SELECT p1.presentacion FROM PresentacionEntity p1 WHERE p1.id = CostoEntity.presentacionId" +
                    "   )" +
                    ") " +
                    "WHERE presentacionId IN (" +
                    "   SELECT id FROM PresentacionEntity" +
                    "   WHERE id NOT IN (SELECT MIN(id) FROM PresentacionEntity GROUP BY presentacion)" +
                    ")"
            );

            db.execSQL(
                    "DELETE FROM PresentacionEntity " +
                    "WHERE id NOT IN (SELECT MIN(id) FROM PresentacionEntity GROUP BY presentacion)"
            );

            db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_PresentacionEntity_presentacion " +
                    "ON PresentacionEntity(presentacion)"
            );

            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_CostoEntity_productoId ON CostoEntity(productoId)"
            );
            db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_CostoEntity_presentacionId ON CostoEntity(presentacionId)"
            );
        }
    };

    /**
     * Agrega NOT NULL real (a nivel de columna SQLite, no solo en el código Java) a
     * ProductoEntity.nombre, PresentacionEntity.presentacion y CostoEntity.moneda,
     * y agrega Foreign Keys reales de CostoEntity hacia ProductoEntity/PresentacionEntity.
     *
     * SQLite no permite "ALTER TABLE ... ADD CONSTRAINT" ni "ALTER COLUMN", así que
     * para cada tabla: se crea una tabla nueva con la estructura final, se copian los
     * datos, se borra la vieja, y se renombra la nueva. Es el mismo patrón que usa
     * Room internamente cuando necesita este tipo de cambio.
     */
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL("PRAGMA foreign_keys=OFF");

            // ---- ProductoEntity: nombre pasa a NOT NULL ----
            db.execSQL("CREATE TABLE IF NOT EXISTS ProductoEntity_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "nombre TEXT NOT NULL)");
            // COALESCE es una salvaguarda: si por algún motivo ya existiera un nombre
            // nulo en la base de datos actual, no queremos que la migración falle,
            // preferimos dejar un valor visible y detectable en vez de crashear.
            db.execSQL("INSERT INTO ProductoEntity_new (id, nombre) " +
                    "SELECT id, COALESCE(nombre, '(sin nombre)') FROM ProductoEntity");
            db.execSQL("DROP TABLE ProductoEntity");
            db.execSQL("ALTER TABLE ProductoEntity_new RENAME TO ProductoEntity");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_ProductoEntity_nombre ON ProductoEntity(nombre)");

            // ---- PresentacionEntity: presentacion pasa a NOT NULL ----
            db.execSQL("CREATE TABLE IF NOT EXISTS PresentacionEntity_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "presentacion TEXT NOT NULL)");
            db.execSQL("INSERT INTO PresentacionEntity_new (id, presentacion) " +
                    "SELECT id, COALESCE(presentacion, '(sin nombre)') FROM PresentacionEntity");
            db.execSQL("DROP TABLE PresentacionEntity");
            db.execSQL("ALTER TABLE PresentacionEntity_new RENAME TO PresentacionEntity");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_PresentacionEntity_presentacion ON PresentacionEntity(presentacion)");

            // ---- CostoEntity: moneda pasa a NOT NULL + Foreign Keys reales ----
            db.execSQL("CREATE TABLE IF NOT EXISTS CostoEntity_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "productoId INTEGER NOT NULL, " +
                    "presentacionId INTEGER NOT NULL, " +
                    "costo REAL NOT NULL, " +
                    "fechaRegistro INTEGER NOT NULL, " +
                    "moneda TEXT NOT NULL, " +
                    "FOREIGN KEY(productoId) REFERENCES ProductoEntity(id) ON DELETE RESTRICT, " +
                    "FOREIGN KEY(presentacionId) REFERENCES PresentacionEntity(id) ON DELETE RESTRICT)");
            db.execSQL("INSERT INTO CostoEntity_new (id, productoId, presentacionId, costo, fechaRegistro, moneda) " +
                    "SELECT id, productoId, presentacionId, costo, fechaRegistro, COALESCE(moneda, 'S/') FROM CostoEntity");
            db.execSQL("DROP TABLE CostoEntity");
            db.execSQL("ALTER TABLE CostoEntity_new RENAME TO CostoEntity");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_CostoEntity_productoId ON CostoEntity(productoId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_CostoEntity_presentacionId ON CostoEntity(presentacionId)");

            db.execSQL("PRAGMA foreign_keys=ON");
        }
    };
}
