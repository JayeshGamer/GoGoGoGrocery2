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
import com.grocerygo.ProductListActivity;
import com.grocerygo.app.R;
import com.grocerygo.models.Category;
import com.grocerygo.utils.CategoryIconMapper;

import java.util.List;

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.CategoryViewHolder> {
    private static final String TAG = "CategoryGridAdapter";
    private Context context;
    private List<Category> categoryList;

    public CategoryGridAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_large, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());

        // Display product count with better formatting
        int productCount = category.getProductCount();
        String productCountText = productCount > 0
                ? productCount + " items"
                : "No items";
        holder.tvProductCount.setText(productCountText);

        // Use meaningful icons based on category name
        // If category has an imageUrl, load it, otherwise use the mapped icon
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getImageUrl())
                    .placeholder(CategoryIconMapper.getIconForCategory(category.getName()))
                    .error(CategoryIconMapper.getIconForCategory(category.getName()))
                    .centerCrop()
                    .into(holder.ivCategoryIcon);
        } else {
            // Use the meaningful icon directly
            holder.ivCategoryIcon.setImageResource(CategoryIconMapper.getIconForCategory(category.getName()));
        }

        // Show popular badge if product count > 20
        if (productCount > 20) {
            holder.cvPopularBadge.setVisibility(View.VISIBLE);
            holder.tvPopularBadge.setText("Hot");
        } else if (productCount > 10) {
            holder.cvPopularBadge.setVisibility(View.VISIBLE);
            holder.tvPopularBadge.setText("Popular");
        } else {
            holder.cvPopularBadge.setVisibility(View.GONE);
        }

        // Click listener for the entire card - navigate to ProductListActivity
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Category card clicked: " + category.getName());
            navigateToProductList(category);
        });

        // Click listener specifically for the Explore button - with null check
        if (holder.cvExploreButton != null) {
            holder.cvExploreButton.setOnClickListener(v -> {
                Log.d(TAG, "Explore button clicked for category: " + category.getName());
                navigateToProductList(category);
            });
        } else {
            Log.w(TAG, "Explore button is null for category: " + category.getName());
        }
    }

    /**
     * Navigate to ProductListActivity with the selected category
     */
    private void navigateToProductList(Category category) {
        if (category == null) {
            Log.e(TAG, "Category is null, cannot navigate");
            Toast.makeText(context, "Error: Category not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
            Log.e(TAG, "Category ID is null or empty for: " + category.getName());
            Toast.makeText(context, "Error: Invalid category", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Navigating to ProductListActivity with category: " + category.getName() + " (ID: " + category.getCategoryId() + ")");

        try {
            Intent intent = new Intent(context, ProductListActivity.class);
            intent.putExtra("category_id", category.getCategoryId());
            intent.putExtra("category_name", category.getName());
            context.startActivity(intent);
            Log.d(TAG, "Navigation successful");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to ProductListActivity", e);
            Toast.makeText(context, "Error opening category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateList(List<Category> newList) {
        categoryList.clear();
        categoryList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvProductCount;
        TextView tvPopularBadge;
        CardView cvPopularBadge;
        CardView cvExploreButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvProductCount = itemView.findViewById(R.id.tvProductCount);
            tvPopularBadge = itemView.findViewById(R.id.tvPopularBadge);
            cvPopularBadge = itemView.findViewById(R.id.cvPopularBadge);
            cvExploreButton = itemView.findViewById(R.id.cvExploreButton);
        }
    }
}
