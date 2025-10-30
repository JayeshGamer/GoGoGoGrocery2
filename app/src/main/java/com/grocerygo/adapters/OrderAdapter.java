package com.grocerygo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerygo.OrderDetailActivity;
import com.grocerygo.app.R;
import com.grocerygo.models.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    private SimpleDateFormat dateFormat;
    private OnOrderClickListener orderClickListener;

    // Interface for order click listener
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    }

    // Method to set custom click listener (for admin panel)
    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.orderClickListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_enhanced, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set order ID
        holder.tvOrderId.setText("Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

        // Set order date
        if (order.getOrderDate() != null) {
            holder.tvOrderDate.setText("Ordered on " + dateFormat.format(order.getOrderDate()));
        } else {
            holder.tvOrderDate.setText("Order date not available");
        }

        // Set order items count and total
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.tvOrderItems.setText(itemCount + " items • ₹" + String.format("%.2f", order.getTotalAmount()));

        // Set order status with appropriate styling
        setOrderStatus(holder, order.getStatus());

        // Set delivery/status info
        setDeliveryInfo(holder, order);

        // Click listener - use custom listener if set (for admin panel), otherwise open details
        holder.itemView.setOnClickListener(v -> {
            if (orderClickListener != null) {
                orderClickListener.onOrderClick(order);
            } else {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("order_id", order.getOrderId());
                context.startActivity(intent);
            }
        });
    }

    private void setOrderStatus(OrderViewHolder holder, String status) {
        holder.tvOrderStatus.setText(capitalizeFirst(status));

        // Set background color based on status
        switch (status.toLowerCase()) {
            case "pending":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "confirmed":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2196F3"));
                break;
            case "delivered":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_delivered);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "cancelled":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#F44336"));
                break;
            case "shipped":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_shipped);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#9C27B0"));
                break;
            default:
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_badge);
                holder.tvOrderStatus.setTextColor(Color.WHITE);
                break;
        }
    }

    private void setDeliveryInfo(OrderViewHolder holder, Order order) {
        String status = order.getStatus().toLowerCase();

        switch (status) {
            case "delivered":
                if (order.getDeliveryDate() != null) {
                    holder.tvDeliveryInfo.setText("Delivered on " +
                            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(order.getDeliveryDate()));
                    holder.tvDeliveryInfo.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                } else {
                    holder.tvDeliveryInfo.setText("Delivered successfully");
                    holder.tvDeliveryInfo.setTextColor(ContextCompat.getColor(context, R.color.status_success));
                }
                holder.tvDeliveryInfo.setVisibility(View.VISIBLE);
                break;
            case "confirmed":
                holder.tvDeliveryInfo.setText("Your order is being prepared");
                holder.tvDeliveryInfo.setTextColor(ContextCompat.getColor(context, R.color.secondary_blue));
                holder.tvDeliveryInfo.setVisibility(View.VISIBLE);
                break;
            case "pending":
                holder.tvDeliveryInfo.setText("Waiting for confirmation");
                holder.tvDeliveryInfo.setTextColor(ContextCompat.getColor(context, R.color.accent_orange));
                holder.tvDeliveryInfo.setVisibility(View.VISIBLE);
                break;
            case "shipped":
                holder.tvDeliveryInfo.setText("Out for delivery");
                holder.tvDeliveryInfo.setTextColor(ContextCompat.getColor(context, R.color.primary_green));
                holder.tvDeliveryInfo.setVisibility(View.VISIBLE);
                break;
            case "cancelled":
                holder.tvDeliveryInfo.setText("Order was cancelled");
                holder.tvDeliveryInfo.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.tvDeliveryInfo.setVisibility(View.VISIBLE);
                break;
            default:
                holder.tvDeliveryInfo.setVisibility(View.GONE);
                break;
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderItems, tvDeliveryInfo;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvDeliveryInfo = itemView.findViewById(R.id.tvDeliveryInfo);
        }
    }
}
