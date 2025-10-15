package com.example.oil_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MenuPrincipalActivity extends AppCompatActivity {

    Button btnCrearCotizacion, btnRegistrarCostos;
    TextView txtBienvenida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        txtBienvenida = findViewById(R.id.tvBienvenida);
        btnCrearCotizacion = findViewById(R.id.btnCrearCotizacion);
        btnRegistrarCostos = findViewById(R.id.btnRegistrarCostos);

        // Recuperar nombre del usuario
        SharedPreferences prefs = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        String nombre = prefs.getString("nombreUsuario", "Usuario");

        txtBienvenida.setText("HOLA " + nombre.toUpperCase());

        // Acciones de los botones
        btnCrearCotizacion.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearCotizacionActivity.class);
            startActivity(intent);
        });

        btnRegistrarCostos.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionProductoActivity.class);
            startActivity(intent);
        });
    }
}
