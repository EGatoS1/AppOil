package com.example.oil_app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oil_app.data.entity.ProductoEntity;
import com.example.oil_app.viewmodel.CrearCotizacionViewModel;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;

public class CrearCotizacionActivity extends AppCompatActivity {

    AutoCompleteTextView inputProducto, inputPresentacion;
    EditText inputCantidad, inputPrecioUnitario, inputCliente;
    Button btnAgregarProducto, btnGenerarBoleta;
    RecyclerView recyclerProductos;
    Spinner inputVendedor, spinnerMoneda;
    TextView tvTotalCotizacion;
    CotizacionAdapter adapter;

    private CrearCotizacionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cotizacion);

        // El permiso de almacenamiento es responsabilidad de la vista (pide UI del sistema),
        // así que se queda aquí tal cual, no es lógica de negocio.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }

        inputProducto = findViewById(R.id.inputProducto);
        inputPresentacion = findViewById(R.id.inputPresentacion);
        inputCantidad = findViewById(R.id.inputCantidad);
        inputPrecioUnitario = findViewById(R.id.inputPrecioUnitario);
        inputCliente = findViewById(R.id.inputCliente);
        btnAgregarProducto = findViewById(R.id.btnAgregarProducto);
        btnGenerarBoleta = findViewById(R.id.btnGenerarBoleta);
        recyclerProductos = findViewById(R.id.recyclerProductos);
        inputVendedor = findViewById(R.id.inputVendedor);
        spinnerMoneda = findViewById(R.id.spinnerMoneda);
        tvTotalCotizacion = findViewById(R.id.tvTotalCotizacion);

        viewModel = new ViewModelProvider(this).get(CrearCotizacionViewModel.class);

        adapter = new CotizacionAdapter(position -> viewModel.eliminarItem(position));
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProductos.setAdapter(adapter);

        // El spinner de vendedores es una lista estática de UI, no viene de la base de datos,
        // así que se queda tal cual la tenías.
        String[] listaVendedores = {" ", "Elvira Aguirre", "Sara Aguirre", "Celso Gato Lino", "David Gato", "John Gave"};
        ArrayAdapter<String> adapterVendedor = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, listaVendedores) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
        adapterVendedor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputVendedor.setAdapter(adapterVendedor);

        // Nuevo: moneda de la cotización completa. Mismo patrón visual que el spinner de vendedores.
        ArrayAdapter<String> adapterMoneda = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, new String[]{"Soles (S/)", "Dólares ($)"}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
        adapterMoneda.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(adapterMoneda);

        // Nuevo: cuando el usuario cambia de moneda, se actualiza en vivo tanto el
        // símbolo que muestra cada producto ya agregado como la barra de total.
        spinnerMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSimboloMoneda(simboloMonedaActual());
                actualizarTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No aplica: el spinner siempre tiene una opción seleccionada por defecto.
            }
        });

        // --- Observers: reemplazan runOnUiThread(...) y notifyDataSetChanged() manuales ---

        viewModel.getProductos().observe(this, productos -> {
            List<String> nombres = new ArrayList<>();
            for (ProductoEntity p : productos) nombres.add(p.nombre);

            ArrayAdapter<String> adapterProducto = new ArrayAdapter<>(
                    this, R.layout.item_dropdown_producto, R.id.dropdownText, nombres);
            inputProducto.setAdapter(adapterProducto);
            inputProducto.setThreshold(3);
        });

        viewModel.getPresentaciones().observe(this, presentaciones -> {
            List<String> nombres = new ArrayList<>();
            for (var p : presentaciones) nombres.add(p.presentacion);

            ArrayAdapter<String> adapterPresentacion = new ArrayAdapter<>(
                    this, R.layout.item_dropdown_producto, R.id.dropdownText, nombres);
            inputPresentacion.setAdapter(adapterPresentacion);
            inputPresentacion.setThreshold(2);
        });

        viewModel.getItems().observe(this, items -> {
            adapter.actualizarLista(items);
            actualizarTotal();
        });

        viewModel.getMensajeError().observe(this, mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show());

        viewModel.getNavegarABoleta().observe(this, datos -> {
            Intent intent = new Intent(this, BoletaPreviewActivity.class);
            intent.putExtra("nombreCliente", datos.nombreCliente);
            intent.putExtra("nombreVendedor", datos.nombreVendedor);
            intent.putExtra("moneda", datos.moneda);
            intent.putExtra("listaProductos", datos.items);
            startActivity(intent);
        });

        // --- Los listeners de botones ahora solo delegan al ViewModel ---

        btnAgregarProducto.setOnClickListener(v -> viewModel.agregarItem(
                inputProducto.getText().toString().trim(),
                inputPresentacion.getText().toString().trim(),
                inputCantidad.getText().toString().trim(),
                inputPrecioUnitario.getText().toString().trim()
        ));

        btnGenerarBoleta.setOnClickListener(v -> {
            String nombreVendedor = inputVendedor.getSelectedItem() != null
                    ? inputVendedor.getSelectedItem().toString() : "";
            String monedaSeleccionada = spinnerMoneda.getSelectedItem() != null
                    ? spinnerMoneda.getSelectedItem().toString() : "";
            viewModel.onGenerarBoletaClic(inputCliente.getText().toString(), nombreVendedor, monedaSeleccionada);
        });

        // Al agregar un producto con éxito, limpiar los campos (esto es puramente UI).
        // Observamos el trigger dedicado, no getItems(), para que esto NO se dispare
        // también cuando el usuario elimina un producto de la lista.
        viewModel.getItemAgregadoTrigger().observe(this, contador -> {
            inputProducto.setText("");
            inputPresentacion.setText("");
            inputCantidad.setText("");
            inputPrecioUnitario.setText("");
        });
    }

    /** Solo lee lo que el spinner tiene seleccionado ahora mismo; es puramente UI. */
    private String simboloMonedaActual() {
        Object seleccion = spinnerMoneda.getSelectedItem();
        return (seleccion != null && seleccion.toString().contains("S/")) ? "S/" : "$";
    }

    /**
     * Suma los items que ya están en la lista (todavía no se generó la boleta) y
     * actualiza la barra fija de abajo. Puramente presentación: no toca al ViewModel
     * porque el total ya está calculado por item (precioTotal), solo hay que sumarlo.
     */
    private void actualizarTotal() {
        List<com.example.oil_app.data.entity.CotizacionItem> items = viewModel.getItems().getValue();
        double total = 0;
        if (items != null) {
            for (com.example.oil_app.data.entity.CotizacionItem item : items) {
                total += item.getPrecioTotal();
            }
        }
        tvTotalCotizacion.setText(simboloMonedaActual() + " " + String.format(java.util.Locale.US, "%.2f", total));
    }
}
