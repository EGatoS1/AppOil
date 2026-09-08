package com.example.oil_app.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.entity.CotizacionItem;
import com.example.oil_app.data.repository.BoletaExportRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BoletaPreviewViewModel extends AndroidViewModel {

    private final BoletaExportRepository exportRepository;
    private final MutableLiveData<BoletaUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeExportacion = new MutableLiveData<>();

    private String codigoGenerado;
    private boolean inicializado = false;

    public BoletaPreviewViewModel(@NonNull Application application) {
        super(application);
        exportRepository = new BoletaExportRepository(application);
    }

    public LiveData<BoletaUiState> getUiState() {
        return uiState;
    }

    public LiveData<String> getMensajeExportacion() {
        return mensajeExportacion;
    }

    public String getCodigoGenerado() {
        return codigoGenerado;
    }

    /**
     * Antes: todo esto vivía dentro de llenarDatosEnPantalla(), leyendo directo
     * los extras del Intent y escribiendo directo en cada TextView.
     * Se llama una sola vez (el flag evita recalcular si la Activity se recrea, por ejemplo al rotar).
     */
    public void inicializar(String nombreCliente, String nombreVendedor, String moneda,
                             ArrayList<CotizacionItem> listaProductos, int caracteresDisponibles) {
        if (inicializado) return;
        inicializado = true;

        // Antes: 25/14/5/8/8 fijos, sin importar el ancho real del celular.
        // Ahora: se calculan a partir de lo que la Activity midió en pantalla,
        // manteniendo las mismas proporciones relativas que ya tenían esos números.
        int[] anchos = calcularAnchosDeColumna(caracteresDisponibles);
        int anchoProducto = anchos[0];
        int anchoPresentacion = anchos[1];
        int anchoCantidad = anchos[2];
        int anchoUnitario = anchos[3];
        int anchoTotal = anchos[4];

        String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        SharedPreferences prefs = getApplication().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "X");
        codigoGenerado = nombreUsuario.substring(0, 1).toUpperCase()
                + String.format("%07d", System.currentTimeMillis() % 10000000);

        String encabezado = String.format(Locale.US, "%s %s %s %s %s",
                centrarTexto("Producto", anchoProducto),
                centrarTexto("Presentación", anchoPresentacion),
                centrarTexto("Cant.", anchoCantidad),
                centrarTexto("Unit.", anchoUnitario),
                centrarTexto("Total", anchoTotal));

        StringBuilder tabla = new StringBuilder();
        double total = 0;
        for (CotizacionItem item : listaProductos) {
            List<String> lineasProducto = dividirTextoPorPalabras(item.getProducto(), anchoProducto);
            List<String> lineasPresentacion = dividirTextoPorPalabras(item.getPresentacion(), anchoPresentacion);
            int maxLineas = Math.max(lineasProducto.size(), lineasPresentacion.size());

            for (int i = 0; i < maxLineas; i++) {
                String lp = i < lineasProducto.size() ? lineasProducto.get(i) : "";
                String lpr = i < lineasPresentacion.size() ? lineasPresentacion.get(i) : "";

                if (i == 0) {
                    tabla.append(String.format(Locale.US, "%-" + anchoProducto + "s %-" + anchoPresentacion + "s %-" + anchoCantidad + "s %-" + anchoUnitario + "s %-" + anchoTotal + "s\n",
                            centrarTexto(lp, anchoProducto),
                            centrarTexto(lpr, anchoPresentacion),
                            centrarTexto(String.valueOf(item.getCantidad()), anchoCantidad),
                            centrarTexto(String.format("%.2f", item.getPrecioUnitario()), anchoUnitario),
                            centrarTexto(String.format("%.2f", item.getPrecioTotal()), anchoTotal)
                    ));
                } else {
                    tabla.append(String.format(Locale.US, "%-" + anchoProducto + "s %-" + anchoPresentacion + "s\n",
                            centrarTexto(lp, anchoProducto),
                            centrarTexto(lpr, anchoPresentacion)));
                }
            }
            total += item.getPrecioTotal();
        }

        uiState.setValue(new BoletaUiState(
                "Nombre: " + nombreCliente,
                "Código: " + codigoGenerado,
                "Fecha: " + fechaHora,
                "Vendedor: " + nombreVendedor,
                encabezado,
                tabla.toString(),
                "Total General: " + moneda + " " + total
        ));
    }

    /**
     * Reparte "caracteresDisponibles" entre las 5 columnas, manteniendo las mismas
     * proporciones que ya tenían los números fijos originales (25/14/5/8/8, que suman
     * 60 + 4 espacios separadores = 64 caracteres de referencia).
     * Nunca deja una columna en menos de un mínimo legible, y si el celular es MUY
     * angosto o algo falló al medir, cae de vuelta a los números fijos de siempre.
     */
    private int[] calcularAnchosDeColumna(int caracteresDisponibles) {
        int separadores = 4; // los 4 espacios entre las 5 columnas del String.format
        int disponibleParaColumnas = caracteresDisponibles - separadores;

        if (disponibleParaColumnas < 40) {
            // Muy angosto para calcular proporciones de forma confiable: usamos
            // los valores originales tal cual, en vez de arriesgar columnas ilegibles.
            return new int[]{25, 14, 5, 8, 8};
        }

        int anchoProducto = Math.max(12, Math.round(disponibleParaColumnas * (25f / 60f)));
        int anchoPresentacion = Math.max(8, Math.round(disponibleParaColumnas * (14f / 60f)));
        int anchoCantidad = Math.max(4, Math.round(disponibleParaColumnas * (5f / 60f)));
        int anchoUnitario = Math.max(6, Math.round(disponibleParaColumnas * (8f / 60f)));
        // El total se lleva lo que sobra, para que la suma cierre exacta con el
        // ancho medido (evita dejar 1-2 caracteres de espacio suelto por el redondeo).
        int anchoTotal = disponibleParaColumnas - anchoProducto - anchoPresentacion - anchoCantidad - anchoUnitario;
        anchoTotal = Math.max(6, anchoTotal);

        return new int[]{anchoProducto, anchoPresentacion, anchoCantidad, anchoUnitario, anchoTotal};
    }

    /** Antes: btnGenerar.setOnClickListener llamaba directo a generarBoletaComoImagen(...) */
    public void generarImagen(Bitmap bitmapYaDibujado) {
        exportRepository.guardarComoImagenEnGaleria(bitmapYaDibujado, codigoGenerado,
                (exito, mensaje) -> mensajeExportacion.setValue(mensaje));
    }

    /** Antes: btnGenerarPDF.setOnClickListener llamaba directo a generarPDFDesdeImagen(...) */
    public void generarPDF(Bitmap bitmapYaDibujado) {
        exportRepository.generarYGuardarPDF(bitmapYaDibujado, codigoGenerado,
                (exito, mensaje) -> mensajeExportacion.setValue(mensaje));
    }

    // --- Helpers de texto puro: no tocan ninguna vista de Android, por eso pueden
    // vivir tranquilamente en el ViewModel (antes eran métodos privados de la Activity) ---

    private String centrarTexto(String texto, int ancho) {
        int espacioIzquierda = (ancho - texto.length()) / 2;
        int espacioDerecha = ancho - texto.length() - espacioIzquierda;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < espacioIzquierda; i++) sb.append(" ");
        sb.append(texto);
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

    /** Todo lo que la Activity necesita pintar en las vistas, ya formateado. */
    public static class BoletaUiState {
        public final String nombreCliente;
        public final String codigoBoleta;
        public final String fechaHora;
        public final String vendedor;
        public final String encabezadoProductos;
        public final String tablaProductos;
        public final String totalGeneral;

        public BoletaUiState(String nombreCliente, String codigoBoleta, String fechaHora, String vendedor,
                              String encabezadoProductos, String tablaProductos, String totalGeneral) {
            this.nombreCliente = nombreCliente;
            this.codigoBoleta = codigoBoleta;
            this.fechaHora = fechaHora;
            this.vendedor = vendedor;
            this.encabezadoProductos = encabezadoProductos;
            this.tablaProductos = tablaProductos;
            this.totalGeneral = totalGeneral;
        }
    }
}
