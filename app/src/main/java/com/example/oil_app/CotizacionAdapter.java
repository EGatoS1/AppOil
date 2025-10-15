package com.example.oil_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oil_app.ENTITY.CotizacionItem;

import java.util.ArrayList;

public class CotizacionAdapter extends RecyclerView.Adapter<CotizacionAdapter.ViewHolder> {

    ArrayList<CotizacionItem> lista;

    public CotizacionAdapter(ArrayList<CotizacionItem> lista) {
        this.lista = lista;
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
        holder.tvCantidad.setText("Cantidad: " + item.getCantidad());
        holder.tvPrecioUnitario.setText("P. Unit: S/ " + item.getPrecioUnitario());
        holder.tvPrecioTotal.setText("Total: S/ " + item.getPrecioTotal());

        holder.btnEliminar.setOnClickListener(v -> {
            lista.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lista.size());
        });
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