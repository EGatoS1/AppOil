package com.example.oil_app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.Manifest;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.oil_app.ENTITY.CotizacionItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BoletaPreviewActivity extends AppCompatActivity {

    private TextView tvNombreCliente,etComentario, tvCodigoBoleta, tvFechaHora, tvEncabezadoProductos, tvTablaProductos, tvTotalGeneral, tvVendedor, tvLineaDetalle;
    private Button btnGenerar, btnGenerarPDF;
    private ArrayList<CotizacionItem> listaProductos;
    private String nombreCliente, nombreVendedor;
    private String codigoGenerado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_boleta);

        // Permiso de escritura para Android 9 o menor
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }

        // Recibir datos
        Intent intent = getIntent();
        nombreCliente = intent.getStringExtra("nombreCliente");
        nombreVendedor = intent.getStringExtra("nombreVendedor");
        listaProductos = (ArrayList<CotizacionItem>) intent.getSerializableExtra("listaProductos");

        if (nombreCliente == null || listaProductos == null) {
            Toast.makeText(this, "Error al recibir datos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencias UI
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

        llenarDatosEnPantalla();

        btnGenerar.setOnClickListener(v -> {
            View viewBoleta = findViewById(R.id.layoutBoletaCompleta);
            viewBoleta.post(() -> generarBoletaComoImagen(nombreCliente, listaProductos));

        });

        btnGenerarPDF.setOnClickListener(v -> {
            View viewBoleta = findViewById(R.id.layoutBoletaCompleta);

            // Mide y genera el bitmap (igual que en generarBoletaComoImagen)
            viewBoleta.measure(
                    View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            viewBoleta.layout(0, 0, viewBoleta.getMeasuredWidth(), viewBoleta.getMeasuredHeight());

            Bitmap bitmap = Bitmap.createBitmap(viewBoleta.getMeasuredWidth(), viewBoleta.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            viewBoleta.draw(canvas);

            generarPDFDesdeImagen(bitmap);
        });
    }
    private void llenarDatosEnPantalla() {
        String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "X");
        codigoGenerado = nombreUsuario.substring(0, 1).toUpperCase() + String.format("%07d", System.currentTimeMillis() % 10000000);

        tvNombreCliente.setText("Nombre: " + nombreCliente);
        tvCodigoBoleta.setText("Código: " + codigoGenerado);
        tvFechaHora.setText("Fecha: " + fechaHora);
        tvVendedor.setText("Vendedor: " + nombreVendedor);

        // Define el texto completo
        String texto = "DISTRIBUIDOR OIL SAC:\n" +
                "BCP SOLES: 191-2224311-087\n" +
                "BCP CCI SOLES: 002-19100222431108755\n" +
                "\n" +
                "BCP DOLARES: 191-2205549181\n" +
                "BCP DOLARES CCI: 002-19100220554918159\n" +
                "\n" +
                "BBVA SOLES: 0011-0176-0200398791\n" +
                "BBVA SOLES CCI: 011 176 000200398791 50\n";

        // Crea un SpannableString para poder aplicar formato
        SpannableString spannable = new SpannableString(texto);

        // Aplica negrita a "DISTRIBUIDOR OIL SAC:"
        int inicioDistribuidorOil = texto.indexOf("DISTRIBUIDOR OIL SAC:");
        int finDistribuidorOil = inicioDistribuidorOil + "DISTRIBUIDOR OIL SAC:".length();
        spannable.setSpan(new StyleSpan(Typeface.BOLD), inicioDistribuidorOil, finDistribuidorOil, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        // Asigna el texto con formato a tu EditText
        etComentario.setText(spannable);

        String encabezado = String.format(Locale.US, "%s %s %s %s %s",
                centrarTexto("Producto", 25),
                centrarTexto("Presentación", 14),
                centrarTexto("Cant.", 5),
                centrarTexto("Unit.", 8),
                centrarTexto("Total", 8));
        tvEncabezadoProductos.setText(encabezado);

        StringBuilder tabla = new StringBuilder();
        double total = 0;
        for (CotizacionItem item : listaProductos) {
            List<String> lineasProducto = dividirTextoPorPalabras(item.getProducto(), 25);
            List<String> lineasPresentacion = dividirTextoPorPalabras(item.getPresentacion(), 14);
            int maxLineas = Math.max(lineasProducto.size(), lineasPresentacion.size());

            for (int i = 0; i < maxLineas; i++) {
                String lp = i < lineasProducto.size() ? lineasProducto.get(i) : "";
                String lpr = i < lineasPresentacion.size() ? lineasPresentacion.get(i) : "";

                if (i == 0) {
                    tabla.append(String.format(Locale.US, "%-25s %-14s %-5s %-8s %-8s\n",
                            centrarTexto(lp, 25),
                            centrarTexto(lpr, 14),
                            centrarTexto(String.valueOf(item.getCantidad()), 5),
                            centrarTexto(String.format("%.2f", item.getPrecioUnitario()), 8),
                            centrarTexto(String.format("%.2f", item.getPrecioTotal()), 8)
                    ));
                } else {
                    tabla.append(String.format(Locale.US, "%-25s %-14s\n",
                            centrarTexto(lp, 25),
                            centrarTexto(lpr, 14)));
                }
            }
            total += item.getPrecioTotal();
        }

        tvTablaProductos.setText(tabla.toString());
        tvTotalGeneral.setText("Total General: S/ " + total);

        tvLineaDetalle.post(() -> {
            String palabra = " DETALLE ";
            Paint paint = tvLineaDetalle.getPaint();
            int widthPx = tvLineaDetalle.getWidth();
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
            tvLineaDetalle.setText(sb.toString());
        });
        TextView textView8 = findViewById(R.id.textView8);
        TextView textView10 = findViewById(R.id.textView5);

        centrarTextoConAsteriscos(textView8, "RESUMEN");
        centrarTextoConAsteriscos(textView10, "COTIZACIÓN");

    }
    private void centrarTextoConAsteriscos(TextView textView, String palabra) {
        textView.post(() -> {
            Paint paint = textView.getPaint();
            int widthPx = textView.getWidth();
            if (widthPx == 0) {
                widthPx = getResources().getDisplayMetrics().widthPixels - dpToPx(16); // fallback
            }

            float anchoCaracter = paint.measureText("*");
            float anchoPalabra = paint.measureText(" " + palabra + " ");
            int totalCaracteres = (int) (widthPx / anchoCaracter);
            int caracteresPalabra = (int) (anchoPalabra / anchoCaracter);

            int asteriscosRestantes = totalCaracteres - caracteresPalabra;
            int izq = asteriscosRestantes / 2, der = asteriscosRestantes - izq;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < izq; i++) sb.append("*");
            sb.append(" ").append(palabra).append(" ");
            for (int i = 0; i < der; i++) sb.append("*");

            textView.setText(sb.toString());
        });
    }

    private void generarBoletaComoImagen(String nombreCliente, ArrayList<CotizacionItem> listaProductos) {
        View viewBoleta = findViewById(R.id.layoutBoletaCompleta);


        viewBoleta.measure(
                View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        viewBoleta.layout(0, 0, viewBoleta.getMeasuredWidth(), viewBoleta.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(viewBoleta.getMeasuredWidth(), viewBoleta.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        viewBoleta.draw(canvas);

        try {
            File tempFile = File.createTempFile("boleta_" + codigoGenerado, ".png", getCacheDir());
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            guardarImagenEnGaleria(tempFile);
            Toast.makeText(this, "Imagen generada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar la boleta", Toast.LENGTH_SHORT).show();
        }
    }
    private void guardarImagenEnGaleria(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 y superior usa MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Boletas"); // Guardado en la carpeta Boletas

            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try (OutputStream stream = getContentResolver().openOutputStream(imageUri)) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar la imagen en la galería", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Para versiones anteriores de Android (antes de Android 10)
            File galeriaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), file.getName());
            try (FileOutputStream outStream = new FileOutputStream(galeriaFile)) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar la imagen en la galería", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void generarPDFDesdeImagen(Bitmap bitmap) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        try {
            // Crea archivo temporal del PDF
            File tempPDF = File.createTempFile("boleta_" + codigoGenerado, ".pdf", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempPDF);
            document.writeTo(fos);
            document.close();
            fos.close();

            // Ahora lo guardamos en carpeta pública
            guardarPDFEnArchivos(tempPDF);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear el PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarPDFEnArchivos(File pdfTemporal) {
        String nombreArchivo = "boleta_" + codigoGenerado + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ usa MediaStore para guardar archivos públicos
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Boletas");

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri);
                     FileInputStream in = new FileInputStream(pdfTemporal)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                    Toast.makeText(this, "PDF guardado en Documentos/Boletas", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al guardar el PDF", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Android 9 o menor: guardar directamente en la carpeta Documentos
            File directorio = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Boletas");
            if (!directorio.exists()) directorio.mkdirs();

            File destino = new File(directorio, nombreArchivo);
            try (FileInputStream in = new FileInputStream(pdfTemporal);
                 FileOutputStream out = new FileOutputStream(destino)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                Toast.makeText(this, "PDF guardado en Documentos/Boletas", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar el PDF", Toast.LENGTH_SHORT).show();
            }
        }
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
    public int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
    private String centrarTexto(String texto, int ancho) {
        int espacioIzquierda = (ancho - texto.length()) / 2;
        int espacioDerecha = ancho - texto.length() - espacioIzquierda;
        StringBuilder sb = new StringBuilder();

        // Espacio a la izquierda
        for (int i = 0; i < espacioIzquierda; i++) sb.append(" ");
        sb.append(texto);
        // Espacio a la derecha
        for (int i = 0; i < espacioDerecha; i++) sb.append(" ");

        return sb.toString();
    }
    private static List<String> dividirTextoPorPalabras(String texto, int maxLongitud) {
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            if (linea.length() + palabra.length() + 1 <= maxLongitud) {
                if (linea.length() > 0) linea.append(" ");
                linea.append(palabra);
            } else {
                lineas.add(String.format("%-" + maxLongitud + "s", linea.toString()));
                linea = new StringBuilder(palabra);
            }
        }
        if (linea.length() > 0) {
            lineas.add(String.format("%-" + maxLongitud + "s", linea.toString()));
        }
        return lineas;
    }
}
