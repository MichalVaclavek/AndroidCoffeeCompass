package cz.fungisoft.coffeecompass2.activity.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImagesLoadListener;
import cz.fungisoft.coffeecompass2.asynctask.image.GetCoffeeSiteImageUrlsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Fragment of the CoffeeSiteDetailActivity - view to show photos of the CoffeeSite.
 * Uses a ViewPager2 with dot indicators and navigation arrows for swiping
 * between multiple images.
 */
public class CoffeeSiteImageFragment extends Fragment implements CoffeeSiteImagesLoadListener {

    private static final String TAG = "CoffeeSiteImageFrag";

    private CoffeeSite coffeeSite;

    private Context mContext;

    private ViewPager2 imagePager;
    private LinearLayout dotIndicatorLayout;
    private TextView arrowLeft;
    private TextView arrowRight;
    private TextView noImageInfoLabel;
    private CoffeeSiteImagePagerAdapter imagePagerAdapter;
    private int maxImagesToShow;

    /** Tracks the current number of images shown in the pager. */
    private int currentImageCount = 0;

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
        imagePager = view.findViewById(R.id.coffeesite_image_pager);
        dotIndicatorLayout = view.findViewById(R.id.coffeesite_image_dot_indicator);
        arrowLeft = view.findViewById(R.id.image_pager_arrow_left);
        arrowRight = view.findViewById(R.id.image_pager_arrow_right);
        noImageInfoLabel = view.findViewById(R.id.site_detail_no_photo_label);

        maxImagesToShow = getResources().getInteger(R.integer.coffeesite_detail_max_images);

        imagePagerAdapter = new CoffeeSiteImagePagerAdapter();
        imagePager.setAdapter(imagePagerAdapter);

        // React to page changes to update dots and arrow visibility
        imagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectDot(position);
                updateArrowVisibility(position);
            }
        });

        // Arrow click listeners
        arrowLeft.setOnClickListener(v -> {
            int current = imagePager.getCurrentItem();
            if (current > 0) {
                imagePager.setCurrentItem(current - 1, true);
            }
        });

        arrowRight.setOnClickListener(v -> {
            int current = imagePager.getCurrentItem();
            if (current < currentImageCount - 1) {
                imagePager.setCurrentItem(current + 1, true);
            }
        });

        loadImagesForCoffeeSite();

        return view;
    }

    public void setCoffeeSite(CoffeeSite site) {
        this.coffeeSite = site;
        if (imagePagerAdapter != null && isAdded()) {
            loadImagesForCoffeeSite();
        }
    }

    private void loadImagesForCoffeeSite() {
        if (mContext == null || coffeeSite == null) {
            return;
        }

        boolean isOnline = Utils.isOnline(mContext);
        List<String> initialImages = new ArrayList<>();

        if (isOnline && !coffeeSite.getMainImageURL().isEmpty()) {
            initialImages.add(coffeeSite.getMainImageURL());
        } else {
            File localFile = ImageUtil.getCoffeeSiteImageFile(mContext, coffeeSite);
            if (localFile.exists()) {
                initialImages.add(localFile.toURI().toString());
            }
        }

        applyImageUrls(initialImages);

        if (isOnline) {
            new GetCoffeeSiteImageUrlsAsyncTask(this, coffeeSite).execute();
        }
    }

    private void applyImageUrls(List<String> imageUrls) {
        List<String> normalizedUrls = normalizeImageUrls(imageUrls);
        currentImageCount = normalizedUrls.size();
        imagePagerAdapter.setImageUrls(normalizedUrls);
        buildDotIndicators(currentImageCount);
        updateArrowVisibility(imagePager.getCurrentItem());
        noImageInfoLabel.setVisibility(normalizedUrls.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    normalized.add(url);
                }
            }
        }

        List<String> limited = new ArrayList<>();
        for (String url : normalized) {
            limited.add(url);
            if (limited.size() >= maxImagesToShow) {
                break;
            }
        }
        return limited;
    }

    // -------- Dot indicator helpers --------

    /**
     * Builds dot indicator views. Shows them only when there are 2+ images.
     */
    private void buildDotIndicators(int count) {
        dotIndicatorLayout.removeAllViews();
        if (count <= 1) {
            dotIndicatorLayout.setVisibility(View.GONE);
            return;
        }
        dotIndicatorLayout.setVisibility(View.VISIBLE);

        int dotSizePx = dpToPx(8);
        int dotMarginPx = dpToPx(4);

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            params.setMargins(dotMarginPx, 0, dotMarginPx, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.image_pager_indicator_dot_unselected);
            dotIndicatorLayout.addView(dot);
        }

        // Highlight the current page
        selectDot(imagePager.getCurrentItem());
    }

    /**
     * Highlights the dot at the given position and dims all others.
     */
    private void selectDot(int position) {
        int count = dotIndicatorLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView dot = (ImageView) dotIndicatorLayout.getChildAt(i);
            dot.setImageResource(i == position
                    ? R.drawable.image_pager_indicator_dot_selected
                    : R.drawable.image_pager_indicator_dot_unselected);
        }
    }

    // -------- Arrow helpers --------

    /**
     * Shows/hides left and right arrows based on current position and image count.
     * Arrows are only shown when there are 2+ images.
     */
    private void updateArrowVisibility(int position) {
        if (currentImageCount <= 1) {
            arrowLeft.setVisibility(View.GONE);
            arrowRight.setVisibility(View.GONE);
            return;
        }
        arrowLeft.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
        arrowRight.setVisibility(position < currentImageCount - 1 ? View.VISIBLE : View.GONE);
    }

    // -------- Utility --------

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // -------- CoffeeSiteImagesLoadListener --------

    @Override
    public void onImageUrlsLoaded(List<String> imageUrls) {
        if (!isAdded()) {
            return;
        }
        applyImageUrls(imageUrls);
    }

    @Override
    public void onImageUrlsLoadFailed(Result.Error error) {
        Log.e(TAG, "Image URLs load failed: " + (error != null ? error.getDetail() : ""));
    }

}
