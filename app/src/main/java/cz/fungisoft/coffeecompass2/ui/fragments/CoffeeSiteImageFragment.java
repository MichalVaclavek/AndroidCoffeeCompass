package cz.fungisoft.coffeecompass2.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;

/**
 * Fragment of the CoffeeSiteImageActivity view to show photo of the CoffeeSite.
 */
public class CoffeeSiteImageFragment extends Fragment {

    private CoffeeSite coffeeSite;
    private boolean offLineModeOn;

    public static CoffeeSiteImageFragment newInstance() {
        return new CoffeeSiteImageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.coffee_site_image_fragment, container, false);
        ImageView pictureImageView = view.findViewById(R.id.coffeesitePictureImageView);

        if (this.coffeeSite != null && !coffeeSite.getMainImageURL().isEmpty()) {
            if (!offLineModeOn) {
                Picasso.get().load(coffeeSite.getMainImageURL())
                             .resize(0, pictureImageView.getMaxHeight())
                             .into(pictureImageView);
            } else {
                Picasso.get().load(ImageUtil.getImageFile(Objects.requireNonNull(this.getContext()).getApplicationContext(), ImageUtil.COFFEESITE_IMAGE_DIR, coffeeSite.getMainImageFileName()))
                        .fit().placeholder(R.drawable.kafe_backround_120x160)
                        .into(pictureImageView);
            }
        }
        return view;
    }

    public void setCoffeeSite(CoffeeSite site) {
        this.coffeeSite = site;
    }

    public void setOffLineModeOn(boolean offLineModeOn) {
        this.offLineModeOn = offLineModeOn;
    }

}
