package cz.fungisoft.coffeecompass2.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
 * Adapter class to create and return fragments {@link CoffeeSiteDetailFragment} and
 * {@link CoffeeSiteImageFragment} in the CoffeeSiteDetailActivity.
 */
public class CoffeeSiteDetailsTabsAdapter extends FragmentStateAdapter {

    private final CoffeeSiteDetailFragment detailFragment;

    private final CoffeeSiteImageFragment imageFragment;

    public static final String ARG_OBJECT_FRAGMENT = "coffee_site";

    private static final int NUM_OF_TABS = 2;

    /**
     * The object this fragment is presenting.
     */
    private CoffeeSite coffeeSite;

    public CoffeeSiteDetailsTabsAdapter(Fragment fragment, CoffeeSite coffeeSite) {
        super(fragment);
        this.coffeeSite = coffeeSite;

        //detailFragment.setCoffeeSite(coffeeSite);
        Bundle detailsArgs = new Bundle();
        detailFragment = new CoffeeSiteDetailFragment();
        detailsArgs.putParcelable(ARG_OBJECT_FRAGMENT, coffeeSite);
        detailFragment.setArguments(detailsArgs);

        Bundle imageArgs = new Bundle();
        imageFragment = new CoffeeSiteImageFragment();
        imageArgs.putParcelable(ARG_OBJECT_FRAGMENT, coffeeSite);
        imageFragment.setArguments(imageArgs);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                return detailFragment;
            case 1:
                return imageFragment;
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_OF_TABS;
    }

    public void setCoffeeSite(CoffeeSite csm) {
        this.coffeeSite = csm;
        if (detailFragment != null) {
            detailFragment.setCoffeeSite(this.coffeeSite);
        }
        if (imageFragment != null) {
            imageFragment.setCoffeeSite(coffeeSite);
        }
    }

    public void setCurrentUser( LoggedInUser currentUser) {
        if (detailFragment != null) {
            detailFragment.setCurrentUser(currentUser);
        }
    }

}
