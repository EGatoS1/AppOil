package com.example.oil_app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;

import com.example.oil_app.ENTITY.CotizacionItem;
import com.example.oil_app.ENTITY.PresentacionEntity;
import com.example.oil_app.ENTITY.ProductoEntity;

public class CrearCotizacionActivity extends AppCompatActivity {

    AutoCompleteTextView inputProducto, inputPresentacion;
    EditText inputCantidad, inputPrecioUnitario;
    Button btnAgregarProducto, btnGenerarBoleta;
    RecyclerView recyclerProductos;
    Spinner inputVendedor;
    ArrayList<CotizacionItem> listaProductos = new ArrayList<>();
    CotizacionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cotizacion);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // API 28 y versiones anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        cargarProductosDesdeCSV();
        cargarPresentacionesDesdeCSV();
        // Vistas
        inputProducto = findViewById(R.id.inputProducto);
        inputPresentacion = findViewById(R.id.inputPresentacion);
        inputCantidad = findViewById(R.id.inputCantidad);
        inputPrecioUnitario = findViewById(R.id.inputPrecioUnitario);
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        btnGenerarBoleta = findViewById(R.id.btnGenerarBoleta);
        recyclerProductos = findViewById(R.id.recyclerProductos);
        inputVendedor = findViewById(R.id.inputVendedor);

        // Cargar productos y presentaciones desde la base de datos
        cargarProductosDesdeBaseDeDatos();
        cargarPresentacionesDesdeBaseDeDatos();

        // RecyclerView
        adapter = new CotizacionAdapter(listaProductos);
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProductos.setAdapter(adapter);

        String[] listaVendedores = {" ", "Elvira Aguirre", "Sara Aguirre", "Celso Gato Lino", "David Gato", "John Gave"};
        ArrayAdapter<String> adapterVendedor = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                listaVendedores
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);  // Color del texto visible
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);  // Color del dropdown
                return view;
            }
        };

        adapterVendedor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputVendedor.setAdapter(adapterVendedor);


        // Botón agregar
        btnAgregarProducto.setOnClickListener(v -> {
            String producto = inputProducto.getText().toString().trim();
            String presentacion = inputPresentacion.getText().toString().trim();
            String cantidadStr = inputCantidad.getText().toString().trim();
            String precioUnitarioStr = inputPrecioUnitario.getText().toString().trim();

            if (producto.isEmpty() || presentacion.isEmpty() || cantidadStr.isEmpty() || precioUnitarioStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                double precioUnitario = Double.parseDouble(precioUnitarioStr);
                double precioTotal = cantidad * precioUnitario;

                CotizacionItem item = new CotizacionItem(producto, presentacion, cantidad, precioUnitario, precioTotal);
                listaProductos.add(item);
                adapter.notifyDataSetChanged();

                inputProducto.setText("");
                inputPresentacion.setText("");
                inputCantidad.setText("");
                inputPrecioUnitario.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad o precio inválido", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón generar boleta
        btnGenerarBoleta.setOnClickListener(v -> {
            String nombreCliente = ((EditText) findViewById(R.id.inputCliente)).getText().toString().trim();

            if (nombreCliente.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el nombre del cliente", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombreVendedor = inputVendedor.getSelectedItem().toString();
            if (nombreVendedor.trim().isEmpty()) {
                Toast.makeText(this, "Por favor selecciona un vendedor", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, BoletaPreviewActivity.class);
            intent.putExtra("nombreCliente", nombreCliente);
            intent.putExtra("nombreVendedor", nombreVendedor);
            intent.putExtra("listaProductos", listaProductos); // Tu CotizacionItem debe implementar Serializable o Parcelable
            startActivity(intent);
        });

    }
    private void cargarProductosDesdeBaseDeDatos() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<ProductoEntity> productosDb = db.productoDao().obtenerTodos();

                List<String> nombres = new ArrayList<>();
                for (ProductoEntity producto : productosDb) {
                    nombres.add(producto.nombre);
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapterProducto = new ArrayAdapter<>(
                            CrearCotizacionActivity.this,
                            R.layout.item_dropdown_producto, // Layout personalizado para los ítems
                            R.id.dropdownText,
                            nombres);
                    inputProducto.setAdapter(adapterProducto);
                    inputProducto.setThreshold(3);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void cargarProductosDesdeCSV() {
        new Thread(() -> {
            // Verificar si ya hay productos en la base de datos
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<ProductoEntity> productosDb = db.productoDao().obtenerTodos();

            // Si ya hay productos, no cargues el CSV nuevamente
            if (productosDb != null && !productosDb.isEmpty()) {

                return; // Sale del metodo si ya hay productos en la base de datos
            }
            try {
                InputStream is = getAssets().open("productos.csv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                List<ProductoEntity> listaProductos = new ArrayList<>();

                // Opcional: salta encabezado si lo hay
                reader.readLine(); // Quitar esta línea si tu CSV no tiene encabezado

                while ((line = reader.readLine()) != null) {
                    String[] partes = line.split(";");

                    // Verificar si hay más de un campo y limpiar el nombre del producto
                    if (partes.length > 0) {
                        String nombreProducto = partes[0].trim(); // Elimina los espacios al principio y al final

                        // Elimina los puntos y comas al final del nombre, si los hay
                        nombreProducto = nombreProducto.replaceAll(";+\\s*$", "");

                        if (!nombreProducto.isEmpty()) {
                            ProductoEntity producto = new ProductoEntity();
                            producto.nombre = nombreProducto;
                            listaProductos.add(producto);
                        }
                    }
                }

                // Insertar todos los productos en la base de datos
                db.productoDao().insertarTodo(listaProductos);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Carga completada: " + listaProductos.size() + " productos", Toast.LENGTH_LONG).show();
                    cargarProductosDesdeBaseDeDatos(); // Actualiza autocompletado si es necesario
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al leer CSV", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    private void cargarPresentacionesDesdeBaseDeDatos() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<PresentacionEntity> presentacionesDb = db.presentacionDao().obtenerTodos();

                List<String> nombres = new ArrayList<>();
                for (PresentacionEntity presentacion : presentacionesDb) {
                    nombres.add(presentacion.presentacion);
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapterPresentacion = new ArrayAdapter<>(
                            CrearCotizacionActivity.this,
                            R.layout.item_dropdown_producto, // Layout personalizado
                            R.id.dropdownText, // ID del TextView en el layout
                            nombres);
                    inputPresentacion.setAdapter(adapterPresentacion);
                    inputPresentacion.setThreshold(2);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(CrearCotizacionActivity.this, "Error al cargar presentaciones", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    private void cargarPresentacionesDesdeCSV() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<PresentacionEntity> presentacionesDb = db.presentacionDao().obtenerTodos();

            // Si ya hay presentaciones, no cargues el CSV nuevamente
            if (presentacionesDb != null && !presentacionesDb.isEmpty()) {

                return;
            }

            try {
                InputStream is = getAssets().open("presentaciones.csv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                List<PresentacionEntity> listaPresentaciones = new ArrayList<>();

                // Salta el encabezado si lo hay
                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] partes = line.split(";");

                    if (partes.length > 0) {
                        String nombrePresentacion = partes[0].trim();
                        nombrePresentacion = nombrePresentacion.replaceAll(";+\\s*$", "");

                        if (!nombrePresentacion.isEmpty()) {
                            PresentacionEntity presentacion = new PresentacionEntity();
                            presentacion.presentacion = nombrePresentacion;
                            listaPresentaciones.add(presentacion);
                        }
                    }
                }

                db.presentacionDao().insertarTodo(listaPresentaciones);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Carga completada: " + listaPresentaciones.size() + " presentaciones", Toast.LENGTH_LONG).show();
                    cargarPresentacionesDesdeBaseDeDatos(); // Actualiza el autocompletado
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al leer CSV de presentaciones", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}