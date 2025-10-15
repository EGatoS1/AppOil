package com.example.oil_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editNombre;
    Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si ya hay un nombre guardado
        SharedPreferences prefs = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        String nombreGuardado = prefs.getString("nombreUsuario", null);

        if (nombreGuardado != null) {
            // Ya hay nombre guardado, ir directamente al menú
            Intent intent = new Intent(this, MenuPrincipalActivity.class);
            startActivity(intent);
            finish(); // Cerramos esta actividad
            return;
        }

        setContentView(R.layout.activity_main);

        editNombre = findViewById(R.id.editNombre);
        btnContinuar = findViewById(R.id.btnContinuar);

        btnContinuar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show();
            } else {
                // Guardar el nombre
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("nombreUsuario", nombre);
                editor.apply();

                // Ir a Menú
                Intent intent = new Intent(this, MenuPrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}