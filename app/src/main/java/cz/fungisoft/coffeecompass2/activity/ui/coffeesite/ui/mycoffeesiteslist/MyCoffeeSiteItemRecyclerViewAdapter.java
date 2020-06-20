package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.AppCompatImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteServiceStatusOperationsListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteImageActivity;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
 * Adapter to show loaded list of CoffeeSites created by logged-in user
 */
public class MyCoffeeSiteItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                                                 implements CoffeeSiteServiceStatusOperationsListener {

    private static final String TAG = "MyCoffeeSiteListAdapter";

    /**
     * CoffeeSite selected by user from List to be working on
     */
    private CoffeeSite selectedCoffeeSite;
    private int selectedPosition;

    /**
     * To indicate, that update Author's comment function was selected by user.
     * Used when reacting to onUpdateCoffeeSite()  event from CoffeeSiteCUDOOperationsService,
     * which is processed in parrent MyCoffeeSitesListActivity.
     * Ensures, that only one correct Toast is shown, when reacting to the same event,
     * but in CreateCoffeeSiteActivity (which shows first Toast, that operation update succeeded).
     */
    private boolean updatingCommentOnly = false;

    public boolean isUpdatingCommentOnly() {
        return updatingCommentOnly;
    }

    /**
     * A coffeeSite returned from server after modification
     * i.e. activating, deactivating, canceling.
     * Used to compare with originally selectedCoffeeSite to be modified
     * we are ensuring, that modified CoffeeSite is the one intended for modification
     * in selectedCoffeeSite
     */
    private CoffeeSite modifiedCoffeeSite;

    private final MyCoffeeSitesListActivity mParentActivity;
    private List<CoffeeSite> mValues;

    /**
     * Used for opening CoffeeSiteDetailActivity
     */
    private View.OnClickListener mOnClickListenerToCoffeeSiteDetailActivityStart;

    /**
     * Used for opening CoffeeSiteImageActivity
     */
    private View.OnClickListener mOnClickListenerToCoffeeSiteImageActivityStart;

    /**
     * Used for changing status of selected CoffeeSite from/to ACTIVE/INACTIVE/CANCEL
     * statuses after click on respective action button by user.
     */
    private CoffeeSiteStatusChangeService coffeeSiteStatusChangeService;

    /**
     * Used for saving selected CoffeeSite after inserting CoffeeSite's creator initial coment
     */
    private CoffeeSiteCUDOperationsService coffeeSiteCUDOperationsService;

    /**
     * Request type to ask CreateCoffeeSiteActivity to edit CoffeeSite
     */
    static final int EDIT_COFFEESITE_REQUEST = 1;

    /**
     * Standard constructor of the class MyCoffeeSiteItemRecyclerViewAdapter
     *
     * @param parent - parent Activity for the Adapter, in this case this FoundCoffeeSitesListActivity
     * @param content - instance of the CoffeeSiteMovableListContent to be displayed by this activity
     */
    public MyCoffeeSiteItemRecyclerViewAdapter(MyCoffeeSitesListActivity parent,
                                               CoffeeSiteStatusChangeService coffeeSiteStatusChangeService,
                                               CoffeeSiteCUDOperationsService coffeeSiteCUDOperationsService,
                                               List<CoffeeSite> content) {
        mParentActivity = parent;
        mValues = content;

        mOnClickListenerToCoffeeSiteDetailActivityStart = createOnClickListenerForDetailActivityStart();

        mOnClickListenerToCoffeeSiteImageActivityStart = createOnClickListenerForShowImageActivityStart();

        this.coffeeSiteStatusChangeService = coffeeSiteStatusChangeService;
        if (this.coffeeSiteStatusChangeService != null) {
            this.coffeeSiteStatusChangeService.addCoffeeSiteStatusOperationsListener(this);
        }

        this.coffeeSiteCUDOperationsService = coffeeSiteCUDOperationsService;
    }

