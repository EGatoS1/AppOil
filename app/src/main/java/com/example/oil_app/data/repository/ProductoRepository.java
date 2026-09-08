package com.example.oil_app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oil_app.data.AppDatabase;
import com.example.oil_app.data.dao.ProductoDao;
import com.example.oil_app.data.entity.ProductoEntity;
import com.example.oil_app.util.AppExecutors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto único de acceso a los datos de Producto.
 * Usada tanto por SeleccionProductoViewModel como por CrearCotizacionViewModel:
 * la carga desde CSV que antes estaba duplicada en CrearCotizacionActivity
 * ahora vive en un solo lugar.
 */
public class ProductoRepository {

    private final Application application;
    private final ProductoDao productoDao;
    private final MutableLiveData<List<ProductoEntity>> productos = new MutableLiveData<>();

    public ProductoRepository(Application application) {
        this.application = application;
        productoDao = AppDatabase.getInstance(application).productoDao();
    }

    /** Devuelve un LiveData que la UI puede observar; la carga ocurre en background. */
    public LiveData<List<ProductoEntity>> obtenerProductos() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<ProductoEntity> resultado = productoDao.obtenerTodos();
            AppExecutors.getInstance().mainThread().post(() -> productos.setValue(resultado));
        });
        return productos;
    }

    /**
     * Antes: cargarProductosDesdeCSV() dentro de CrearCotizacionActivity.
     * Se ejecuta una sola vez: si ya hay productos en la base de datos, no hace nada.
     * Al terminar, refresca el LiveData de productos para que la UI se actualice sola.
     */
    public void cargarDesdeCSVSiEstaVacio() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<ProductoEntity> actuales = productoDao.obtenerTodos();
            if (actuales != null && !actuales.isEmpty()) {
                return;
            }

            List<ProductoEntity> nuevos = new ArrayList<>();
            try (InputStream is = application.getAssets().open("productos.csv");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                reader.readLine(); // salta encabezado
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] partes = line.split(";");
                    if (partes.length > 0) {
                        String nombre = partes[0].trim().replaceAll(";+\\s*$", "");
                        if (!nombre.isEmpty()) {
                            ProductoEntity producto = new ProductoEntity();
                            producto.nombre = nombre;
                            nuevos.add(producto);
                        }
                    }
                }
                productoDao.insertarTodo(nuevos);

            } catch (IOException e) {
                e.printStackTrace();
            }

            List<ProductoEntity> actualizados = productoDao.obtenerTodos();
            AppExecutors.getInstance().mainThread().post(() -> productos.setValue(actualizados));
        });
    }
}
