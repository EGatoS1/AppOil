package com.example.oil_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oil_app.viewmodel.RegistrarCostosViewModel;

public class RegistrarCostosActivity extends AppCompatActivity {

    TextView textTitulo;
    EditText editCosto;
    Button btnRegistrar;
    RecyclerView recyclerCostos;
    Spinner spinnerMoneda;
    CostoAdapter adapter;

    private RegistrarCostosViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_costos);

        // Leer los extras del Intent sí es trabajo de la Activity.
        int productoId = getIntent().getIntExtra("productoId", -1);
        int presentacionId = getIntent().getIntExtra("presentacionId", -1);
        String productoNombre = getIntent().getStringExtra("productoNombre");
        String presentacionNombre = getIntent().getStringExtra("presentacionNombre");

        textTitulo = findViewById(R.id.textTitulo);
        editCosto = findViewById(R.id.editCosto);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        recyclerCostos = findViewById(R.id.recyclerCostos);
        spinnerMoneda = findViewById(R.id.spinnerMoneda);

        textTitulo.setText(productoNombre + " - " + presentacionNombre);

        // Spinner de monedas: lista fija de UI, se queda igual que antes.
        ArrayAdapter<String> monedaAdapter = new ArrayAdapter<String>(
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
        monedaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(monedaAdapter);

        adapter = new CostoAdapter(costo -> viewModel.onEliminarClic(costo));
        recyclerCostos.setLayoutManager(new LinearLayoutManager(this));
        recyclerCostos.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RegistrarCostosViewModel.class);
        viewModel.inicializar(productoId, presentacionId);

        // --- Observers ---
        viewModel.getCostos().observe(this, costos -> adapter.actualizarLista(costos));

        viewModel.getMensajeError().observe(this, mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show());

        // El diálogo de confirmación SÍ debe vivir en la Activity: es UI que necesita un Context.
        // El ViewModel solo avisa "el usuario quiere eliminar este costo", nunca decide cómo preguntarlo.
        viewModel.getSolicitarConfirmacionEliminar().observe(this, costo -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Costo")
                    .setMessage("¿Estás seguro de eliminar este registro?")
                    .setPositiveButton("Sí", (dialog, which) -> viewModel.confirmarEliminacion(costo))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        viewModel.getCostoRegistradoTrigger().observe(this, contador -> editCosto.setText(""));

        btnRegistrar.setOnClickListener(v -> {
            String monedaSeleccionada = spinnerMoneda.getSelectedItem().toString();
            viewModel.registrarCosto(editCosto.getText().toString(), monedaSeleccionada);
        });
    }
}
