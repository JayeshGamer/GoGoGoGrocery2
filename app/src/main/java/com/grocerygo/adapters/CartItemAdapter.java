package com.grocerygo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grocerygo.app.R;
import com.grocerygo.models.CartItem;
import com.grocerygo.utils.CartManager;

import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private CartManager cartManager;

    public CartItemAdapter(Context context, List<CartItem> cartItems, CartManager cartManager) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartManager = cartManager;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Set product details
        holder.tvProductName.setText(item.getProductName());
        holder.tvProductWeight.setText(item.getProductUnit());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "₹%.0f", item.getProductPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "Total: ₹%.0f", item.getTotalPrice()));

        // Load product image
        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .into(holder.ivProductImage);

        // Increment quantity -> update via CartManager only
        holder.btnIncrement.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity < CartManager.MAX_QUANTITY) {
                cartManager.updateQuantity(item.getProductId(), currentQuantity + 1);
                // rely on CartManager listener in CartActivity to refresh adapter data
            } else {
                Toast.makeText(context, "Maximum quantity is " + CartManager.MAX_QUANTITY, Toast.LENGTH_SHORT).show();
            }
        });

        // Decrement quantity -> update via CartManager; removal handled by CartManager
        holder.btnDecrement.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                cartManager.updateQuantity(item.getProductId(), currentQuantity - 1);
            } else {
                // Request removal via CartManager. Do not mutate adapter list here.
                cartManager.removeFromCart(item.getProductId());
                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove item from cart (via CartManager)
        holder.btnRemove.setOnClickListener(v -> {
            cartManager.removeFromCart(item.getProductId());
            Toast.makeText(context, item.getProductName() + " removed from cart", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductWeight, tvProductPrice;
        TextView tvQuantity, tvTotalPrice;
        ImageButton btnIncrement, btnDecrement, btnRemove;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductWeight = itemView.findViewById(R.id.tvProductWeight);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnIncrement = itemView.findViewById(R.id.btnIncrement);
            btnDecrement = itemView.findViewById(R.id.btnDecrement);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
