package com.example.oil_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.entity.CotizacionItem;
import com.example.oil_app.data.entity.PresentacionEntity;
import com.example.oil_app.data.entity.ProductoEntity;
import com.example.oil_app.data.repository.PresentacionRepository;
import com.example.oil_app.data.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.List;

public class CrearCotizacionViewModel extends AndroidViewModel {

    private final ProductoRepository productoRepository;
    private final PresentacionRepository presentacionRepository;

    private final LiveData<List<ProductoEntity>> productos;
    private final LiveData<List<PresentacionEntity>> presentaciones;

    private final MutableLiveData<List<CotizacionItem>> items = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<DatosBoleta> navegarABoleta = new MutableLiveData<>();
    // Contador simple: cada vez que sube, significa "se agregó un item con éxito,
    // limpia los campos". Un booleano fijo no serviría porque no cambiaría de valor
    // si el usuario agrega dos veces seguidas.
    private final MutableLiveData<Integer> itemAgregadoTrigger = new MutableLiveData<>(0);

    public CrearCotizacionViewModel(@NonNull Application application) {
        super(application);
        productoRepository = new ProductoRepository(application);
        presentacionRepository = new PresentacionRepository(application);

        // Antes: cargarProductosDesdeCSV() + cargarProductosDesdeBaseDeDatos() en onCreate()
        productoRepository.cargarDesdeCSVSiEstaVacio();
        presentacionRepository.cargarDesdeCSVSiEstaVacio();

        productos = productoRepository.obtenerProductos();
        presentaciones = presentacionRepository.obtenerPresentaciones();
    }

    public LiveData<List<ProductoEntity>> getProductos() {
        return productos;
    }

    public LiveData<List<PresentacionEntity>> getPresentaciones() {
        return presentaciones;
    }

    public LiveData<List<CotizacionItem>> getItems() {
        return items;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<DatosBoleta> getNavegarABoleta() {
        return navegarABoleta;
    }

    public LiveData<Integer> getItemAgregadoTrigger() {
        return itemAgregadoTrigger;
    }

    /** Antes: el bloque completo dentro de btnAgregarProducto.setOnClickListener(...) */
    public void agregarItem(String producto, String presentacion, String cantidadStr, String precioUnitarioStr) {
        if (producto.isEmpty() || presentacion.isEmpty() || cantidadStr.isEmpty() || precioUnitarioStr.isEmpty()) {
            mensajeError.setValue("Completa todos los campos");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            double precioUnitario = Double.parseDouble(precioUnitarioStr);
            double precioTotal = cantidad * precioUnitario;

            List<CotizacionItem> actuales = new ArrayList<>(items.getValue());
            actuales.add(new CotizacionItem(producto, presentacion, cantidad, precioUnitario, precioTotal));
            items.setValue(actuales);
            itemAgregadoTrigger.setValue(itemAgregadoTrigger.getValue() + 1);

        } catch (NumberFormatException e) {
            mensajeError.setValue("Cantidad o precio inválido");
        }
    }

    /** Antes: la lógica de "eliminar" vivía dentro del propio CotizacionAdapter */
    public void eliminarItem(int position) {
        List<CotizacionItem> actuales = new ArrayList<>(items.getValue());
        if (position >= 0 && position < actuales.size()) {
            actuales.remove(position);
            items.setValue(actuales);
        }
    }

    /** Antes: el bloque completo dentro de btnGenerarBoleta.setOnClickListener(...) */
    public void onGenerarBoletaClic(String nombreCliente, String nombreVendedor, String monedaSeleccionada) {
        if (nombreCliente.trim().isEmpty()) {
            mensajeError.setValue("Por favor ingresa el nombre del cliente");
            return;
        }
        if (nombreVendedor == null || nombreVendedor.trim().isEmpty()) {
            mensajeError.setValue("Por favor selecciona un vendedor");
            return;
        }

        // Mismo criterio que ya usábamos en RegistrarCostosViewModel: nos quedamos
        // con el símbolo, no con el texto completo del spinner ("Soles (S/)" -> "S/").
        String simboloMoneda = (monedaSeleccionada != null && monedaSeleccionada.contains("S/")) ? "S/" : "$";

        navegarABoleta.setValue(new DatosBoleta(
                nombreCliente.trim(),
                nombreVendedor.trim(),
                simboloMoneda,
                new ArrayList<>(items.getValue())
        ));
    }

    /** Datos que la Activity necesita para armar el Intent hacia BoletaPreviewActivity */
    public static class DatosBoleta {
        public final String nombreCliente;
        public final String nombreVendedor;
        public final String moneda;
        public final ArrayList<CotizacionItem> items;

        public DatosBoleta(String nombreCliente, String nombreVendedor, String moneda, ArrayList<CotizacionItem> items) {
            this.nombreCliente = nombreCliente;
            this.nombreVendedor = nombreVendedor;
            this.moneda = moneda;
            this.items = items;
        }
    }
}
