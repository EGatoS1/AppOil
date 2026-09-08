package com.example.oil_app.data.repository;

import android.app.Application;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.oil_app.util.AppExecutors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Antes: guardarImagenEnGaleria(), generarPDFDesdeImagen() y guardarPDFEnArchivos()
 * vivían dentro de BoletaPreviewActivity, mezclando manejo de archivos con la Activity.
 * Nota clave: este Repository trabaja con Bitmap (datos de imagen), NUNCA con View.
 * Medir y dibujar el View sigue siendo trabajo de la Activity — eso no se puede mover.
 */
public class BoletaExportRepository {

    public interface ExportCallback {
        void onResultado(boolean exito, String mensaje);
    }

    private final Application application;

    public BoletaExportRepository(Application application) {
        this.application = application;
    }

    public void guardarComoImagenEnGaleria(Bitmap bitmap, String codigoGenerado, ExportCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File tempFile = File.createTempFile("boleta_" + codigoGenerado, ".png", application.getCacheDir());
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                boolean ok = guardarImagenEnGaleria(tempFile);
                postResultado(callback, ok,
                        ok ? "Imagen guardada en la galería" : "Error al guardar la imagen en la galería");
            } catch (Exception e) {
                e.printStackTrace();
                postResultado(callback, false, "Error al generar la boleta");
            }
        });
    }

    public void generarYGuardarPDF(Bitmap bitmap, String codigoGenerado, ExportCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            page.getCanvas().drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);

            try {
                File tempPDF = File.createTempFile("boleta_" + codigoGenerado, ".pdf", application.getCacheDir());
                try (FileOutputStream fos = new FileOutputStream(tempPDF)) {
                    document.writeTo(fos);
                }
                document.close();

                boolean ok = guardarPDFEnArchivos(tempPDF, "boleta_" + codigoGenerado + ".pdf");
                postResultado(callback, ok,
                        ok ? "PDF guardado en Documentos/Boletas" : "Error al guardar el PDF");

            } catch (IOException e) {
                e.printStackTrace();
                postResultado(callback, false, "Error al crear el PDF");
            }
        });
    }

    private boolean guardarImagenEnGaleria(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Boletas");

            Uri imageUri = application.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri == null) return false;

            try (OutputStream stream = application.getContentResolver().openOutputStream(imageUri)) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File galeriaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), file.getName());
            try (FileOutputStream outStream = new FileOutputStream(galeriaFile)) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean guardarPDFEnArchivos(File pdfTemporal, String nombreArchivo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Boletas");

            Uri uri = application.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            if (uri == null) return false;

            try (OutputStream out = application.getContentResolver().openOutputStream(uri);
                 FileInputStream in = new FileInputStream(pdfTemporal)) {
                copiar(in, out);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File directorio = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Boletas");
            if (!directorio.exists()) directorio.mkdirs();
            File destino = new File(directorio, nombreArchivo);

            try (FileInputStream in = new FileInputStream(pdfTemporal);
                 FileOutputStream out = new FileOutputStream(destino)) {
                copiar(in, out);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void copiar(FileInputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

    private void postResultado(ExportCallback callback, boolean exito, String mensaje) {
        AppExecutors.getInstance().mainThread().post(() -> callback.onResultado(exito, mensaje));
    }
}
