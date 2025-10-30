package com.grocerygo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grocerygo.ProductDetailActivity;
import com.grocerygo.app.R;
import com.grocerygo.models.CartItem;
import com.grocerygo.models.Product;
import com.grocerygo.utils.CartManager;
import com.grocerygo.utils.WishlistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private List<Product> productListFull; // For search/filter functionality
    private CartManager cartManager;
    private WishlistManager wishlistManager;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.cartManager = CartManager.getInstance(context);
        this.wishlistManager = WishlistManager.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvCurrentPrice.setText(String.format(Locale.getDefault(), "₹%.0f", product.getPrice()));
        holder.tvUnit.setText("/" + product.getUnit());
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));

        // Load product image using Glide
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .into(holder.ivProductImage);

        // Update UI based on cart state
        updateCartUI(holder, product);

        // Update wishlist icon based on current state
        updateWishlistIcon(holder, product);

        // Wishlist button click listener - REAL-TIME SYNC
        holder.ivFavorite.setOnClickListener(v -> {
            if (product.getProductId() == null || product.getProductId().isEmpty()) {
                Toast.makeText(context, "Invalid product", Toast.LENGTH_SHORT).show();
                return;
            }

            // Toggle wishlist state
            boolean currentlyInWishlist = wishlistManager.isInWishlist(product.getProductId());

            // Optimistically update UI
            holder.ivFavorite.setImageResource(!currentlyInWishlist ?
                R.drawable.ic_favorite : R.drawable.ic_favorite_border);

            // Update in Firebase
            wishlistManager.toggleWishlist(product.getProductId())
                    .addOnSuccessListener(aVoid -> {
                        String message = !currentlyInWishlist ?
                            "Added to wishlist" : "Removed from wishlist";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Revert icon on failure
                        holder.ivFavorite.setImageResource(currentlyInWishlist ?
                            R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                        Toast.makeText(context, "Failed to update wishlist", Toast.LENGTH_SHORT).show();
                    });
        });

        // Click listener on product IMAGE to open product details
        holder.ivProductImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getProductId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_image", product.getImageUrl());
            intent.putExtra("product_description", product.getDescription());
            intent.putExtra("product_unit", product.getUnit());
            intent.putExtra("product_rating", product.getRating());
            context.startActivity(intent);
        });

        // Click listener on ADD BUTTON to add to cart
        holder.cvAddButton.setOnClickListener(v -> {
            try {
                // Validate product data
                if (product.getProductId() == null || product.getName() == null) {
                    Toast.makeText(context, "Error: Invalid product data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prevent adding if already at max quantity
                int currentQty = cartManager.getProductQuantity(product.getProductId());
                if (currentQty >= CartManager.MAX_QUANTITY) {
                    Toast.makeText(context, "Maximum quantity is " + CartManager.MAX_QUANTITY, Toast.LENGTH_SHORT).show();
                    updateCartUI(holder, product);
                    return;
                }

                // Create cart item
                CartItem cartItem = new CartItem(
                        product.getProductId(),
                        product.getName(),
                        product.getImageUrl(),
                        product.getPrice(),
                        product.getUnit(),
                        1 // Add 1 unit
                );

                // Add to cart
                cartManager.addToCart(cartItem);

                // Update UI to show quantity controls
                updateCartUI(holder, product);

                // Show feedback to user
                Toast.makeText(context,
                        product.getName() + " added to cart",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("ProductAdapter", "Error adding to cart", e);
                Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener on REMOVE BUTTON to decrement or remove from cart
        holder.cvRemoveButton.setOnClickListener(v -> {
            try {
                String productId = product.getProductId();
                int currentQuantity = cartManager.getProductQuantity(productId);

                if (currentQuantity > 0) {
                    // Decrement quantity or remove if quantity becomes 0
                    cartManager.updateQuantity(productId, currentQuantity - 1);

                    // Update UI
                    updateCartUI(holder, product);

                    // Show feedback
                    if (currentQuantity == 1) {
                        Toast.makeText(context,
                                product.getName() + " removed from cart",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                "Quantity updated",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e("ProductAdapter", "Error removing from cart", e);
                Toast.makeText(context, "Failed to update cart", Toast.LENGTH_SHORT).show();
            }
        });

        // Also keep the old item click listener for the card (navigates to details)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getProductId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_image", product.getImageUrl());
            intent.putExtra("product_description", product.getDescription());
            intent.putExtra("product_unit", product.getUnit());
            intent.putExtra("product_rating", product.getRating());
            context.startActivity(intent);
        });
    }

    /**
     * Update the UI to show/hide quantity controls based on cart state
     */
    private void updateCartUI(ProductViewHolder holder, Product product) {
        int quantity = cartManager.getProductQuantity(product.getProductId());

        // Always show both buttons
        holder.cvRemoveButton.setVisibility(View.VISIBLE);
        holder.cvAddButton.setVisibility(View.VISIBLE);

        if (quantity > 0) {
            // Item is in cart - show quantity badge on the add button
            holder.tvQuantity.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText(String.valueOf(quantity));

            // Enable minus button
            holder.cvRemoveButton.setAlpha(1.0f);
            holder.cvRemoveButton.setEnabled(true);
        } else {
            // Item not in cart - hide quantity, disable minus button
            holder.tvQuantity.setVisibility(View.GONE);

            // Make minus button semi-transparent to indicate it's disabled
            holder.cvRemoveButton.setAlpha(0.3f);
            holder.cvRemoveButton.setEnabled(false);
        }
    }

    /**
     * Update the wishlist icon based on current wishlist state
     */
    private void updateWishlistIcon(ProductViewHolder holder, Product product) {
        if (product.getProductId() != null && holder.ivFavorite != null) {
            boolean isInWishlist = wishlistManager.isInWishlist(product.getProductId());
            holder.ivFavorite.setImageResource(isInWishlist ?
                R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        productListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(productListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : productListFull) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery) ||
                    product.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    productList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivFavorite;
        TextView tvProductName, tvCurrentPrice, tvUnit, tvRating, tvQuantity;
        CardView cvAddButton, cvRemoveButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCurrentPrice = itemView.findViewById(R.id.tvCurrentPrice);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            cvAddButton = itemView.findViewById(R.id.cvAddButton);
            cvRemoveButton = itemView.findViewById(R.id.cvRemoveButton);
        }
    }
}
