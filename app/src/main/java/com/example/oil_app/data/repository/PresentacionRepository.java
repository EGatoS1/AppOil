package com.example.oil_app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.AppDatabase;
import com.example.oil_app.data.dao.PresentacionDao;
import com.example.oil_app.data.entity.PresentacionEntity;
import com.example.oil_app.util.AppExecutors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PresentacionRepository {

    private final Application application;
    private final PresentacionDao presentacionDao;
    private final MutableLiveData<List<PresentacionEntity>> presentaciones = new MutableLiveData<>();

    public PresentacionRepository(Application application) {
        this.application = application;
        presentacionDao = AppDatabase.getInstance(application).presentacionDao();
    }

    public LiveData<List<PresentacionEntity>> obtenerPresentaciones() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<PresentacionEntity> resultado = presentacionDao.obtenerTodos();
            AppExecutors.getInstance().mainThread().post(() -> presentaciones.setValue(resultado));
        });
        return presentaciones;
    }

    /** Antes: cargarPresentacionesDesdeCSV() dentro de CrearCotizacionActivity. */
    public void cargarDesdeCSVSiEstaVacio() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<PresentacionEntity> actuales = presentacionDao.obtenerTodos();
            if (actuales != null && !actuales.isEmpty()) {
                return;
            }

            List<PresentacionEntity> nuevas = new ArrayList<>();
            try (InputStream is = application.getAssets().open("presentaciones.csv");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                reader.readLine(); // salta encabezado
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] partes = line.split(";");
                    if (partes.length > 0) {
                        String nombre = partes[0].trim().replaceAll(";+\\s*$", "");
                        if (!nombre.isEmpty()) {
                            PresentacionEntity presentacion = new PresentacionEntity();
                            presentacion.presentacion = nombre;
                            nuevas.add(presentacion);
                        }
                    }
                }
                presentacionDao.insertarTodo(nuevas);

            } catch (IOException e) {
                e.printStackTrace();
            }

            List<PresentacionEntity> actualizadas = presentacionDao.obtenerTodos();
            AppExecutors.getInstance().mainThread().post(() -> presentaciones.setValue(actualizadas));
        });
    }
}
