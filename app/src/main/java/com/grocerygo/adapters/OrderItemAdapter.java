package com.grocerygo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grocerygo.app.R;
import com.grocerygo.models.Order;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private Context context;
    private List<Order.OrderItem> orderItems;

    public OrderItemAdapter(Context context, List<Order.OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Order.OrderItem item = orderItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvPrice.setText("₹" + String.format("%.2f", item.getPrice()));
        holder.tvTotalPrice.setText("₹" + String.format("%.2f", item.getPrice() * item.getQuantity()));

        // Load product image
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .into(holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void updateItems(List<Order.OrderItem> newItems) {
        orderItems.clear();
        orderItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvQuantity, tvPrice, tvTotalPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}
