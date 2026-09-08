package com.example.oil_app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.AppDatabase;
import com.example.oil_app.data.dao.CostoDao;
import com.example.oil_app.data.entity.CostoEntity;
import com.example.oil_app.util.AppExecutors;

import java.util.List;

/**
 * Antes: RegistrarCostosActivity llamaba directo a db.costoDao().insertar(...),
 * .eliminarPorId(...), .obtenerRecientesPorProductoPresentacion(...), etc.
 * Como AppDatabase tenía allowMainThreadQueries(), esas llamadas corrían
 * en el hilo principal sin que se notara. Aquí ya van todas a background.
 */
public class CostoRepository {

    private static final long DOS_ANIOS_EN_MS = 2L * 365 * 24 * 60 * 60 * 1000;

    private final CostoDao costoDao;
    private final MutableLiveData<List<CostoEntity>> costos = new MutableLiveData<>();

    public CostoRepository(Application application) {
        costoDao = AppDatabase.getInstance(application).costoDao();
    }

    public LiveData<List<CostoEntity>> obtenerCostosRecientes(int productoId, int presentacionId) {
        refrescar(productoId, presentacionId);
        return costos;
    }

    public void registrarCosto(CostoEntity costo, int productoId, int presentacionId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            costoDao.insertar(costo);
            cargarYPublicar(productoId, presentacionId);
        });
    }

    public void eliminarCosto(int costoId, int productoId, int presentacionId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            costoDao.eliminarPorId(costoId);
            cargarYPublicar(productoId, presentacionId);
        });
    }

    private void refrescar(int productoId, int presentacionId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            long limite = System.currentTimeMillis() - DOS_ANIOS_EN_MS;
            costoDao.eliminarAntiguos(limite); // antes: eliminarRegistrosAntiguos() en la Activity
            cargarYPublicar(productoId, presentacionId);
        });
    }

    /** Debe llamarse ya dentro del hilo de background (diskIO), nunca directo desde la UI. */
    private void cargarYPublicar(int productoId, int presentacionId) {
        long limite = System.currentTimeMillis() - DOS_ANIOS_EN_MS;
        List<CostoEntity> lista = costoDao.obtenerRecientesPorProductoPresentacion(limite, productoId, presentacionId);
        AppExecutors.getInstance().mainThread().post(() -> costos.setValue(lista));
    }
}
