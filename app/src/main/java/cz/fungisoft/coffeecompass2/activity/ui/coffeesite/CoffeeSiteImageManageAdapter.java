package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.ImageFile;

/**
 * RecyclerView adapter for the image management grid in {@link CoffeeSiteImagesActivity}.
 * Displays existing images with a delete overlay button on each.
 */
public class CoffeeSiteImageManageAdapter extends RecyclerView.Adapter<CoffeeSiteImageManageAdapter.ImageViewHolder> {

    /**
     * Listener interface for delete button clicks on individual images.
     */
    public interface OnImageDeleteClickListener {
        void onImageDeleteClick(ImageFile imageFile, int position);
    }

    private final List<ImageFile> imageFiles = new ArrayList<>();
    private final OnImageDeleteClickListener deleteClickListener;

    public CoffeeSiteImageManageAdapter(OnImageDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    /**
     * Replaces the current image list and refreshes the grid.
     */
    public void setImageFiles(List<ImageFile> files) {
        imageFiles.clear();
        if (files != null) {
            imageFiles.addAll(files);
        }
        notifyDataSetChanged();
    }

    /**
     * Removes the image file at the given position.
     */
    public void removeAt(int position) {
        if (position >= 0 && position < imageFiles.size()) {
            imageFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageFiles.size());
        }
    }

    public int getImageCount() {
        return imageFiles.size();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coffeesite_image_manage, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageFile imageFile = imageFiles.get(position);
        String imageUrl = imageFile.getBytesUrl("mid");

        Picasso.get()
                .load(imageUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.kafe_backround_120x160)
                .into(holder.imageView);

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onImageDeleteClick(imageFile, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageView;
        final ImageView deleteButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.manage_image_view);
            deleteButton = itemView.findViewById(R.id.manage_image_delete_button);
        }
    }
}
