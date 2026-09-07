package com.example.oil_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oil_app.data.entity.CotizacionItem;

import java.util.ArrayList;
import java.util.List;

public class CotizacionAdapter extends RecyclerView.Adapter<CotizacionAdapter.ViewHolder> {

    public interface OnEliminarListener {
        void onEliminar(int position);
    }

    private List<CotizacionItem> lista = new ArrayList<>();
    private final OnEliminarListener onEliminarListener;
    // Antes: "S/" estaba escrito fijo en onBindViewHolder. Ahora la Activity lo actualiza
    // cada vez que cambia el spinner de moneda, y esto se re-pinta con notifyDataSetChanged().
    private String simboloMoneda = "S/";

    public CotizacionAdapter(OnEliminarListener onEliminarListener) {
        this.onEliminarListener = onEliminarListener;
    }

    /** Nuevo: la Activity llama esto cuando el usuario cambia la moneda seleccionada. */
    public void setSimboloMoneda(String simbolo) {
        this.simboloMoneda = simbolo;
        notifyDataSetChanged();
    }

    /** Antes: la Activity hacía adapter.notifyDataSetChanged() a mano cada vez.
     *  Ahora la Activity llama esto dentro del observer del LiveData de items. */
    public void actualizarLista(List<CotizacionItem> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CotizacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CotizacionAdapter.ViewHolder holder, int position) {
        CotizacionItem item = lista.get(position);

        holder.tvProducto.setText(item.getProducto());
        holder.tvPresentacion.setText(item.getPresentacion());
        holder.tvCantidad.setText("x" + item.getCantidad());
        holder.tvPrecioUnitario.setText("P. Unit: " + simboloMoneda + " " + String.format(java.util.Locale.US, "%.2f", item.getPrecioUnitario()));
        holder.tvPrecioTotal.setText(simboloMoneda + " " + String.format(java.util.Locale.US, "%.2f", item.getPrecioTotal()));

        holder.btnEliminar.setOnClickListener(v -> onEliminarListener.onEliminar(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvProducto, tvPresentacion, tvCantidad, tvPrecioUnitario, tvPrecioTotal;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProducto = itemView.findViewById(R.id.tvProducto);
            tvPresentacion = itemView.findViewById(R.id.tvPresentacion);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvPrecioUnitario = itemView.findViewById(R.id.tvPrecioUnitario);
            tvPrecioTotal = itemView.findViewById(R.id.tvPrecioTotal);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
