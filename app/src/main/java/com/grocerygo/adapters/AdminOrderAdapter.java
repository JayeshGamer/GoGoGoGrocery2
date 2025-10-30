package com.grocerygo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.grocerygo.app.R;
import com.grocerygo.models.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private AdminOrderListener listener;

    public interface AdminOrderListener {
        void onAssignDelivery(Order order);
        void onConfirmOrder(Order order);
        void onMarkDelivered(Order order);
        void onViewDetails(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orders, AdminOrderListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order ID
        holder.tvOrderId.setText("Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

        // Order date
        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvOrderDate.setText(sdf.format(order.getOrderDate()));
        } else {
            holder.tvOrderDate.setText("Date not available");
        }

        // Items count and total
        holder.tvItemsCount.setText(order.getItemCount() + " items");
        holder.tvTotalAmount.setText("₹" + String.format(Locale.getDefault(), "%.2f", order.getTotalAmount()));

        // Status
        holder.tvStatus.setText(order.getStatus().toUpperCase());
        setStatusColor(holder.tvStatus, order.getStatus());

        // Delivery address
        holder.tvDeliveryAddress.setText(order.getDeliveryAddress());

        // Delivery partner info
        if (order.getAssignedDeliveryPartner() != null) {
            holder.tvDeliveryPartner.setVisibility(View.VISIBLE);
            holder.tvDeliveryPartner.setText("Delivery: " + order.getDeliveryPartnerName());
        } else {
            holder.tvDeliveryPartner.setVisibility(View.GONE);
        }

        // Button visibility and actions based on order state
        setupButtons(holder, order);

        // Click listeners
        holder.btnAssignDelivery.setOnClickListener(v -> listener.onAssignDelivery(order));
        holder.btnConfirmOrder.setOnClickListener(v -> listener.onConfirmOrder(order));
        holder.btnMarkDelivered.setOnClickListener(v -> listener.onMarkDelivered(order));
        holder.cardOrder.setOnClickListener(v -> listener.onViewDetails(order));
    }

    private void setupButtons(AdminOrderViewHolder holder, Order order) {
        String status = order.getStatus();
        boolean hasDeliveryPartner = order.getAssignedDeliveryPartner() != null;
        boolean isConfirmed = order.isConfirmed();

        // Hide all buttons first
        holder.btnAssignDelivery.setVisibility(View.GONE);
        holder.btnConfirmOrder.setVisibility(View.GONE);
        holder.btnMarkDelivered.setVisibility(View.GONE);

        if ("pending".equals(status)) {
            // Pending order: Show assign delivery and confirm buttons
            holder.btnAssignDelivery.setVisibility(View.VISIBLE);
            holder.btnAssignDelivery.setText(hasDeliveryPartner ? "Reassign Delivery" : "Assign Delivery");

            if (hasDeliveryPartner) {
                holder.btnConfirmOrder.setVisibility(View.VISIBLE);
            }
        } else if ("confirmed".equals(status)) {
            // Confirmed order: Show mark as delivered button
            holder.btnMarkDelivered.setVisibility(View.VISIBLE);
            holder.btnAssignDelivery.setVisibility(View.VISIBLE);
            holder.btnAssignDelivery.setText("Reassign Delivery");
        } else if ("delivered".equals(status)) {
            // Delivered order: No action buttons
            // Already delivered, nothing to show
        } else if ("cancelled".equals(status)) {
            // Cancelled order: No action buttons
            // Cancelled, nothing to show
        }
    }

    private void setStatusColor(TextView textView, String status) {
        int colorRes;
        switch (status.toLowerCase()) {
            case "pending":
                colorRes = R.color.status_pending;
                break;
            case "confirmed":
                colorRes = R.color.status_confirmed;
                break;
            case "delivered":
                colorRes = R.color.status_delivered;
                break;
            case "cancelled":
                colorRes = R.color.status_cancelled;
                break;
            default:
                colorRes = R.color.text_secondary;
        }
        textView.setTextColor(context.getResources().getColor(colorRes, null));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardOrder;
        TextView tvOrderId, tvOrderDate, tvItemsCount, tvTotalAmount;
        TextView tvStatus, tvDeliveryAddress, tvDeliveryPartner;
        Button btnAssignDelivery, btnConfirmOrder, btnMarkDelivered;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvDeliveryPartner = itemView.findViewById(R.id.tvDeliveryPartner);
            btnAssignDelivery = itemView.findViewById(R.id.btnAssignDelivery);
            btnConfirmOrder = itemView.findViewById(R.id.btnConfirmOrder);
            btnMarkDelivered = itemView.findViewById(R.id.btnMarkDelivered);
        }
    }
}

