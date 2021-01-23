package cz.fungisoft.coffeecompass2.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Fragment containing child fragments CoffeeSiteImageFragment and CoffeeSiteImageFragment,
 * which is inserted into CoffeeSiteDetailActivity.
 */
public class DetailsCollectionFragment extends Fragment {

    // When requested, this adapter returns a CoffeeSiteDetailFragment,
    // or CoffeeSiteImageFragment
    CoffeeSiteDetailsTabsAdapter coffeeSiteDetailsTabsAdapter;
    ViewPager2 viewPager;

    private CoffeeSite coffeeSite;

    private boolean showImageFirstRequest = false;


    @Override
    public void onStart() {
        super.onStart();
        getView().setBackgroundColor(Color.TRANSPARENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            coffeeSite = bundle.getParcelable(CoffeeSiteDetailsTabsAdapter.ARG_OBJECT_FRAGMENT);
            if (!(coffeeSite instanceof CoffeeSiteMovable)) {
                coffeeSite = new CoffeeSiteMovable(coffeeSite);
            }
            showImageFirstRequest = bundle.getBoolean("showImageFirst");
        }
        return inflater.inflate(R.layout.details_fragments_collection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        coffeeSiteDetailsTabsAdapter = new CoffeeSiteDetailsTabsAdapter(this, coffeeSite);
        viewPager = view.findViewById(R.id.details_pager);
        viewPager.setAdapter(coffeeSiteDetailsTabsAdapter);

        /*
         * We need to setup height of the imageFragment to be the same as the height of the
         * details fragment
         */
        viewPager.registerOnPageChangeCallback( new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position,positionOffset,positionOffsetPixels);
                if (position > 0 && positionOffset == 0.0f && positionOffsetPixels == 0) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewPager.getLayoutParams();
                    lp.height = viewPager.getChildAt(0).getHeight();
                    viewPager.setLayoutParams(lp);
                }
            }

        }); // registerOnPageChangeCallback

        // TODO - not working yet
        if (showImageFirstRequest) {
            viewPager.setCurrentItem(1);
        }

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "detaily" : "foto")
        ).attach();
    }

    public void setCoffeeSite(CoffeeSite csm) {
        if (coffeeSiteDetailsTabsAdapter != null) {
            coffeeSiteDetailsTabsAdapter.setCoffeeSite(csm);
        }
    }

    public void setCurrentUser( LoggedInUser currentUser) {
        if (coffeeSiteDetailsTabsAdapter != null) {
            coffeeSiteDetailsTabsAdapter.setCurrentUser(currentUser);
        }
    }

}