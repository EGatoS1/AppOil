package com.example.oil_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oil_app.ENTITY.CostoEntity;

import java.util.*;

public class RegistrarCostosActivity extends AppCompatActivity {

    TextView textTitulo;
    EditText editCosto;
    Button btnRegistrar;
    RecyclerView recyclerCostos;

    AppDatabase db;
    CostoAdapter adapter;
    List<CostoEntity> listaCostos = new ArrayList<>();

    int productoId;
    int presentacionId;
    String productoNombre;
    String presentacionNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_costos);

        // Obtener datos desde el intent
        Intent intent = getIntent();
        productoId = intent.getIntExtra("productoId", -1);
        presentacionId = intent.getIntExtra("presentacionId", -1);
        productoNombre = intent.getStringExtra("productoNombre");
        presentacionNombre = intent.getStringExtra("presentacionNombre");

        // Inicializar vistas
        textTitulo = findViewById(R.id.textTitulo);
        editCosto = findViewById(R.id.editCosto);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        recyclerCostos = findViewById(R.id.recyclerCostos);

        Spinner spinnerMoneda = findViewById(R.id.spinnerMoneda);

        ArrayAdapter<String> monedaAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Soles (S/)", "Dólares ($)"}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // Color del texto visible
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // Color de los ítems desplegables
                return view;
            }
        };

        monedaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(monedaAdapter);


        // Mostrar el título con los nombres seleccionados
        textTitulo.setText(productoNombre + " - " + presentacionNombre);

        db = AppDatabase.getInstance(this);

        configurarRecyclerView();
        eliminarRegistrosAntiguos();
        cargarCostos();

        btnRegistrar.setOnClickListener(v -> registrarCosto());
    }

    void registrarCosto() {
        String costoStr = editCosto.getText().toString();
        if (costoStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el costo", Toast.LENGTH_SHORT).show();
            return;
        }

        Spinner spinnerMoneda = findViewById(R.id.spinnerMoneda);
        String monedaSeleccionada = spinnerMoneda.getSelectedItem().toString();

        String simboloMoneda = monedaSeleccionada.contains("S/") ? "S/" : "$";

        CostoEntity costo = new CostoEntity();
        costo.productoId = productoId;
        costo.presentacionId = presentacionId;
        costo.costo = Double.parseDouble(costoStr);
        costo.fechaRegistro = System.currentTimeMillis();
        costo.moneda = simboloMoneda;

        db.costoDao().insertar(costo);
        editCosto.setText("");
        cargarCostos();
    }

    void configurarRecyclerView() {
        adapter = new CostoAdapter(listaCostos, this::confirmarEliminacion);
        recyclerCostos.setLayoutManager(new LinearLayoutManager(this));
        recyclerCostos.setAdapter(adapter);
    }

    void cargarCostos() {
        long haceDosAnios = System.currentTimeMillis() - (2L * 365 * 24 * 60 * 60 * 1000);
        listaCostos.clear();
        listaCostos.addAll(db.costoDao().obtenerRecientesPorProductoPresentacion(haceDosAnios, productoId, presentacionId));
        adapter.notifyDataSetChanged();
    }

    void eliminarRegistrosAntiguos() {
        long haceDosAnios = System.currentTimeMillis() - (2L * 365 * 24 * 60 * 60 * 1000);
        db.costoDao().eliminarAntiguos(haceDosAnios);
    }

    void confirmarEliminacion(CostoEntity costo) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Costo")
                .setMessage("¿Estás seguro de eliminar este registro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.costoDao().eliminarPorId(costo.id);
                    cargarCostos();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
