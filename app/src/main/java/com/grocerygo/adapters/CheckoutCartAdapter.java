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
import com.grocerygo.models.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutCartAdapter extends RecyclerView.Adapter<CheckoutCartAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private List<CartItem> displayedItems;
    private boolean isExpanded = false;
    private static final int MAX_COLLAPSED_ITEMS = 3;

    public CheckoutCartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
        this.displayedItems = new ArrayList<>();
        updateDisplayedItems();
    }

    private void updateDisplayedItems() {
        displayedItems.clear();
        if (isExpanded) {
            displayedItems.addAll(cartItems);
        } else {
            int itemsToShow = Math.min(cartItems.size(), MAX_COLLAPSED_ITEMS);
            displayedItems.addAll(cartItems.subList(0, itemsToShow));
        }
    }

    public void toggleExpand() {
        isExpanded = !isExpanded;
        updateDisplayedItems();
        notifyDataSetChanged();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public boolean shouldShowExpandButton() {
        return cartItems.size() > MAX_COLLAPSED_ITEMS;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = displayedItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvUnitPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getProductPrice()));

        double itemTotal = item.getProductPrice() * item.getQuantity();
        holder.tvItemTotal.setText(String.format(Locale.getDefault(), "₹%.2f", itemTotal));

        // Load product image
        if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
            Glide.with(context)
                    .load(item.getProductImage())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.ivProductImage);
        }
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvQuantity, tvUnitPrice, tvItemTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
        }
    }
}