    /**
     * OnClick listener to open CoffeeSiteDetailActivity to show details
     * of the CoffeeSite as it is shown when searching and looking into the detailes.
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForDetailActivityStart() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSite item = (CoffeeSite) view.getTag();
                if (item != null) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) item);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }

    /**
     * OnClick listener to open CoffeeSiteDetailActivity to show details
     * of the CoffeeSite as it is shown when searching and looking into the detailes.
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForShowImageActivityStart() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSite item = (CoffeeSite) view.getTag();
                if (item != null) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteImageActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) item);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder retVal;
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mycoffeesite_list_content, parent, false);
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
        return mValues.size();
    }

    /**
     * Clears internal list of items and call update of View
     */
    public void clearList() {
        if (mValues != null) {
            int size = mValues.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    mValues.remove(0);
                }
                this.notifyItemRangeRemoved(0, size);
            }
        }
    }


    private void setupBasicViewHolder(int position, ViewHolder viewHolder) {

        viewHolder.csNameView.setText(this.mValues.get(position).getName());
        viewHolder.locAndTypeView.setText(this.mValues.get(position).getTypPodniku().toString() + ", " +  this.mValues.get(position).getTypLokality().toString());
        viewHolder.createdOnView.setText(this.mValues.get(position).getCreatedOnString());

        // Set default color for CoffeeSite Status view
        viewHolder.statusView.setTextColor(mParentActivity.getResources().getColor(R.color.site_status_gray));
        // Prelozeni jmen statusu CoffeeSitu
        String status = this.mValues.get(position).getStatusZaznamu().toString();
        switch(status)
        {
            case "ACTIVE":
                viewHolder.statusView.setTextColor(mParentActivity.getResources().getColor(R.color.colorPrimary));
                viewHolder.statusView.setText(R.string.status_active);
                break;
            case "INACTIVE":
                viewHolder.statusView.setText(R.string.status_inactive);
                break;
            case "CREATED":
                viewHolder.statusView.setText(R.string.status_created);
                viewHolder.statusView.setTextColor(mParentActivity.getResources().getColor(R.color.site_status_dark_gray));
                break;
            default:
                viewHolder.statusView.setText(this.mValues.get(position).getStatusZaznamu().toString());
        }

        if (!this.mValues.get(position).getMesto().isEmpty()) {
            viewHolder.cityView.setText(this.mValues.get(position).getMesto());
        }

        if (!this.mValues.get(position).getMainImageURL().isEmpty()) {
            Picasso.get().load(this.mValues.get(position).getMainImageURL()).into(viewHolder.siteFoto);
        } else {
            viewHolder.siteFoto.setImageDrawable(viewHolder.siteFoto.getContext().getResources().getDrawable(R.drawable.kafe_backround_120x160)); // @drawable/kafe_backround_120x160
        }

        // Set CoffeeSite instance of this RecyclerView item as a tag to all buttons
        // to be available later in button's onClick methods
        viewHolder.editCoffeeSiteButton.setTag(this.mValues.get(position));
        viewHolder.activateCoffeeSiteButton.setTag(this.mValues.get(position));
        viewHolder.deactivateCoffeeSiteButton.setTag(this.mValues.get(position));
        viewHolder.cancelCoffeeSiteButton.setTag(this.mValues.get(position));
        viewHolder.insertCommentButton.setTag(this.mValues.get(position));

        // Foto and main Label with CoffeeSite name are clickable
        viewHolder.siteFoto.setTag(this.mValues.get(position));
        viewHolder.siteFoto.setOnClickListener(mOnClickListenerToCoffeeSiteImageActivityStart);

        viewHolder.csNameView.setTag(this.mValues.get(position));
        viewHolder.csNameView.setOnClickListener(mOnClickListenerToCoffeeSiteDetailActivityStart);
        viewHolder.createdOnLinearLayout.setTag(this.mValues.get(position));
        viewHolder.createdOnLinearLayout.setOnClickListener(mOnClickListenerToCoffeeSiteDetailActivityStart);
        viewHolder.locationAndStatusLinearLayout.setTag(this.mValues.get(position));
        viewHolder.locationAndStatusLinearLayout.setOnClickListener(mOnClickListenerToCoffeeSiteDetailActivityStart);

        if (this.mValues.get(position).canBeActivated()) {
            viewHolder.activateCoffeeSiteButton.setEnabled(true);
        } else {
            viewHolder.activateCoffeeSiteButton.setEnabled(false);
        }
        setImageButtonEnabled(viewHolder.activateCoffeeSiteButton, mParentActivity.getDrawable(R.drawable.round_play_circle_outline_green_18) , viewHolder.activateCoffeeSiteButton.isEnabled());

        if (this.mValues.get(position).canBeDeactivated()) {
            viewHolder.deactivateCoffeeSiteButton.setEnabled(true);
        } else {
            viewHolder.deactivateCoffeeSiteButton.setEnabled(false);
        }
        setImageButtonEnabled(viewHolder.deactivateCoffeeSiteButton, mParentActivity.getDrawable(R.drawable.round_pause_circle_outline_black_18), viewHolder.deactivateCoffeeSiteButton.isEnabled());
    }

    /**
     * Show the dialog to confirm CoffeeSite cancelation
     */
    private void showConfirmDeleteAccountDialog() {
        // Create an instance of the dialog fragment and show it
        CancelCoffeeSiteDialogFragment dialog = new CancelCoffeeSiteDialogFragment();
        dialog.show(mParentActivity.getSupportFragmentManager(), "CancelCoffeeSiteDialogFragment");
    }

    /**
     * Show the dialog to insert CoffeeSite's author comment
     */
    private void showInsertAuthorsCommentDialog() {
        // Create an instance of the dialog fragment and show it
        //Passes CoffeeSite's author comment into the dialog
        InsertAuthorCommentDialogFragment dialog = newInstance(this.selectedCoffeeSite.getUvodniKoment());
        dialog.show(mParentActivity.getSupportFragmentManager(), "InsertAuthorCommentDialogFragment");
    }

    private static InsertAuthorCommentDialogFragment newInstance(String authorComment) {
        InsertAuthorCommentDialogFragment f = new InsertAuthorCommentDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("authorComment", authorComment);
        f.setArguments(args);

        return f;
    }

    /* ********************************************************************* */

    void onCancelCoffeeSiteDialogPositiveClick() {
        if (Utils.isOnline()) {
            if (coffeeSiteStatusChangeService != null) {
                mParentActivity.showProgressbarAndDisableMenuItems();
                coffeeSiteStatusChangeService.cancel(selectedCoffeeSite);
            }
        } else {
            Utils.showNoInternetToast(mParentActivity.getApplicationContext());
        }
    }

    void onCancelCoffeeSiteDialogNegativeClick() {
    }

    void onInsertAuthorCommentDialogPositiveClick(InsertAuthorCommentDialogFragment dialog) {
        if (dialog != null && !dialog.getAuthorComment().equals(selectedCoffeeSite.getUvodniKoment())) {
            if (Utils.isOnline()) {
                if (coffeeSiteStatusChangeService != null) {
                    mParentActivity.showProgressbarAndDisableMenuItems();
                    updatingCommentOnly = true;
                    selectedCoffeeSite.setUvodniKoment(dialog.getAuthorComment());
                    coffeeSiteCUDOperationsService.update(selectedCoffeeSite);
                }
            } else {
                Utils.showNoInternetToast(mParentActivity.getApplicationContext());
            }
        }
    }

    void onInsertAuthorCommentDialogNegativeClick(InsertAuthorCommentDialogFragment dialog) {
    }

    /* *************************************************** */

    @Override
    public void onCoffeeSiteActivated(CoffeeSite activeCoffeeSite, String error) {
        mParentActivity.hideProgressbarAndEnableMenuItems();
        modifiedCoffeeSite = activeCoffeeSite;

        Log.i(TAG, "Activation success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteActivateFailure(error);
        }
        else {
            showCoffeeSiteActivateSuccess();
            updateRecyclerViewCoffeeSiteActivated();
        }
    }

    @Override
    public void onCoffeeSiteDeactivated(CoffeeSite inactiveCoffeeSite, String error) {
        mParentActivity.hideProgressbarAndEnableMenuItems();
        modifiedCoffeeSite = inactiveCoffeeSite;

        Log.i(TAG, "Deactivate success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteDeactivateFailure(error);
        }
        else {
            showCoffeeSiteDeactivateSuccess();
            updateRecyclerViewCoffeeSiteDeactivated();
        }
    }

    @Override
    public void onCoffeeSiteCanceled(CoffeeSite canceledCoffeeSite, String error) {
        mParentActivity.hideProgressbarAndEnableMenuItems();
        modifiedCoffeeSite = canceledCoffeeSite;

        Log.i(TAG, "Cancel success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteCancelFailure(error);
        }
        else {
            showCoffeeSiteCancelSuccess();
            updateRecyclerViewItemRemoved();
        }
    }

        /**
         * Inner ViewHolder class for MyCoffeeSiteItemRecyclerViewAdapter
         */
        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView csNameView; // to show name of CoffeeSite
            final TextView locAndTypeView; // to show type of the CoffeeSite and location type
            final TextView createdOnView; // to show created date of this CoffeeSite
            final TextView statusView; // to show record status of the CoffeeSite
            final TextView cityView; // to show city name of the CoffeeSite
            /**
             * To insert listener to whole group of TextViews
             */
            final LinearLayout createdOnLinearLayout;
            final LinearLayout locationAndStatusLinearLayout;

            final ImageView siteFoto;

            /* Buttons */
            final AppCompatImageButton editCoffeeSiteButton;
            final AppCompatImageButton activateCoffeeSiteButton;
            final AppCompatImageButton deactivateCoffeeSiteButton;
            final AppCompatImageButton cancelCoffeeSiteButton;
            final AppCompatImageButton insertCommentButton;

            /**
             * Standard constructor for ViewHolder.
             *
             * @param view
             */
            ViewHolder(View view) {
                super(view);
                csNameView = (TextView) view.findViewById(R.id.cs_name_TextView);
                locAndTypeView = (TextView) view.findViewById(R.id.cs_loc_and_type_TextView);
                createdOnView = (TextView) view.findViewById(R.id.cs_createdOn_TextView);

                statusView = (TextView) view.findViewById(R.id.cs_status_TextView);
                cityView = (TextView) view.findViewById(R.id.city_TextView);
                siteFoto = (ImageView) view.findViewById(R.id.csListFotoImageView);

                editCoffeeSiteButton = view.findViewById(R.id.button_edit_coffeesite);
                editCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onEditButtonClick);

                activateCoffeeSiteButton = view.findViewById(R.id.button_activate_coffeesite);
                // set original icon
                activateCoffeeSiteButton.setImageResource(R.drawable.round_play_circle_outline_green_18);
                activateCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onActivateButtonClick);

                deactivateCoffeeSiteButton = view.findViewById(R.id.button_deactivate_coffeesite);
                deactivateCoffeeSiteButton.setImageResource(R.drawable.round_pause_circle_outline_black_18);
                deactivateCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onDeactivateButtonClick);

                cancelCoffeeSiteButton = view.findViewById(R.id.button_cancel_coffeesite);
                cancelCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onCancelButtonClick);

                insertCommentButton = view.findViewById(R.id.insert_creator_comment_ImageButton);
                insertCommentButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onInsertCommentButtonClick);

                createdOnLinearLayout = view.findViewById(R.id.created_on_linear_layout);
                locationAndStatusLinearLayout = view.findViewById(R.id.location_and_status_linear_layout);
            }
        }


    private CoffeeSite findCoffeeSiteFromListById(int id) {
        for (CoffeeSite coffeeSite : mValues) {
            if (coffeeSite.getId() == id) {
                return coffeeSite;
            }
        }
        return null;
    }

    /**
     * Called from parent activity (MyCoffeeSitesListActivity), when the CreateCoffeeSiteActivity
     * return result from above calling.
     * Updates RecyclerView with new CoffeeSite's data after editing
     *
     * @param editedCoffeeSite
     * @param position
     */
    public void updateEditedCoffeeSite(CoffeeSite editedCoffeeSite, int position) {
        this.selectedCoffeeSite = editedCoffeeSite;
        this.selectedPosition = position;
        mValues.set(selectedPosition, selectedCoffeeSite);
        this.notifyItemChanged(selectedPosition);
    }

    /**
     * Called from parent activity, if Author's comment was updated.
     * selected position is known from {@link #onInsertCommentButtonClick()}
     *
     * @param editedCoffeeSite
     */
    public void updateCoffeeSiteAfterCommentEdited(CoffeeSite editedCoffeeSite) {
        this.selectedCoffeeSite = editedCoffeeSite;
        updatingCommentOnly = false; // editing of Author's comment finished
        mValues.set(selectedPosition, selectedCoffeeSite);
        this.notifyItemChanged(selectedPosition);
    }

    /** ========== Buttons onClick methods =============== **/

    /** Assigned to buttons in mycoffeesite_list_content.xml editor **/

    public void onEditButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        Intent activityIntent = new Intent(mParentActivity, CreateCoffeeSiteActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) selectedCoffeeSite);
        activityIntent.putExtra("coffeeSitePosition", selectedPosition);

        mParentActivity.startActivityForResult(activityIntent, EDIT_COFFEESITE_REQUEST);
    }


    public void onActivateButtonClick(View v) {
        if (Utils.isOnline()) {
            selectedCoffeeSite = (CoffeeSite) v.getTag();
            selectedPosition = mValues.indexOf(selectedCoffeeSite);
            if (coffeeSiteStatusChangeService != null) {
                mParentActivity.showProgressbarAndDisableMenuItems();
                coffeeSiteStatusChangeService.activate(selectedCoffeeSite);
            }
        } else {
            Utils.showNoInternetToast(mParentActivity.getApplicationContext());
        }
    }

    public void onDeactivateButtonClick(View v) {
        if (Utils.isOnline()) {
            selectedCoffeeSite = (CoffeeSite) v.getTag();
            selectedPosition = mValues.indexOf(selectedCoffeeSite);
            if (coffeeSiteStatusChangeService != null) {
                mParentActivity.showProgressbarAndDisableMenuItems();
                coffeeSiteStatusChangeService.deactivate(selectedCoffeeSite);
            }
        } else {
            Utils.showNoInternetToast(mParentActivity.getApplicationContext());
        }
    }

    public void onCancelButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        showConfirmDeleteAccountDialog();
    }

    public void onInsertCommentButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        showInsertAuthorsCommentDialog();
    }

    /* ****************************************************************** */

    private void showMyCoffeeSitesLoadSuccess()
    {}

    private void showCoffeeSiteActivateSuccess() {
        Toast toast = Toast.makeText(mParentActivity.getApplicationContext(),
                R.string.coffeesite_activated_successfuly,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCoffeeSiteDeactivateSuccess() {
        Toast toast = Toast.makeText(mParentActivity.getApplicationContext(),
                R.string.coffeesite_deactivated_successfuly,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCoffeeSiteCancelSuccess() {
        Toast toast = Toast.makeText(mParentActivity.getApplicationContext(),
                R.string.coffeesite_canceled_successfuly,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showMyCoffeeSitesLoadFailure(String error) {
        error = !error.isEmpty() ? error : mParentActivity.getString(R.string.my_coffeesites_load_failure);
        Snackbar mySnackbar = Snackbar.make(mParentActivity.contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteCancelFailure(String error) {
        error = !error.isEmpty() ? error : mParentActivity.getString(R.string.coffee_site_cancel_failure);
        Snackbar mySnackbar = Snackbar.make(mParentActivity.contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteActivateFailure(String error) {
        error = !error.isEmpty() ? error : mParentActivity.getString(R.string.coffee_site_activate_failure);
        Snackbar mySnackbar = Snackbar.make(mParentActivity.contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteDeactivateFailure(String error) {
        error = !error.isEmpty() ? error : mParentActivity.getString(R.string.coffee_site_cancel_failure);
        Snackbar mySnackbar = Snackbar.make(mParentActivity.contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void updateRecyclerViewItemRemoved() {
        int position = mValues.indexOf(selectedCoffeeSite);
        if (position == selectedPosition
            && modifiedCoffeeSite != null && selectedCoffeeSite != null
            && modifiedCoffeeSite.getId() == selectedCoffeeSite.getId()) {
            mValues.remove(selectedCoffeeSite);
            this.notifyItemRemoved(selectedPosition);
        }
    }

    private void updateRecyclerViewCoffeeSiteActivated() {
        int position = mValues.indexOf(selectedCoffeeSite);
        if (position == selectedPosition
            && modifiedCoffeeSite != null && selectedCoffeeSite != null
            && modifiedCoffeeSite.getId() == selectedCoffeeSite.getId()) {

            mValues.set(selectedPosition, modifiedCoffeeSite);
            this.notifyItemChanged(selectedPosition);
        }
    }

    private void updateRecyclerViewCoffeeSiteDeactivated() {
        int position = mValues.indexOf(selectedCoffeeSite);
        if (position == selectedPosition
            && modifiedCoffeeSite != null && selectedCoffeeSite != null
            && modifiedCoffeeSite.getId() == selectedCoffeeSite.getId()) {
            mValues.set(selectedPosition, modifiedCoffeeSite);
            this.notifyItemChanged(selectedPosition);
        }
    }

    /**
     * Called by parent Activity when destroyed
     */
    public void onDestroy() {
        this.coffeeSiteStatusChangeService.removeCoffeeSiteStatusOperationsListener(this);
    }


    /**
     * Pomocne metody pro vykresleni neaktivnich icon na buttonu sedivou barvou
     * Viz Stackoverflow, kdy se da pouzit i selector v xml souboru
     * https://stackoverflow.com/questions/7228985/android-imagebutton-with-disabled-ui-feel/49162535#49162535
     */
    public static void setImageButtonEnabled(@NonNull final ImageView imageView, Drawable originalIcon,
                                             final boolean enabled) {
        imageView.setEnabled(enabled);
        imageView.setAlpha(enabled ? 1.0f : 0.3f);

        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        imageView.setImageDrawable(icon);
    }

    private static Drawable convertDrawableToGrayScale(@NonNull Drawable drawable) {
        final ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        final Drawable mutated = drawable.mutate();
        mutated.setColorFilter(filter);

        return mutated;
    }

}
