package com.example.oil_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.entity.PresentacionEntity;
import com.example.oil_app.data.entity.ProductoEntity;
import com.example.oil_app.data.repository.PresentacionRepository;
import com.example.oil_app.data.repository.ProductoRepository;

import java.util.List;

public class SeleccionProductoViewModel extends AndroidViewModel {

    private final ProductoRepository productoRepository;
    private final PresentacionRepository presentacionRepository;

    private final LiveData<List<ProductoEntity>> productos;
    private final LiveData<List<PresentacionEntity>> presentaciones;

    // Eventos de un solo uso: la Activity los observa para mostrar Toast o navegar
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<SeleccionValida> navegarACostos = new MutableLiveData<>();

    public SeleccionProductoViewModel(@NonNull Application application) {
        super(application);
        productoRepository = new ProductoRepository(application);
        presentacionRepository = new PresentacionRepository(application);
        productos = productoRepository.obtenerProductos();
        presentaciones = presentacionRepository.obtenerPresentaciones();
    }

    public LiveData<List<ProductoEntity>> getProductos() {
        return productos;
    }

    public LiveData<List<PresentacionEntity>> getPresentaciones() {
        return presentaciones;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<SeleccionValida> getNavegarACostos() {
        return navegarACostos;
    }

    /**
     * Toda la validación que antes estaba dentro del onClickListener
     * de btnContinuar ahora vive aquí, sin tocar ninguna vista de Android.
     */
    public void onContinuarClic(String nombreProd, String nombrePres) {
        if (nombreProd == null || nombreProd.trim().isEmpty()
                || nombrePres == null || nombrePres.trim().isEmpty()) {
            mensajeError.setValue("Selecciona un producto y una presentación");
            return;
        }

        Integer productoId = buscarIdProducto(nombreProd.trim());
        if (productoId == null) {
            mensajeError.setValue("Producto no válido");
            return;
        }

        Integer presentacionId = buscarIdPresentacion(nombrePres.trim());
        if (presentacionId == null) {
            mensajeError.setValue("Presentación no válida");
            return;
        }

        navegarACostos.setValue(new SeleccionValida(productoId, presentacionId, nombreProd.trim(), nombrePres.trim()));
    }

    private Integer buscarIdProducto(String nombre) {
        List<ProductoEntity> lista = productos.getValue();
        if (lista == null) return null;
        for (ProductoEntity p : lista) {
            if (p.nombre.equals(nombre)) return p.id;
        }
        return null;
    }

    private Integer buscarIdPresentacion(String nombre) {
        List<PresentacionEntity> lista = presentaciones.getValue();
        if (lista == null) return null;
        for (PresentacionEntity p : lista) {
            if (p.presentacion.equals(nombre)) return p.id;
        }
        return null;
    }

    /** Datos que la Activity necesita para armar el Intent hacia RegistrarCostosActivity */
    public static class SeleccionValida {
        public final int productoId;
        public final int presentacionId;
        public final String productoNombre;
        public final String presentacionNombre;

        public SeleccionValida(int productoId, int presentacionId, String productoNombre, String presentacionNombre) {
            this.productoId = productoId;
            this.presentacionId = presentacionId;
            this.productoNombre = productoNombre;
            this.presentacionNombre = presentacionNombre;
        }
    }
}
