package com.grocerygo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grocerygo.adapters.ProductAdapter;
import com.grocerygo.app.R;
import com.grocerygo.firebase.ProductRepository;
import com.grocerygo.models.CartItem;
import com.grocerygo.models.Product;
import com.grocerygo.utils.CartManager;
import com.grocerygo.utils.WishlistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity
        implements CartManager.CartUpdateListener, WishlistManager.WishlistUpdateListener {
    private static final String TAG = "ProductDetailActivity";

    private ImageView ivProductImage, ivFavorite;
    private CardView btnBack, btnShare;
    private TextView tvProductTitle, tvProductName, tvPrice, tvUnit, tvRating;
    private TextView tvQuantity, tvTotalPrice, tvProductDescription, tvReadMore;
    private CardView btnDecrease, btnIncrease, btnAddToCart, btnBuyNow;
    private RecyclerView rvRelatedProducts;
    private TextView tvCartQuantity;

    private ProductRepository productRepository;
    private CartManager cartManager;
    private WishlistManager wishlistManager;
    private ProductAdapter relatedProductsAdapter;
    private List<Product> relatedProducts = new ArrayList<>();

    private String productId, productName, productImage, productDescription, productUnit;
    private double productPrice, productRating;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize repository and managers
        productRepository = new ProductRepository();
        cartManager = CartManager.getInstance(this);
        wishlistManager = WishlistManager.getInstance();

        // Get product data from intent
        getProductDataFromIntent();

        // Initialize views
        initViews();

        // If product already in cart, start with that quantity so UI is in sync
        if (productId != null && cartManager != null) {
            int existingQty = cartManager.getProductQuantity(productId);
            if (existingQty > 0) {
                quantity = existingQty;
            }
        }

        // Setup UI
        setupProductDetails();
        setupQuantityControls();
        setupRelatedProducts();
        setupClickListeners();

        // Update wishlist icon based on current state
        updateWishlistIcon();

        updateCartQuantityBadge();
    }

    // Sync the current displayed quantity to the CartManager (real-time)
    private void syncQuantityToCart() {
        if (productId == null || cartManager == null) return;

        int cartQty = cartManager.getProductQuantity(productId);

        if (quantity <= 0) {
            // remove from cart
            cartManager.updateQuantity(productId, 0);
        } else {
            if (cartQty == 0) {
                // not present yet, add with desired quantity
                int cappedQty = Math.min(quantity, CartManager.MAX_QUANTITY);
                CartItem cartItem = new CartItem(
                        productId,
                        productName,
                        productImage,
                        productPrice,
                        productUnit != null ? productUnit : "KG",
                        cappedQty
                );
                cartManager.addToCart(cartItem);
            } else {
                // already present, update to new quantity
                cartManager.updateQuantity(productId, quantity);
            }
        }
        // badge will update via CartUpdateListener callback
    }

    private void getProductDataFromIntent() {
        Intent intent = getIntent();
        productId = intent.getStringExtra("product_id");
        productName = intent.getStringExtra("product_name");
        productPrice = intent.getDoubleExtra("product_price", 0.0);
        productImage = intent.getStringExtra("product_image");
        productDescription = intent.getStringExtra("product_description");
        productUnit = intent.getStringExtra("product_unit");
        productRating = intent.getDoubleExtra("product_rating", 4.0);

        // Validate required data
        if (productId == null || productName == null || productPrice <= 0) {
            Toast.makeText(this, "Error: Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        ivProductImage = findViewById(R.id.ivProductImage);
        ivFavorite = findViewById(R.id.ivFavorite);
        tvProductTitle = findViewById(R.id.tvProductTitle);
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvUnit = findViewById(R.id.tvUnit);
        tvRating = findViewById(R.id.tvRating);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvReadMore = findViewById(R.id.tvReadMore);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        rvRelatedProducts = findViewById(R.id.rvRelatedProducts);
        tvCartQuantity = findViewById(R.id.tvCartQuantity);
    }

    private void setupProductDetails() {
        // Set product details
        tvProductTitle.setText("Details");
        tvProductName.setText(productName != null ? productName : "Product");
        tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", productPrice));
        tvUnit.setText("/" + (productUnit != null ? productUnit : "KG"));
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", productRating));
        tvProductDescription.setText(productDescription != null ? productDescription : "No description available");

        // Load product image
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this)
                    .load(productImage)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);
        }

        // Initialize quantity display with just the number
        updateQuantityDisplay();

        // Update total price
        updateTotalPrice();
    }

    private void updateCartQuantityBadge() {
        if (tvCartQuantity != null && cartManager != null) {
            int cartQty = cartManager.getCartItemCount();
            if (cartQty > 0) {
                tvCartQuantity.setVisibility(View.VISIBLE);
                // show total items; if it's large, show a capped display like 99+
                if (cartQty > 99) {
                    tvCartQuantity.setText("99+");
                    tvCartQuantity.setContentDescription("99+ items in cart");
                } else {
                    tvCartQuantity.setText(String.valueOf(cartQty));
                    tvCartQuantity.setContentDescription(String.format(Locale.getDefault(), "%d items in cart", cartQty));
                }
            } else {
                tvCartQuantity.setVisibility(View.GONE);
                tvCartQuantity.setContentDescription("0 items in cart");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartManager.addCartUpdateListener(this);
        wishlistManager.addWishlistUpdateListener(this);
        updateCartQuantityBadge();
        updateWishlistIcon();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cartManager.removeCartUpdateListener(this);
        wishlistManager.removeWishlistUpdateListener(this);
    }

    @Override
    public void onCartUpdated(int itemCount) {
        runOnUiThread(() -> {
            // Update the global cart badge
            updateCartQuantityBadge();

            // Also update this product's displayed quantity if it changed elsewhere
            if (productId != null && cartManager != null) {
                int currentQty = cartManager.getProductQuantity(productId);
                // Only update if different to avoid unnecessary UI churn
                if (currentQty != quantity) {
                    quantity = currentQty; // will be 0 if removed
                    tvQuantity.setText(String.valueOf(quantity));
                    updateTotalPrice();
                }
            }
        });
    }

    @Override
    public void onWishlistUpdated(int itemCount) {
        runOnUiThread(() -> {
            // Update wishlist icon when wishlist changes
            updateWishlistIcon();
        });
    }

    /**
     * Update the wishlist icon based on current wishlist state
     */
    private void updateWishlistIcon() {
        if (productId != null && wishlistManager != null && ivFavorite != null) {
            boolean isInWishlist = wishlistManager.isInWishlist(productId);
            ivFavorite.setImageResource(isInWishlist ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }

    private void setupQuantityControls() {
        btnIncrease.setOnClickListener(v -> {
            if (quantity < CartManager.MAX_QUANTITY) {
                quantity++;
                updateQuantityDisplay();
                updateTotalPrice();
                syncQuantityToCart();
            } else {
                Toast.makeText(this, "Maximum quantity is " + CartManager.MAX_QUANTITY, Toast.LENGTH_SHORT).show();
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 0) {
                quantity--;
                updateQuantityDisplay();
                updateTotalPrice();
                syncQuantityToCart();

                // If quantity dropped to zero, inform the user
                if (quantity == 0) {
                    Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
        updateCartQuantityBadge();
    }

    private void updateTotalPrice() {
        double total = productPrice * quantity;
        tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.0f", total));
    }

    private void setupRelatedProducts() {
        relatedProductsAdapter = new ProductAdapter(this, relatedProducts);
        rvRelatedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelatedProducts.setAdapter(relatedProductsAdapter);

        // Load related products
        loadRelatedProducts();
    }

    private void loadRelatedProducts() {
        productRepository.getFeaturedProducts(5)
                .addOnSuccessListener(productList -> {
                    if (productList != null && !productList.isEmpty()) {
                        relatedProducts.clear();
                        relatedProducts.addAll(productList);
                        relatedProductsAdapter.updateList(productList);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error silently
                });
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Share button
        btnShare.setOnClickListener(v -> shareProduct());

        // Favorite button - NOW WITH REAL-TIME SYNC
        ivFavorite.setOnClickListener(v -> {
            if (productId == null || productId.isEmpty()) {
                Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
                return;
            }

            // Toggle wishlist state
            boolean currentlyInWishlist = wishlistManager.isInWishlist(productId);

            // Optimistically update UI
            ivFavorite.setImageResource(!currentlyInWishlist ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

            // Update in Firebase
            wishlistManager.toggleWishlist(productId)
                    .addOnSuccessListener(aVoid -> {
                        String message = !currentlyInWishlist ?
                                "Added to wishlist" : "Removed from wishlist";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Revert icon on failure
                        ivFavorite.setImageResource(currentlyInWishlist ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                        Toast.makeText(this, "Failed to update wishlist: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

        // Cart button - navigate to CartActivity instead of directly adding an item
        btnAddToCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Buy Now button - Direct checkout
        btnBuyNow.setOnClickListener(v -> {
            // Prevent checkout if quantity is zero (item removed)
            if (quantity <= 0) {
                Toast.makeText(ProductDetailActivity.this, "No item selected to buy", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navigate to checkout
            Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            intent.putExtra("product_id", productId);
            intent.putExtra("product_name", productName);
            intent.putExtra("product_price", productPrice);
            intent.putExtra("product_image", productImage);
            intent.putExtra("quantity", quantity);
            intent.putExtra("total_amount", productPrice * quantity);
            startActivity(intent);
        });

        // Read more/less
        tvReadMore.setOnClickListener(v -> {
            if (tvProductDescription.getMaxLines() == Integer.MAX_VALUE) {
                tvProductDescription.setMaxLines(3);
                tvReadMore.setText("Read More");
            } else {
                tvProductDescription.setMaxLines(Integer.MAX_VALUE);
                tvReadMore.setText("Read Less");
            }
        });
    }

    private void shareProduct() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareMessage = "Check out " + productName + " for just ₹" + (int)productPrice + " on GroceryGo!";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void addProductToCart() {
        if (productId == null || productName == null || productPrice <= 0) {
            Toast.makeText(this, "Error: Invalid product data", Toast.LENGTH_SHORT).show();
            return;
        }
        int cartQty = cartManager.getProductQuantity(productId);
        int newQty = cartQty + quantity;
        if (newQty > CartManager.MAX_QUANTITY) newQty = CartManager.MAX_QUANTITY;
        CartItem cartItem = new CartItem(
            productId,
            productName,
            productImage,
            productPrice,
            productUnit != null ? productUnit : "KG",
            newQty
        );
        cartManager.updateQuantity(productId, newQty);
        updateCartQuantityBadge();
        String message = newQty + " " + productName + " in cart";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
