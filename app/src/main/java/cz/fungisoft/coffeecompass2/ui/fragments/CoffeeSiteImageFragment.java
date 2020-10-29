package cz.fungisoft.coffeecompass2.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Fragment of the CoffeeSiteImageActivity view to show photo of the CoffeeSite.
 */
public class CoffeeSiteImageFragment extends Fragment {

    private CoffeeSite coffeeSite;

    private Context mContext;

    private ImageView pictureImageView;

    private View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public static CoffeeSiteImageFragment newInstance() {
        return new CoffeeSiteImageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.coffee_site_image_fragment, container, false);
        pictureImageView = view.findViewById(R.id.coffeesitePictureImageView);

        if (this.coffeeSite != null && !coffeeSite.getMainImageURL().isEmpty()) {
            if (!Utils.isOfflineModeOn(mContext)) {
                Picasso.get().load(coffeeSite.getMainImageURL())
                             .resize(0, pictureImageView.getMaxHeight())
                             .placeholder(R.drawable.kafe_backround_120x160)
                             .into(pictureImageView);
            } else {
                Picasso.get().load(ImageUtil.getImageFile(mContext, ImageUtil.COFFEESITE_IMAGE_DIR, coffeeSite.getMainImageFileName()))
                        .resize(0, pictureImageView.getMaxHeight())
                        .placeholder(R.drawable.kafe_backround_120x160)
                        .into(pictureImageView);
            }
        }
        return view;
    }

    public void setCoffeeSite(CoffeeSite site) {
        this.coffeeSite = site;
    }

}
