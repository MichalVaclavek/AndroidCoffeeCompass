package cz.fungisoft.coffeecompass2.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteListContent;


public class CoffeeSiteImageFragment extends Fragment {

    private CoffeeSite site;

    private CoffeeSiteListContent content;

    public static final String ARG_ITEM_ID = "item_id";

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

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            site = content.getItemsMap().get(getArguments().getString(ARG_ITEM_ID));

            if (this.site != null && !site.getMainImageURL().isEmpty()) {
                Picasso.get().load(site.getMainImageURL()).rotate(90).into(pictureImageView);
            }
        }
        return view;
    }
/*
    public void setCoffeeSite(CoffeeSite site) {
        this.site = site;
    }
*/
    public void setCoffeeSiteListContent(CoffeeSiteListContent content) {
        this.content = content;
        site = content.getItemsMap().get(getArguments().getString(ARG_ITEM_ID));
    }

}
