package cz.fungisoft.coffeecompass2.activity.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;

/**
 * Fragment to show name of town selected to be notified about, with deleteUser button.
 */
public class SelectedTownFragment extends Fragment {

    private final List<FragmentRemovableListener> closeFragmentListeners = new ArrayList<>();

    public void addDeleteFragmentListener(FragmentRemovableListener listener) {
        closeFragmentListeners.add(listener);
    }
    public void removeDeleteFragmentListener(FragmentRemovableListener listener) {
        closeFragmentListeners.remove(listener);
    }


    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_TOWN_ITEM_ID = "town_id";

    private String townName;

    private View rootView;

    private ImageView closeIcon;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SelectedTownFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (rootView != null && townName != null) {
            showTown(rootView, townName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.selected_town_fragment, container, false);
        closeIcon = rootView.findViewById(R.id.town_close_icon);
        closeIcon.setOnClickListener(createOnClickListenerForCancelTownIcon());

        Bundle bundle = getArguments();
        if (bundle != null) {
            townName = bundle.getString(ARG_TOWN_ITEM_ID);
        }
        return rootView;
    }

    private void showTown(View rootView, String townName) {
        // Show the town name in a TextView
        if (townName != null && townName.length() > 1) {
            ((TextView) rootView.findViewById(R.id.town_textView)).setText(townName);
        }
    }

    /**
     * OnClick listener to handle click on edit CoffeeSite imageView icon
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForCancelTownIcon() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get selected cup image
                if (view instanceof ImageView) {
                    informTownFragmentRemovedListener();
                }
            }
        };
        return retVal;
    }

    private void informTownFragmentRemovedListener() {
        for (FragmentRemovableListener listener : closeFragmentListeners) {
            listener.onFragmentClosed(this);
        }
    }

    public String getTownName() {
        return townName;
    }
}
