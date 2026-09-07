package com.example.oil_app;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.oil_app.data.entity.CotizacionItem;
import com.example.oil_app.viewmodel.BoletaPreviewViewModel;

import java.util.ArrayList;

import android.Manifest;

public class BoletaPreviewActivity extends AppCompatActivity {

    private TextView tvNombreCliente, tvCodigoBoleta, tvFechaHora, tvEncabezadoProductos,
            tvTablaProductos, tvTotalGeneral, tvVendedor, tvLineaDetalle;
    private TextView etComentario;
    private Button btnGenerar, btnGenerarPDF;

    private BoletaPreviewViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_boleta);

        // Permiso de escritura para Android 9 o menor: esto es una API del sistema,
        // no lógica de negocio, así que se queda igual que antes.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }

        // Leer los extras del Intent SÍ es trabajo de la Activity (el ViewModel no conoce Intent).
        String nombreCliente = getIntent().getStringExtra("nombreCliente");
        String nombreVendedor = getIntent().getStringExtra("nombreVendedor");
        String moneda = getIntent().getStringExtra("moneda");
        if (moneda == null || moneda.isEmpty()) moneda = "S/";
        @SuppressWarnings("unchecked")
        ArrayList<CotizacionItem> listaProductos =
                (ArrayList<CotizacionItem>) getIntent().getSerializableExtra("listaProductos");

        if (nombreCliente == null || listaProductos == null) {
            Toast.makeText(this, "Error al recibir datos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvNombreCliente = findViewById(R.id.tvNombreCliente);
        tvCodigoBoleta = findViewById(R.id.tvCodigoBoleta);
        tvFechaHora = findViewById(R.id.tvFechaHora);
        tvEncabezadoProductos = findViewById(R.id.tvEncabezadoProductos);
        tvTablaProductos = findViewById(R.id.tvTablaProductos);
        tvTotalGeneral = findViewById(R.id.tvTotalGeneral);
        tvVendedor = findViewById(R.id.tvVendedor);
        tvLineaDetalle = findViewById(R.id.tvLineaDetalle);
        etComentario = findViewById(R.id.editTextTextMultiLine2);
        btnGenerar = findViewById(R.id.btnGenerarBoleta);
        btnGenerarPDF = findViewById(R.id.btnGenerarPdf);

        viewModel = new ViewModelProvider(this).get(BoletaPreviewViewModel.class);

        // Antes: viewModel.inicializar(...) se llamaba directo aquí, con columnas de
        // ancho fijo (25/14/5/8/8 caracteres) sin importar el ancho real del celular.
        // Ahora: esperamos a que tvTablaProductos ya tenga su ancho definido por el
        // layout (todavía sin texto, pero el ancho del contenedor no depende del
        // contenido) y recién ahí calculamos cuántos caracteres monospace entran,
        // para pasárselo al ViewModel y que arme columnas proporcionales a ESE ancho.
        String monedaFinal = moneda;
        String nombreVendedorFinal = nombreVendedor;
        ArrayList<CotizacionItem> listaProductosFinal = listaProductos;
        tvTablaProductos.post(() -> {
            int caracteresDisponibles = calcularCaracteresMonospaceQueEntran(tvTablaProductos);
            viewModel.inicializar(nombreCliente, nombreVendedorFinal, monedaFinal, listaProductosFinal, caracteresDisponibles);
        });

        pintarContenidoEstatico();
        pintarDecoracionesDeTitulo();

        // --- Observer: pinta todo lo que el ViewModel ya calculó y formateó ---
        viewModel.getUiState().observe(this, estado -> {
            tvNombreCliente.setText(estado.nombreCliente);
            tvCodigoBoleta.setText(estado.codigoBoleta);
            tvFechaHora.setText(estado.fechaHora);
            tvVendedor.setText(estado.vendedor);
            tvEncabezadoProductos.setText(estado.encabezadoProductos);
            tvTablaProductos.setText(estado.tablaProductos);
            tvTotalGeneral.setText(estado.totalGeneral);
        });

        viewModel.getMensajeExportacion().observe(this, mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show());

        // --- Generar imagen: medir y dibujar el View es trabajo exclusivo de la Activity.
        // Una vez que el Bitmap existe, se lo entregamos al ViewModel para que decida qué hacer con él. ---
        btnGenerar.setOnClickListener(v -> {
            View viewBoleta = findViewById(R.id.layoutBoletaCompleta);
            viewBoleta.post(() -> viewModel.generarImagen(dibujarComoBitmap(viewBoleta)));
        });

        btnGenerarPDF.setOnClickListener(v -> {
            View viewBoleta = findViewById(R.id.layoutBoletaCompleta);
            viewModel.generarPDF(dibujarComoBitmap(viewBoleta));
        });
    }

    /** Antes: este bloque de measure/layout/draw estaba duplicado en los dos listeners. */
    private Bitmap dibujarComoBitmap(View viewBoleta) {
        viewBoleta.measure(
                View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        viewBoleta.layout(0, 0, viewBoleta.getMeasuredWidth(), viewBoleta.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(viewBoleta.getMeasuredWidth(), viewBoleta.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        viewBoleta.draw(canvas);
        return bitmap;
    }

    /** Texto bancario fijo con una parte en negrita: es contenido estático de UI, no datos del ViewModel. */
    private void pintarContenidoEstatico() {
        String texto = "DISTRIBUIDOR OIL SAC:\n" +
                "BCP SOLES: 191-2224311-087\n" +
                "BCP CCI SOLES: 002-19100222431108755\n" +
                "\n" +
                "BCP DOLARES: 191-2205549181\n" +
                "BCP DOLARES CCI: 002-19100220554918159\n" +
                "\n" +
                "BBVA SOLES: 0011-0176-0200398791\n" +
                "BBVA SOLES CCI: 011 176 000200398791 50\n";

        SpannableString spannable = new SpannableString(texto);
        int inicio = texto.indexOf("DISTRIBUIDOR OIL SAC:");
        int fin = inicio + "DISTRIBUIDOR OIL SAC:".length();
        spannable.setSpan(new StyleSpan(Typeface.BOLD), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        etComentario.setText(spannable);
    }

    /** Requiere medir el ancho real de los TextView, así que se queda en la Activity. */
    private void pintarDecoracionesDeTitulo() {
        tvLineaDetalle.post(() -> tvLineaDetalle.setText(centrarConAsteriscos(tvLineaDetalle, " DETALLE ")));

        TextView textView8 = findViewById(R.id.textView8);
        TextView textView10 = findViewById(R.id.textView5);
        textView8.post(() -> textView8.setText(centrarConAsteriscos(textView8, " RESUMEN ")));
        textView10.post(() -> textView10.setText(centrarConAsteriscos(textView10, " COTIZACIÓN ")));
    }

    private String centrarConAsteriscos(TextView textView, String palabra) {
        Paint paint = textView.getPaint();
        int widthPx = textView.getWidth();
        if (widthPx == 0) widthPx = getResources().getDisplayMetrics().widthPixels - dpToPx(16);

        float anchoCaracter = paint.measureText("*");
        float anchoPalabra = paint.measureText(palabra);
        int totalCaracteres = (int) (widthPx / anchoCaracter);
        int caracteresPalabra = (int) (anchoPalabra / anchoCaracter);
        int asteriscosRestantes = totalCaracteres - caracteresPalabra;
        int izq = asteriscosRestantes / 2, der = asteriscosRestantes - izq;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < izq; i++) sb.append("*");
        sb.append(palabra);
        for (int i = 0; i < der; i++) sb.append("*");
        return sb.toString();
    }

    /**
     * Mide cuántos caracteres monospace entran en el ancho real de este TextView.
     * Usa textView.getPaint() (el Paint que el propio TextView ya trae configurado
     * con su tamaño y tipografía reales) en vez de crear un Paint nuevo a mano —
     * así la medición coincide exacto con lo que después se va a dibujar, sin
     * depender de que Typeface.MONOSPACE y android:fontFamily="monospace" midan
     * igual (en algunos fabricantes de celular no es exactamente lo mismo).
     */
    private int calcularCaracteresMonospaceQueEntran(TextView textView) {
        float anchoCaracterPx = textView.getPaint().measureText("0"); // monospace: todos los caracteres miden igual
        int anchoDisponiblePx = textView.getWidth() - textView.getPaddingStart() - textView.getPaddingEnd();

        if (anchoCaracterPx <= 0 || anchoDisponiblePx <= 0) {
            return 64; // resguardo: si algo no se pudo medir, usamos el valor que ya tenías antes
        }
        return (int) (anchoDisponiblePx / anchoCaracterPx);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado, no se puede guardar la boleta", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
