package com.example.oil_app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        indices = {
                @Index("productoId"),
                @Index("presentacionId")
        },
        foreignKeys = {
                // RESTRICT: la base de datos va a IMPEDIR borrar un producto/presentación
                // que ya tenga costos registrados. Cuando armes la función de eliminar,
                // ese intento va a lanzar una excepción en vez de dejar costos huérfanos.
                @ForeignKey(
                        entity = ProductoEntity.class,
                        parentColumns = "id",
                        childColumns = "productoId",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = PresentacionEntity.class,
                        parentColumns = "id",
                        childColumns = "presentacionId",
                        onDelete = ForeignKey.RESTRICT
                )
        }
)
public class CostoEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productoId;
    public int presentacionId;
    public double costo;
    public long fechaRegistro; // Usamos timestamp (System.currentTimeMillis())

    @NonNull
    public String moneda;
}
