package com.example.oil_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oil_app.ENTITY.PresentacionEntity;
import com.example.oil_app.ENTITY.ProductoEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeleccionProductoActivity extends AppCompatActivity {

    AutoCompleteTextView autoProducto, autoPresentacion;
    Button btnContinuar;

    AppDatabase db;
    Map<String, Integer> productoMap = new HashMap<>();
    Map<String, Integer> presentacionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_producto);

        autoProducto = findViewById(R.id.autoProducto);
        autoPresentacion = findViewById(R.id.autoPresentacion);
        btnContinuar = findViewById(R.id.btnContinuar);

        db = AppDatabase.getInstance(this);

        // Deshabilitar campos hasta que se carguen los datos
        autoProducto.setEnabled(false);
        autoPresentacion.setEnabled(false);

        cargarProductosDesdeBaseDeDatos();
        cargarPresentacionesDesdeBaseDeDatos();

        btnContinuar.setOnClickListener(v -> {
            String nombreProd = autoProducto.getText().toString().trim();
            String nombrePres = autoPresentacion.getText().toString().trim();

            if (nombreProd.isEmpty() || nombrePres.isEmpty()) {
                Toast.makeText(this, "Selecciona un producto y una presentación", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!productoMap.containsKey(nombreProd)) {
                Toast.makeText(this, "Producto no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!presentacionMap.containsKey(nombrePres)) {
                Toast.makeText(this, "Presentación no válida", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, RegistrarCostosActivity.class);
            intent.putExtra("productoId", productoMap.get(nombreProd));
            intent.putExtra("presentacionId", presentacionMap.get(nombrePres));
            intent.putExtra("productoNombre", nombreProd);
            intent.putExtra("presentacionNombre", nombrePres);
            startActivity(intent);
        });
    }

    private void cargarPresentacionesDesdeBaseDeDatos() {
        new Thread(() -> {
            try {
                List<PresentacionEntity> presentacionesDb = db.presentacionDao().obtenerTodos();
                List<String> nombresPresentaciones = new ArrayList<>();
                presentacionMap.clear();

                for (PresentacionEntity presentacion : presentacionesDb) {
                    nombresPresentaciones.add(presentacion.presentacion);
                    presentacionMap.put(presentacion.presentacion, presentacion.id);
                }

                Log.d("Presentaciones", nombresPresentaciones.toString());

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            R.layout.item_dropdown_producto,
                            R.id.dropdownText,
                            nombresPresentaciones
                    );
                    autoPresentacion.setAdapter(adapter);
                    autoPresentacion.setThreshold(2);
                    autoPresentacion.setEnabled(true);  // Habilitar después de cargar
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarProductosDesdeBaseDeDatos() {
        new Thread(() -> {
            try {
                List<ProductoEntity> productosDb = db.productoDao().obtenerTodos();
                List<String> nombresProductos = new ArrayList<>();
                productoMap.clear();

                for (ProductoEntity producto : productosDb) {
                    nombresProductos.add(producto.nombre);
                    productoMap.put(producto.nombre, producto.id);
                }

                Log.d("Productos", nombresProductos.toString());

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            R.layout.item_dropdown_producto,
                            R.id.dropdownText,
                            nombresProductos
                    );
                    autoProducto.setAdapter(adapter);
                    autoProducto.setThreshold(3);
                    autoProducto.setEnabled(true);  // Habilitar después de cargar
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}


