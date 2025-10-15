package com.example.oil_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oil_app.ENTITY.CostoEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CostoAdapter extends RecyclerView.Adapter<CostoAdapter.ViewHolder> {

    interface OnItemLongClick {
        void onLongClick(CostoEntity costo);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    List<CostoEntity> lista;
    OnItemLongClick listener;

    public CostoAdapter(List<CostoEntity> lista, OnItemLongClick listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_HEADER) ? R.layout.item_costo_header : R.layout.item_costo;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) return; // Encabezado

        CostoEntity costo = lista.get(position - 1);

        Locale localeEspañol = new Locale("es", "PE");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", localeEspañol);
        String fecha = sdf.format(new Date(costo.fechaRegistro));

        String[] parts = fecha.split(" "); // Dividir la fecha en partes (día, mes, año)
        String fechaFinal = parts[0] + " de " + capitalize(parts[1]) + " del " + parts[2];

        holder.textFecha.setText(fechaFinal);
        holder.textCosto.setText(costo.moneda + " " + String.format(Locale.getDefault(), "%.2f", costo.costo));


        holder.btnEliminar.setOnClickListener(v -> listener.onLongClick(costo));
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    @Override
    public int getItemCount() {
        return lista.size() + 1; // +1 por encabezado
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textFecha, textCosto;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFecha = itemView.findViewById(R.id.textFecha);
            textCosto = itemView.findViewById(R.id.textCosto);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}


