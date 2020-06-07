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

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
 * Fragment of the CoffeeSiteImageActivity view to show photo of the CoffeeSite.
 */
public class CoffeeSiteImageFragment extends Fragment {

    private CoffeeSite site;

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

        if (this.site != null && !site.getMainImageURL().isEmpty()) {
            Picasso.get().load(site.getMainImageURL()).into(pictureImageView);
        }
        return view;
    }

    public void setCoffeeSite(CoffeeSite site) {
        this.site = site;
    }

}
