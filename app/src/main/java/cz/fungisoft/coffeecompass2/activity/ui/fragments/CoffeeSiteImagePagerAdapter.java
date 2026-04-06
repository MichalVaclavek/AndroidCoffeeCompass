package cz.fungisoft.coffeecompass2.activity.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;

public class CoffeeSiteImagePagerAdapter extends RecyclerView.Adapter<CoffeeSiteImagePagerAdapter.ImageViewHolder> {

    private static final String TAG = "CSImagePagerAdapter";

    private final List<String> imageUrls = new ArrayList<>();

    public CoffeeSiteImagePagerAdapter() {
    }

    public void setImageUrls(List<String> urls) {
        imageUrls.clear();
        if (urls != null) {
            imageUrls.addAll(urls);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coffee_site_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Log.i(TAG, "onBindViewHolder position=" + position + " url=" + url);
        holder.progressBar.setVisibility(View.VISIBLE);

        Picasso.get()
                .load(url)
                .fit()
                .centerInside()
                .placeholder(R.drawable.kafe_backround_120x160)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Picasso onSuccess position=" + holder.getBindingAdapterPosition());
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Picasso onError position=" + holder.getBindingAdapterPosition() + " url=" + url, e);
                        holder.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ProgressBar progressBar;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.coffee_site_image_item_view);
            progressBar = itemView.findViewById(R.id.coffee_site_image_item_progress);
        }
    }
}
