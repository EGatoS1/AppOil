package com.example.oil_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.entity.CostoEntity;
import com.example.oil_app.data.repository.CostoRepository;

import java.util.List;

public class RegistrarCostosViewModel extends AndroidViewModel {

    private final CostoRepository costoRepository;

    private int productoId;
    private int presentacionId;
    private boolean inicializado = false;

    private LiveData<List<CostoEntity>> costos;
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Integer> costoRegistradoTrigger = new MutableLiveData<>(0);
    // Evento de un solo uso: la Activity observa esto para MOSTRAR el AlertDialog
    // (el diálogo en sí es UI, así que no puede vivir en el ViewModel).
    private final MutableLiveData<CostoEntity> solicitarConfirmacionEliminar = new MutableLiveData<>();

    public RegistrarCostosViewModel(@NonNull Application application) {
        super(application);
        costoRepository = new CostoRepository(application);
    }

    /** Antes: se leía productoId/presentacionId del Intent directo en onCreate(). */
    public void inicializar(int productoId, int presentacionId) {
        if (inicializado) return;
        inicializado = true;
        this.productoId = productoId;
        this.presentacionId = presentacionId;
        costos = costoRepository.obtenerCostosRecientes(productoId, presentacionId);
    }

    public LiveData<List<CostoEntity>> getCostos() {
        return costos;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public LiveData<Integer> getCostoRegistradoTrigger() {
        return costoRegistradoTrigger;
    }

    public LiveData<CostoEntity> getSolicitarConfirmacionEliminar() {
        return solicitarConfirmacionEliminar;
    }

    /** Antes: el cuerpo completo de registrarCosto() en la Activity. */
    public void registrarCosto(String costoStr, String monedaSeleccionada) {
        if (costoStr.isEmpty()) {
            mensajeError.setValue("Ingrese el costo");
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(costoStr);
        } catch (NumberFormatException e) {
            mensajeError.setValue("Costo inválido");
            return;
        }

        String simboloMoneda = monedaSeleccionada.contains("S/") ? "S/" : "$";

        CostoEntity costo = new CostoEntity();
        costo.productoId = productoId;
        costo.presentacionId = presentacionId;
        costo.costo = valor;
        costo.fechaRegistro = System.currentTimeMillis();
        costo.moneda = simboloMoneda;

        costoRepository.registrarCosto(costo, productoId, presentacionId);
        costoRegistradoTrigger.setValue(costoRegistradoTrigger.getValue() + 1);
    }

    /** Antes: btnEliminar.setOnClickListener llamaba directo a confirmarEliminacion(costo) */
    public void onEliminarClic(CostoEntity costo) {
        solicitarConfirmacionEliminar.setValue(costo);
    }

    /** La Activity llama esto solo después de que el usuario confirma en el AlertDialog. */
    public void confirmarEliminacion(CostoEntity costo) {
        costoRepository.eliminarCosto(costo.id, productoId, presentacionId);
    }
}
