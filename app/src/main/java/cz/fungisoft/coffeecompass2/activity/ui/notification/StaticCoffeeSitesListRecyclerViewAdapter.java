package cz.fungisoft.coffeecompass2.activity.ui.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.activity.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Adapter to show list of CoffeeSites received upon Notification
 * or list of CoffeeSites in town. Used by StaticCoffeeSitesListActivity.
 */
public class StaticCoffeeSitesListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "StaticCoffeeSitesAdapter";

    private final boolean mTwoPane;

    private final StaticCoffeeSitesListActivity mParentActivity;

    /**
     * List of CoffeeSites to show in RecyclerView
     */
    private List<CoffeeSite> mValues;

    /**
     * Used for opening CoffeeSiteDetailActivity
     */
    private final View.OnClickListener mOnClickListenerToCoffeeSiteDetailActivityStart;

    /**
     * Used for opening CoffeeSiteDetailActivity with image first
     */
    private final View.OnClickListener mOnClickListenerToCoffeeSiteImageActivityStart;

    /**
     * Standard constructor of the class NotificationNewCoffeeSitesRecyclerViewAdapter
     *
     * @param mValues - list of CoffeeSites to be shown by the NotificationNewCoffeeSitesListActivity
     * @param parent - parent Activity for the Adapter, in this case this NotificationNewCoffeeSitesListActivity
     */
    public StaticCoffeeSitesListRecyclerViewAdapter(StaticCoffeeSitesListActivity parent,
                                                    List<CoffeeSite> mValues, boolean twoPane) {
        mParentActivity = parent;
        this.mValues = mValues;
        mTwoPane = twoPane;

        mOnClickListenerToCoffeeSiteDetailActivityStart = createOnClickListener();
        mOnClickListenerToCoffeeSiteImageActivityStart = createOnClickListenerForShowImageActivityStart();
    }

    /**
     * OnClick listener to open CoffeeSiteDetailActivity to show details
     * of the CoffeeSite as it is shown when searching and looking into the details.
     *
     * @return
     */
    private View.OnClickListener createOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSite coffeeSite = (CoffeeSite) view.getTag();
                if (mTwoPane) {
                    // Open CoffeeSiteDetailFragment, if the coffeeSite is clicked and there is
                    // landscape orientation to show details of the CoffeeSiteMovable holding this coffeeSite
                    Bundle arguments = new Bundle();
                    arguments.putString(CoffeeSiteDetailFragment.ARG_ITEM_ID, Long.toString(coffeeSite.getId()));
                    CoffeeSiteDetailFragment fragment = new CoffeeSiteDetailFragment();
                    fragment.setArguments(arguments);
                } else {
                    // Open CoffeeSiteDetailActivity if the coffeeSite is clicked
                    // to show details of the CoffeeSiteMovable holding this coffeeSite
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) coffeeSite);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }

    /**
     * OnClick listener to open CoffeeSiteDetailActivity to show picture
     * of the CoffeeSite as it is shown when searching and looking into the details.
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForShowImageActivityStart() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSite coffeeSite = (CoffeeSite) view.getTag();
                if (coffeeSite != null && !coffeeSite.getMainImageURL().isEmpty()) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) coffeeSite);
                    intent.putExtra("showImageFirst", true);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }


    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder retVal;
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_new_list_content, parent, false);
        retVal = new ViewHolder(view);
        return retVal;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder viewHolder1 = (ViewHolder) viewHolder;
        setupBasicViewHolder(position, viewHolder1);
    }

    @Override
    public int getItemCount() {
        return (mValues != null) ? mValues.size() : 0;
    }

    /**
     * Setup all views values and assign some click listeners
     *
     * @param position
     * @param viewHolder
     */
    private void setupBasicViewHolder(int position, ViewHolder viewHolder) {

        viewHolder.csNameView.setText(this.mValues.get(position).getName());
        viewHolder.locAndTypeView.setText(this.mValues.get(position).getTypPodniku().toString() + ", " +  this.mValues.get(position).getTypLokality().toString());
        viewHolder.createdOnView.setText(this.mValues.get(position).getCreatedOnString());

        String coffeeSorts = this.mValues.get(position).getCoffeeSortsOneString();
        viewHolder.coffeeSortsView.setText(coffeeSorts);

        if (!this.mValues.get(position).getMesto().isEmpty()) {
            viewHolder.cityView.setText(this.mValues.get(position).getMesto());
        }

        boolean isOnline = Utils.isOnline(mParentActivity.getApplicationContext());
        if (isOnline && !this.mValues.get(position).getMainImageURL().isEmpty()) {
            Picasso.get().load(this.mValues.get(position).getMainImageURL())
                   .fit().placeholder(R.drawable.kafe_backround_120x160)
                   .into(viewHolder.siteFoto);
        }
        if (!isOnline || this.mValues.get(position).getMainImageURL().isEmpty()) {
            Picasso.get().load(ImageUtil.getCoffeeSiteImageFile(mParentActivity.getApplicationContext(), this.mValues.get(position)))
                         .fit().placeholder(R.drawable.kafe_backround_120x160)
                         .into(viewHolder.siteFoto);
        }

        // Foto and main Label with CoffeeSite name are clickable
        viewHolder.siteFoto.setTag(this.mValues.get(position));
        viewHolder.siteFoto.setOnClickListener(mOnClickListenerToCoffeeSiteImageActivityStart);

        viewHolder.coffeeSiteDataLayout.setTag(this.mValues.get(position));
        viewHolder.coffeeSiteDataLayout.setOnClickListener(mOnClickListenerToCoffeeSiteDetailActivityStart);
    }

    /**
     * Inner ViewHolder class for NotificationNewCoffeeSitesRecyclerViewAdapter
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView csNameView; // to show name of CoffeeSite
        final TextView locAndTypeView; // to show type of the CoffeeSite and location type
        final TextView createdOnView; // to show created date of this CoffeeSite
        final TextView coffeeSortsView; // to show list of coffee sorts available
        final TextView cityView; // to show city name of the CoffeeSite
        final LinearLayout coffeeSiteDataLayout; // to assign click listener to open details activity

        /**
         * To insert listener to whole group of TextViews
         */
        final LinearLayout createdOnLinearLayout;
        final LinearLayout locationAndSortsLinearLayout;

        final ImageView siteFoto;

        /**
         * Standard constructor for ViewHolder.
         *
         * @param view
         */
        ViewHolder(View view) {
            super(view);
            csNameView = (TextView) view.findViewById(R.id.notification_cs_name_TextView);
            locAndTypeView = (TextView) view.findViewById(R.id.notification_cs_loc_and_type_TextView);
            createdOnView = (TextView) view.findViewById(R.id.notification_cs_createdOn_TextView);

            coffeeSortsView = (TextView) view.findViewById(R.id.notification_coffeeSorts_TextView);
            cityView = (TextView) view.findViewById(R.id.notification_city_TextView);
            siteFoto = (ImageView) view.findViewById(R.id.notification_csListFotoImageView);

            createdOnLinearLayout = view.findViewById(R.id.notification_created_on_linear_layout);
            locationAndSortsLinearLayout = view.findViewById(R.id.notification_location_and_sorts_linear_layout);

            coffeeSiteDataLayout = view.findViewById(R.id.notification_coffeesite_data_layout);
        }
    }

}
