package cz.fungisoft.coffeecompass.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;


public class CoffeeSiteImageFragment extends Fragment {

    private CoffeeSite site;

//    private final String baseURL = "http://coffeecompass.cz/rest/image/bytes/";
//    private String requestImageURL;

    public static CoffeeSiteImageFragment newInstance() {

        return new CoffeeSiteImageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState); // ??
        View view = inflater.inflate(R.layout.coffee_site_image_fragment, container, false);
        ImageView pictureImageView = view.findViewById(R.id.coffeesitePictureImageView);

        if (this.site != null && !site.getMainImageURL().isEmpty()) {
            Picasso.get().load(site.getMainImageURL()).rotate(90).into(pictureImageView);
        }

        return view;
    }

    public void setCoffeeSiteId(CoffeeSite site) {
        this.site = site;
//        requestImageURL = baseURL + siteId;
    }

}
