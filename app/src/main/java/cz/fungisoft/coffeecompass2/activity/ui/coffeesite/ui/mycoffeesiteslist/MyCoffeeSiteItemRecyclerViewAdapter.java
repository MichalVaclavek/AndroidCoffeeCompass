package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.AppCompatImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_ACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_CANCEL;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_DEACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_DELETE;

/**
 * Adapter to show found list of CoffeeSites created by logged-in user
 */
public class MyCoffeeSiteItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MyCoffeeSiteListAdapter";

    /**
     * CoffeeSite selected by user from List to be working on
     */
    private CoffeeSite selectedCoffeeSite;

    // A coffeeSite returned from server after modification
    // i.e. activating, deactivating, canceling
    // Used to compare with originaly selectedCoffeeSite to be modified
    // we are ensuring, that modified CoffeeSite is the one intended for modification
    // in selectedCoffeeSite
    private CoffeeSite modifiedCoffeeSite;
    private int selectedPosition;

    private final MyCoffeeSitesListActivity mParentActivity;
    private List<CoffeeSite> mValues;

    /**
     * Used for opening CoffeeSiteDetailActivity
     */
    private View.OnClickListener mOnClickListener;

    private CoffeeSiteServiceOperationsReceiver coffeeSiteServiceReceiver;

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
                                               List<CoffeeSite> content) {
        mParentActivity = parent;
        mValues = content;

        mOnClickListener = createOnClickListener();
        registerCoffeeSiteOperationsReceiver();
    }

    /**
     * OnClick listener to open CoffeeSiteDetailActivity to show details
     * of the CoffeeSite as it is shown when searching and looking into the detailes.
     *
     * @return
     */
    private View.OnClickListener createOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSite item = (CoffeeSite) view.getTag();
                if (item instanceof CoffeeSite) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) item);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder retVal = null;

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
                break;
            default:
                viewHolder.statusView.setText(this.mValues.get(position).getStatusZaznamu().toString());
        }

        if (!this.mValues.get(position).getMesto().isEmpty()) {
            viewHolder.cityView.setText(this.mValues.get(position).getMesto());
        }

        //viewHolder.siteFoto.setImageDrawable(null);
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

        // Foto and main Label with CoffeeSite name are clickable
        viewHolder.siteFoto.setTag(this.mValues.get(position));
        viewHolder.siteFoto.setOnClickListener(mOnClickListener);
        viewHolder.csNameView.setTag(this.mValues.get(position));
        viewHolder.csNameView.setOnClickListener(mOnClickListener);


        if (this.mValues.get(position).canBeActivated()) {
            viewHolder.activateCoffeeSiteButton.setEnabled(true);
        } else {
            viewHolder.activateCoffeeSiteButton.setEnabled(false);
        }
        setImageButtonEnabled(viewHolder.activateCoffeeSiteButton, viewHolder.activateCoffeeSiteButton.isEnabled());

        if (this.mValues.get(position).canBeDeactivated()) {
            viewHolder.deactivateCoffeeSiteButton.setEnabled(true);
        } else {
            viewHolder.deactivateCoffeeSiteButton.setEnabled(false);
        }
        setImageButtonEnabled(viewHolder.deactivateCoffeeSiteButton, viewHolder.deactivateCoffeeSiteButton.isEnabled());
    }

    /**
     * Show the dialog to confirm CoffeeSite cancelation
     */
    private void showConfirmDeleteAccountDialog() {
        // Create an instance of the dialog fragment and show it
        CancelCoffeeSiteDialogFragment dialog = new CancelCoffeeSiteDialogFragment();
        dialog.show(mParentActivity.getSupportFragmentManager(), "CancelCoffeeSiteDialogFragment");
    }

    public void onDialogPositiveClick() {
        startCoffeeSiteServiceOperation(selectedCoffeeSite, COFFEE_SITE_CANCEL);
    }

    public void onDialogNegativeClick() {
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

            final ImageView siteFoto;

            /* Buttons */
            final AppCompatImageButton editCoffeeSiteButton;
            final AppCompatImageButton activateCoffeeSiteButton;
            final AppCompatImageButton deactivateCoffeeSiteButton;
            final AppCompatImageButton cancelCoffeeSiteButton;

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
                activateCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onActivateButtonClick);

                deactivateCoffeeSiteButton = view.findViewById(R.id.button_deactivate_coffeesite);
                deactivateCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onDeactivateButtonClick);

                cancelCoffeeSiteButton = view.findViewById(R.id.button_cancel_coffeesite);
                cancelCoffeeSiteButton.setOnClickListener(MyCoffeeSiteItemRecyclerViewAdapter.this::onCancelButtonClick);
            }
        }

    /**
     * Starts operation with currently selected CoffeeSite
     *
     * @param coffeeSite
     * @param operation
     */
    private void startCoffeeSiteServiceOperation(CoffeeSite coffeeSite, int operation) {
        if (Utils.isOnline()) {
            mParentActivity.showProgressbarAndDisableMenuItems();

            Log.i(TAG, "startCoffeeSiteServiceOperation");
            Intent cfServiceIntent = new Intent();
            cfServiceIntent.setClass(mParentActivity, CoffeeSiteService.class);
            cfServiceIntent.putExtra("operation_type", operation);
            cfServiceIntent.putExtra("coffeeSite", (Parcelable) coffeeSite);
            mParentActivity.startService(cfServiceIntent);
            Log.i("CreateCoffeeSiteAct", "startCoffeeSiteServiceOperation, End");
        } else {
            Utils.showNoInternetToast(mParentActivity.getApplicationContext());
        }
    }

    /**
     *  Registering Results receiver from CoffeeSiteService
     **/
    private void registerCoffeeSiteOperationsReceiver() {
        Log.i(TAG, "registerCoffeeSiteOperationsReceiver() start");
        coffeeSiteServiceReceiver = new CoffeeSiteServiceOperationsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoffeeSiteService.COFFEE_SITE_STATUS);

        LocalBroadcastManager.getInstance(mParentActivity).registerReceiver(coffeeSiteServiceReceiver, intentFilter);
        Log.i(TAG, "registerCoffeeSiteOperationsReceiver() end");
    }

    /**
     * Receiver callbacks for CoffeeSiteService operations invoked earlier
     */
    private class CoffeeSiteServiceOperationsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mParentActivity.hideProgressbarAndEnableMenuItems();

            Log.i(TAG, "onReceive() start");
            String result = intent.getStringExtra("operationResult");
            String error = intent.getStringExtra("operationError");
            // This is not originaly selected CoffeeSite, but CoffeeSite with modified status
            modifiedCoffeeSite = intent.getParcelableExtra("coffeeSite");

            // We need to find original CoffeeSite from the list
            int operationType = intent.getIntExtra("operationType", 0);
            Log.i(TAG, "Result: " + result + ". Error: " + error + ". OperationType: " + operationType);

            switch (operationType) {

                case COFFEE_SITE_ACTIVATE: {
                    Log.i(TAG, "Activate result: " + result);
                    if (!error.isEmpty()) {
                        showCoffeeSiteActivateFailure(error);
                    }
                    else {
                        showCoffeeSiteActivateSuccess();
                        updateRecyclerViewCoffeeSiteActivated();
                    }
                }
                break;
                case COFFEE_SITE_DEACTIVATE: {
                    Log.i(TAG, "Deactivate result: " + result);
                    if (!error.isEmpty()) {
                        showCoffeeSiteDeactivateFailure(error);
                    }
                    else {
                        showCoffeeSiteDeactivateSuccess();
                        updateRecyclerViewCoffeeSiteDeactivated();
                    }
                }
                break;
                case COFFEE_SITE_CANCEL: {
                    Log.i(TAG, "Cancel result: " + result);
                    if (!error.isEmpty()) {
                        showCoffeeSiteCancelFailure(error);
                    }
                    else {
                        showCoffeeSiteCancelSuccess();
                        updateRecyclerViewItemRemoved();
                    }
                } break;
                case COFFEE_SITE_DELETE:{ //TODO in the future, when ADMIN User will be allowed to delete
                    Log.i(TAG, "Delete result: " + result);
                } break;
                default: break;
            }
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

    /** Buttons onClick methods **/
    /** Assigned to buttons in mycoffeesite_list_content.xml editor **/

    public void onEditButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        Intent activityIntent = new Intent(mParentActivity, CreateCoffeeSiteActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) selectedCoffeeSite);
        activityIntent.putExtra("coffeeSitePosition", selectedPosition);

        mParentActivity.startActivityForResult(activityIntent, EDIT_COFFEESITE_REQUEST);
    }


    /**
     * Called from parent activity (MyCoffeeSitesListActivity), when the CreateCoffeeSiteActivity
     * return result from above calling.
     * Update recyclerview with new CoffeeSite's data after editing
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


    public void onActivateButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        startCoffeeSiteServiceOperation(selectedCoffeeSite, COFFEE_SITE_ACTIVATE);
    }

    public void onDeactivateButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        startCoffeeSiteServiceOperation(selectedCoffeeSite, COFFEE_SITE_DEACTIVATE);
    }

    public void onCancelButtonClick(View v) {
        selectedCoffeeSite = (CoffeeSite) v.getTag();
        selectedPosition = mValues.indexOf(selectedCoffeeSite);
        showConfirmDeleteAccountDialog();
    }

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
            && modifiedCoffeeSite.getId() == selectedCoffeeSite.getId()) {
            mValues.remove(selectedCoffeeSite);
            this.notifyItemRemoved(selectedPosition);
        }
    }

    private void updateRecyclerViewCoffeeSiteActivated() {
        int position = mValues.indexOf(selectedCoffeeSite);
        if (position == selectedPosition
            && modifiedCoffeeSite.getId() == selectedCoffeeSite.getId()) {

            mValues.set(selectedPosition, modifiedCoffeeSite);
            this.notifyItemChanged(selectedPosition);
        }
    }

    private void updateRecyclerViewCoffeeSiteDeactivated() {
        int position = mValues.indexOf(selectedCoffeeSite);
        if (position == selectedPosition
            && modifiedCoffeeSite.getId() == selectedCoffeeSite.getId()) {
            mValues.set(selectedPosition, modifiedCoffeeSite);
            this.notifyItemChanged(selectedPosition);
        }
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(mParentActivity).unregisterReceiver(coffeeSiteServiceReceiver);
    }


    /** Pomocne metody pro vykresleni neaktivnich icon na buttonu sedivou barvou **/
    /* Viz Stackoverflow, kdy se da pouzit i selector v xml souboru */
    /* https://stackoverflow.com/questions/7228985/android-imagebutton-with-disabled-ui-feel/49162535#49162535 */
    public static void setImageButtonEnabled(@NonNull final ImageView imageView,
                                             final boolean enabled) {
        imageView.setEnabled(enabled);
        imageView.setAlpha(enabled ? 1.0f : 0.3f);

        final Drawable originalIcon = imageView.getDrawable();
        final Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
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
