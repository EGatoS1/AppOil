package com.example.oil_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oil_app.data.entity.ProductoEntity;
import com.example.oil_app.viewmodel.SeleccionProductoViewModel;

import java.util.ArrayList;
import java.util.List;

public class SeleccionProductoActivity extends AppCompatActivity {

    AutoCompleteTextView autoProducto, autoPresentacion;
    Button btnContinuar;

    private SeleccionProductoViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_producto);

        autoProducto = findViewById(R.id.autoProducto);
        autoPresentacion = findViewById(R.id.autoPresentacion);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Deshabilitar campos hasta que se carguen los datos (igual que antes)
        autoProducto.setEnabled(false);
        autoPresentacion.setEnabled(false);

        viewModel = new ViewModelProvider(this).get(SeleccionProductoViewModel.class);

        // --- Observers: reemplazan a los runOnUiThread(...) de antes ---

        viewModel.getProductos().observe(this, productos -> {
            List<String> nombres = new ArrayList<>();
            for (ProductoEntity p : productos) nombres.add(p.nombre);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, R.layout.item_dropdown_producto, R.id.dropdownText, nombres);
            autoProducto.setAdapter(adapter);
            autoProducto.setThreshold(3);
            autoProducto.setEnabled(true);
        });

        viewModel.getPresentaciones().observe(this, presentaciones -> {
            List<String> nombres = new ArrayList<>();
            for (var p : presentaciones) nombres.add(p.presentacion);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, R.layout.item_dropdown_producto, R.id.dropdownText, nombres);
            autoPresentacion.setAdapter(adapter);
            autoPresentacion.setThreshold(2);
            autoPresentacion.setEnabled(true);
        });

        viewModel.getMensajeError().observe(this, mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show());

        viewModel.getNavegarACostos().observe(this, seleccion -> {
            Intent intent = new Intent(this, RegistrarCostosActivity.class);
            intent.putExtra("productoId", seleccion.productoId);
            intent.putExtra("presentacionId", seleccion.presentacionId);
            intent.putExtra("productoNombre", seleccion.productoNombre);
            intent.putExtra("presentacionNombre", seleccion.presentacionNombre);
            startActivity(intent);
        });

        // --- El listener del botón ahora solo delega al ViewModel ---
        btnContinuar.setOnClickListener(v -> viewModel.onContinuarClic(
                autoProducto.getText().toString(),
                autoPresentacion.getText().toString()
        ));
    }
}
