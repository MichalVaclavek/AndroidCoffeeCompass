package cz.fungisoft.coffeecompass2.activity.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Fragment of the {@link CoffeeSiteDetailActivity} - view to show photo of the CoffeeSite
 * next to details of the CoffeeSite in the {@link CoffeeSiteDetailFragment}.
 */
public class CoffeeSiteImageFragment extends Fragment {

    private CoffeeSite coffeeSite;

    private Context mContext;

    private ProgressBar mProgressBar;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            coffeeSite = bundle.getParcelable(CoffeeSiteDetailsTabsAdapter.ARG_OBJECT_FRAGMENT);
            if (coffeeSite != null && !(coffeeSite instanceof CoffeeSiteMovable)) {
                coffeeSite = new CoffeeSiteMovable(coffeeSite);
            }
        }

        View view = inflater.inflate(R.layout.coffee_site_image_fragment, container, false);
        ImageView pictureImageView = view.findViewById(R.id.coffeesitePictureImageView);
        mProgressBar =  view.findViewById(R.id.image_fragment_progressBar);

        TextView noImageInfoLabel = view.findViewById(R.id.site_detail_no_photo_label);

        boolean isOnline = Utils.isOnline(mContext);
        if (isOnline && this.coffeeSite != null && !coffeeSite.getMainImageURL().isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            Picasso.get().load(coffeeSite.getMainImageURL())
                    .resize(0, pictureImageView.getMaxHeight())
                    .placeholder(R.drawable.kafe_backround_120x160)
                    .into(pictureImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                            noImageInfoLabel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mProgressBar.setVisibility(View.GONE);
                            noImageInfoLabel.setVisibility(View.VISIBLE);
                        }
                    });
        }
        if (!isOnline && this.coffeeSite != null) {
            Picasso.get().load(ImageUtil.getCoffeeSiteImageFile(mContext, this.coffeeSite))
                    .resize(0, pictureImageView.getMaxHeight())
                    .placeholder(R.drawable.kafe_backround_120x160)
                    .into(pictureImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                            noImageInfoLabel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mProgressBar.setVisibility(View.GONE);
                            noImageInfoLabel.setVisibility(View.VISIBLE);
                        }
                    });
        }

        return view;
    }

    public void setCoffeeSite(CoffeeSite site) {
        this.coffeeSite = site;
    }

}
