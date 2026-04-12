package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.SelectLocationMapActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceStatusOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImageManageListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.CoffeeSiteCreateFormState;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.CoffeeSiteCreateModel;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.CoffeeSiteEntitiesViewModel;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.asynctask.image.GetImageObjectAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.image.ImageDeleteNewApiAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.image.ImageUploadNewApiAsyncTask;
import cz.fungisoft.coffeecompass2.entity.ImageFile;
import cz.fungisoft.coffeecompass2.entity.ImageObject;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.FileCompressor;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import io.reactivex.disposables.CompositeDisposable;

import static cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSiteItemRecyclerViewAdapter.EDIT_COFFEESITE_REQUEST;
import static cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSitesListActivity.CREATE_COFFEESITE_REQUEST;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService.StatusChangeOperation.COFFEE_SITE_ACTIVATE;


/**
 * Activity to show View, where user can enter data for a new CoffeeSite or
 * where the data of already created CoffeeSite can be edited.
 * It offers 2 functions in every "mode" (MODE_CREATE and MODE_MODIFY)
 * in BottomNavigationMenu.
 * <p>
 * The functions for MODE_CREATE are: Save, Save and Activate, Delete photo
 * The functions for MODE_MODIFY are: Save and Delete photo.
 * <p>
 * Implements many interfaces as many services and their results are to be observed.
 */
public class CreateCoffeeSiteActivity extends ActivityWithLocationService
                                      implements CoffeeSiteImageManageListener,
                                                 TimePickerFragment.EnterTimeDialogListener,
                                                 SaveActivateCoffeeSiteDialogFragment.SaveActivateCoffeeSiteDialogListener,
                                                 DeleteCoffeeSiteImageDialogFragment.DeleteCoffeeSiteImageDialogListener,
                                                 PropertyChangeListener,
                                                 CoffeeSiteServicesConnectionListener,
                                                 CoffeeSiteServiceCUDOperationsListener,
                                                 CoffeeSiteServiceStatusOperationsListener,
                                                 UserAccountServiceConnectionListener {

    private static final String TAG = "CreateCoffeeSiteAct";
    private static final String IMAGE_TYPE_MAIN = "main";
    private static final String IMAGE_TYPE_OTHER = "other";

    private CoffeeSiteEntitiesViewModel coffeeSiteEntitiesViewModel;
    private CoffeeSiteCreateModel createCoffeeSiteViewModel;


    @BindView(R.id.time_od_input_editText)
    TextInputEditText openingFromTimeEditText;
    @BindView(R.id.time_do_input_editText)
    TextInputEditText openingToTimeEditText;

    TimePickerFragment timeFromToPicker;

    @BindView(R.id.coffeesitename_input_edittext)
    TextInputEditText coffeeSiteNameEditText;
    @BindView(R.id.latitude_input_edittext)
    TextInputEditText latitudeEditText;
    @BindView(R.id.longitude_input_edittext)
    TextInputEditText longitudeEditText;

    // All other View input elements needed to create CoffeeSite
    @BindView(R.id.city_edittext)
    TextInputEditText cityEditText;
    @BindView(R.id.street_edittext)
    TextInputEditText streetEditText;

    @BindView(R.id.site_type_dropdown)
    AutoCompleteTextView sourceTypeEditText;
    @BindView(R.id.location_type_dropdown)
    AutoCompleteTextView locationTypeEditText;

    @BindView(R.id.site_type_input_layout)
    TextInputLayout sourceTypeTextInputLayout;
    @BindView(R.id.location_type_input_layout)
    TextInputLayout locationTypeTextInputLayout;

    @BindView(R.id.druhy_kavy_chip_group)
    ChipGroup coffeeSortsChipGroup;
    @BindView(R.id.dalsi_nabidka_chip_group)
    ChipGroup otherOfferChipGroup;

    @BindView(R.id.price_range_dropdown)
    AutoCompleteTextView priceRangeEditText;
    @BindView(R.id.otviracka_dny_dropdown)
    AutoCompleteTextView openingDaysEditText;

    @BindView(R.id.coffeeSite_imageView)
    ImageView siteFotoView;

    @BindView(R.id.google_map_icon_imageView)
    ImageView mapIconToOpenActivityView;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;

    MenuItem imageDeleteMenuItem;
    MenuItem saveMenuItem; // save and/or activate
    MenuItem manageImagesMenuItem;

    private Location currentLocation;
    private boolean firstLocationDetection = true;

    private static final long MAX_STARI_DAT = 1000 * 60;
    private static final float LAST_PRESNOST = 100.0f;

    /**
     * Location of the CoffeeSite selected in SelectLocationMapActivity
     */
    private LatLng selectedCoffeeSiteLocation;


    // UserAccountService binding
    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;
    private boolean mShouldUnbindUserAccountService;

    protected CoffeeSiteCUDOperationsService coffeeSiteCUDOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteCUDOperationsService> coffeeSiteCUDOperationsServiceConnector;

    protected CoffeeSiteStatusChangeService coffeeSiteStatusChangeService;
    private CoffeeSiteServicesConnector<CoffeeSiteStatusChangeService> coffeeSiteStatusChangeServiceConnector;

    /**
     * CoffeeSite being created or edited by this Activity
     */
    private CoffeeSite currentCoffeeSite;

    private CoffeeSite pendingImageOperationCoffeeSite;
    private boolean pendingImageDeleteLookup = false;

    // need to keep CoffeeSite id in case the CoffeeSite is only in phone DB, not saved on server,
    // and ID has to be changed to 0 before saving on server. After return from saving on server,
    // we need to restore original local CoffeeSie DB id for this CoffeeSite.
    private long currentCoffeeSiteID = 0;

    private int coffeeSitePositionInRecyclerView;

    private String[] SITE_TYPES_LOCAL; // array of CoffeeSite types read from local file
    private List<String> coffeeSiteTypesAvailable; // list of CoffeeSite types. either same as SITE_TYPES_LOCAL or values read from DB

    private String[] LOCATION_TYPES_LOCAL; // array of location types read from local file
    private List<String> siteLocationTypesAvailable; // list of location types. either same as LOCATION_TYPES_LOCAL or values read from DB

    /**
     * To detect if the CoffeeSite/Activity is in
     * mode for Creation of a new CoffeeSite or
     * if it modifies already created CoffeeSites.
     */
    public static final int MODE_CREATE = 0;
    public static final int MODE_MODIFY = 1;
    public static final int MODE_CREATE_FROM_MYCOFFEESITESACTIVITY = 2;
    public static final int MODE_MODIFY_FROM_DETAILACTIVITY = 3;

    /**
     * Default mode is create when called from MainActivity
     */
    private int mode = MODE_CREATE;

    /**
     * To indicate, that longitude and latitude TextViews should be entered using LocationService.
     * Used in both CREATE and MODIFY mode to block automatic overwriting of longitudeTextView or latitudeTextView,
     * when user is editing them.
     */
    private boolean locationEnterAutomaticMode = true; // default true i.e. when CREATE mode is on i.e. new CoffeeSite is being created

    /**
     * To indicate, that city and street TextViews should be entered using GeocodingService.
     * Used in both CREATE and MODIFY mode to block automatic overwriting of city/street textViews,
     * when user is editing them.<br>
     * This flag is usually changing exactly as locationEnterAutomaticMode flag, but we want to
     * keep them seaparatelly for possible future use.
     */
    private boolean cityOrStreetEnterAutomaticMode = true; // default true i.e. when CREATE mode is on i.e. new CoffeeSite is being created

    /**
     * To show snackbar
     */
    private View contextView;

    /**
     * Pomocny field k detekci, ze uzivatel vybral Ulozit a Activovat
     * novou lokaci. Slouzi k indikaci, ze po
     * ulozeni se musi zavolat jeste Aktivace a teprve potom
     * jit na senzam lokaci.
     */
    private boolean saveAndActivateRequested = false;

    /**
     * Request type to ask CreateCoffeeSiteActivity to create new CoffeeSite
     */
    static final int GET_COFFEESITE_LOCATION_REQUEST = 1;

    /**
     * Progress ba to be shown during saving of a new or
     * modified CoffeeSite and/or CofeeSite's Image
     */
    @BindView(R.id.create_modify_progressBar)
    ProgressBar saveCoffeeSiteProgressBar;

    private final CompositeDisposable mDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coffee_site);
        contextView = findViewById(R.id.coffeesite_create_main_scrollview);

        ButterKnife.bind(this);

        doBindUserAccountService();
        doBindCoffeeSiteCUDOperationsService();
        doBindCoffeeSiteStatusChangeService();

        coffeeSiteEntitiesViewModel = new CoffeeSiteEntitiesViewModel(getApplication());

        // Enable/disable bottom navigation menu items first, can be modifyed after CoffeeSite is read from bundle
        imageDeleteMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_foto_delete);
        imageDeleteMenuItem.setEnabled(false); // default
        saveMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_ulozit_and_or_aktivovat); // save and/or activate
        saveMenuItem.setEnabled(true);
        saveMenuItem.setTitle(R.string.coffeesite_save_menu_item);
        manageImagesMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_manage_images);
        manageImagesMenuItem.setEnabled(false); // enabled only in MODIFY mode for saved CoffeeSites

        mode = MODE_CREATE; // default mode (when Called from MainActivity)

        // Setup mode value according the way this Activity was called from other activities
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) { // When called from MyCoffeeSitesListActivity or from CoffeeSiteDetailActivity
            int requestCode = bundle.getInt("requestCode");
            if (requestCode == CREATE_COFFEESITE_REQUEST) {
                mode = MODE_CREATE_FROM_MYCOFFEESITESACTIVITY;
            }
            currentCoffeeSite = (CoffeeSite) bundle.getParcelable("coffeeSite");
            coffeeSitePositionInRecyclerView = bundle.getInt("coffeeSitePosition");
            // We are editing site here - insert input values to View from currentCoffeeSite
            // "Edit mode"
            if (currentCoffeeSite != null) {
                mode = MODE_MODIFY;
                if (coffeeSitePositionInRecyclerView == -1) { // called from CoffeeSiteDetailActivity
                    mode = MODE_MODIFY_FROM_DETAILACTIVITY; // this is to go back to CoffeeSiteDetailActivity if called from there
                }
                setupViewToModify(currentCoffeeSite);
            }
        }

        //*** Fill fixed data from DB - START ***
        //new InsertEntitiesDataFromDBAsyncTask().execute();
        showCoffeeSiteTypes();
        showLocationTypes();
        showPriceRanges();

        // Otviracka - dny
        String[] OTVIRACKA_DNY = getResources().getStringArray(R.array.otviracka_dny);
        final ArrayAdapter<String> otvirackaDnyAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                OTVIRACKA_DNY);

        AutoCompleteTextView otvirackaDnyDropdown = findViewById(R.id.otviracka_dny_dropdown);
        otvirackaDnyDropdown.setAdapter(otvirackaDnyAdapter);
        //*** Fill fixed data from DB - END ***

        siteFotoView.setOnClickListener(createSitePhotoViewOnClickListener());
        mapIconToOpenActivityView.setOnClickListener(mapIconToOpenActivityViewOnClickListener());

        createCoffeeSiteViewModel = new ViewModelProvider(this, new CoffeeSiteViewModelFactory(getApplication())).get(CoffeeSiteCreateModel.class);

        // Listener for selecting items from Bottom navigation menu
        BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_ulozit_and_or_aktivovat:

                                // Check if locationType and sourceType are selected
                                boolean errorInInput = false;
                                if (locationTypeEditText.getText().toString().isEmpty()) {
                                    locationTypeTextInputLayout.setError(getString(R.string.invalid_locationType));
                                    errorInInput = true;
                                }
                                if (sourceTypeEditText.getText().toString().isEmpty()) {
                                    sourceTypeTextInputLayout.setError(getString(R.string.invalid_coffeesite_type));
                                    errorInInput = true;
                                }

                                if (errorInInput) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.create_site_missing_attributes),
                                            Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                // Create CoffeeSite instance from model
                                if (mode == MODE_CREATE || mode == MODE_CREATE_FROM_MYCOFFEESITESACTIVITY) {
                                    if (Utils.isOnline(getApplicationContext())) {
                                        // Show dialog to choose, if the CoffeeSite should be only saved
                                        // or saved and activated
                                        currentCoffeeSite = createOrUpdateCoffeeSiteFromViewModel(null, coffeeSiteEntitiesViewModel);
                                        if (imagePhotoFile != null) {
                                            currentCoffeeSite.setMainImageFilePath(imagePhotoFile.getPath());
                                        }
                                        SaveActivateCoffeeSiteDialogFragment dialog = new SaveActivateCoffeeSiteDialogFragment();
                                        dialog.show(getSupportFragmentManager(), "SaveActivateCoffeeSiteDialogFragment");
                                    } else {
                                        // Create CoffeeSite in Offline mode and go to MyCoffeeSitesListActivity
                                        new CreateUpdateCoffeeSiteAsyncTask(coffeeSiteEntitiesViewModel).execute(currentCoffeeSite);
                                    }
                                }
                                // Modify CoffeeSite
                                if (mode == MODE_MODIFY || mode == MODE_MODIFY_FROM_DETAILACTIVITY) {
                                    if (Utils.isOnline(getApplicationContext())) {
                                        currentCoffeeSite = createOrUpdateCoffeeSiteFromViewModel(currentCoffeeSite, coffeeSiteEntitiesViewModel);
                                        if (imagePhotoFile != null) {
                                            currentCoffeeSite.setMainImageFilePath(imagePhotoFile.getPath());
                                        }
                                        // if it is update (and save) of the CoffeeSite, which is not saved on server yet,
                                        // then it is CREATE operation from server point of view.
                                        if (!currentCoffeeSite.isSavedOnServer() && !currentCoffeeSite.isStatusZaznamuAvailable()) {
                                            mode = MODE_CREATE_FROM_MYCOFFEESITESACTIVITY;
                                            currentCoffeeSite.saveId(); // save current DB id to be restored later, if save would fail
                                            currentCoffeeSite.setLastEditUserName(null);
                                            currentCoffeeSite.setHodnoceni(null);
                                            saveCoffeeSite(currentCoffeeSite);
                                        } else {
                                            updateCoffeeSite(currentCoffeeSite);
                                        }
                                    } else {
                                        // Modify CoffeeSite in Offline mode
                                        new CreateUpdateCoffeeSiteAsyncTask(coffeeSiteEntitiesViewModel).execute(currentCoffeeSite);
                                    }
                                }
                                return true;

                            case R.id.navigation_foto_delete:
                                // Show dialog to choose if the CoffeeSite should be cancelled or not
                                DeleteCoffeeSiteImageDialogFragment dialog = new DeleteCoffeeSiteImageDialogFragment();
                                dialog.show(getSupportFragmentManager(), "DeleteCoffeeSiteImageDialogFragment");
                                return true;

                            case R.id.navigation_manage_images:
                                openManageImagesActivity();
                                return true;
                        }
                        return false;
                    }
        };

        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        // Input for CoffeeSite name, Longitude and Latitude validation
        createCoffeeSiteViewModel.getCoffeeSiteFormState().observe(this, new Observer<CoffeeSiteCreateFormState>() {

            @Override
            public void onChanged(@Nullable CoffeeSiteCreateFormState createCoffeeSiteFormState) {
                if (createCoffeeSiteFormState == null) {
                    return;
                }

                MenuItem saveMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_ulozit_and_or_aktivovat); // save
                saveMenuItem.setEnabled(createCoffeeSiteFormState.isDataValid());

                if (createCoffeeSiteFormState.getCoffeeSiteNameError() != null) {
                    coffeeSiteNameEditText.setError(getString(createCoffeeSiteFormState.getCoffeeSiteNameError()));
                } else {
                    coffeeSiteNameEditText.setError(null); // clears error icon and text
                }
                if (createCoffeeSiteFormState.getLatitudeError() != null) {
                    latitudeEditText.setError(getString(createCoffeeSiteFormState.getLatitudeError()));
                } else {
                    latitudeEditText.setError(null); // clears error icon and text
                }
                if (createCoffeeSiteFormState.getLongitudeError() != null) {
                    longitudeEditText.setError(getString(createCoffeeSiteFormState.getLongitudeError()));
                } else {
                    longitudeEditText.setError(null); // clears error icon and text
                }
            }
        });

        /**
         * Common TextWatcher to watch changes in mandatory input fields
         */
        TextWatcher afterTextChangedListener = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            /**
             * Validates input in createCoffeeSiteViewModel
             * @param s
             */
            @Override
            public void afterTextChanged(Editable s) {

                createCoffeeSiteViewModel.coffeeSiteDataChanged(coffeeSiteNameEditText.getText().toString(),
                        longitudeEditText.getText().toString(), latitudeEditText.getText().toString());

                if (!locationTypeEditText.getText().toString().isEmpty()) {
                    locationTypeTextInputLayout.setError(null);
                }
                if (!sourceTypeEditText.getText().toString().isEmpty()) {
                    sourceTypeTextInputLayout.setError(null);
                }
            }
        };

        coffeeSiteNameEditText.addTextChangedListener(afterTextChangedListener);
        longitudeEditText.addTextChangedListener(afterTextChangedListener);
        latitudeEditText.addTextChangedListener(afterTextChangedListener);
        locationTypeEditText.addTextChangedListener(afterTextChangedListener);
        sourceTypeEditText.addTextChangedListener(afterTextChangedListener);

        // Listeners to inputTextFields to ensure keyboard hiding when focus lost
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        coffeeSiteNameEditText.setOnFocusChangeListener(ofcListener);

        /*
         * Special TextWatcher to watch if a user entered manually longitudeTextView or latitudeTextView
         */
        TextWatcher afterLongLatTextViewChangedListener = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Detect if the input of longitude or latitude was changed by setText or by user
                if (longitudeEditText.getTag() == null || latitudeEditText.getTag() == null) {
                    // Value changed by user, because before setText() programmatically entered, the tag
                    // is set to a special value.
                    // means from now on, ignore values entered programmatically in showCurrentLocationInView()
                    // as user wants to enter values manually
                    // but if both  textInputs are cleared, allow automatic enter again
                    locationEnterAutomaticMode = longitudeEditText.getText().toString().isEmpty() && latitudeEditText.getText().toString().isEmpty();
                    cityOrStreetEnterAutomaticMode = locationEnterAutomaticMode; // only if location is deleted by user, then automatic city/street resolving is enabled
                }
            }
        };

        latitudeEditText.addTextChangedListener(afterLongLatTextViewChangedListener);
        longitudeEditText.addTextChangedListener(afterLongLatTextViewChangedListener);

        latitudeEditText.setOnFocusChangeListener(ofcListener);
        longitudeEditText.setOnFocusChangeListener(ofcListener);

        /*
         * Special TextWatcher to watch if a user entered manually cityTextView or streetTextView
         */
        TextWatcher afterCityOrStreetTextViewChangedListener = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Detect if the input of city and street name was changed by setText or by user
                if (cityEditText.getTag() == null || streetEditText.getTag() == null) {
                    // Value changed by user, because before setText() programmatically entered, the tag
                    // user wants to change text - don't change it automatically.
                    // only if longitude/latitude TextView is deleted completely, city and street can be changed automatically
                    cityOrStreetEnterAutomaticMode = false;
                }
            }
        };

        cityEditText.addTextChangedListener(afterCityOrStreetTextViewChangedListener);
        streetEditText.addTextChangedListener(afterCityOrStreetTextViewChangedListener);

        cityEditText.setOnFocusChangeListener(ofcListener);
        streetEditText.setOnFocusChangeListener(ofcListener);

        openingFromTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingFromTimeEditText, event));
        openingToTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingToTimeEditText, event));

        // To compress image/photo files of the CoffeeSite
        fileCompressor = new FileCompressor(this);
        // To detect, that user did not choose new image file yet
        imagePhotoFile = null;
    }

    /**
     * Helper method to setup Activity view when modifying
     */
    private void setupViewToModify(CoffeeSite coffeeSite) {
        if (coffeeSite == null) {
            return;
        }
        locationEnterAutomaticMode = false; // don't change location automatically. wait until user deletes all location info.
        cityOrStreetEnterAutomaticMode = false; // don't change city/street info automatically. wait until user deletes all location info.
        restoreLocalImagesFromCoffeeSite(coffeeSite);
        fillViewWithCoffeeSiteData(coffeeSite);

        saveMenuItem.setEnabled(true);
        saveMenuItem.setTitle(R.string.save_coffeesite_updated);
        currentImageCount = !localImagePaths.isEmpty() ? localImagePaths.size() : (hasMainImage() ? 1 : 0);
        refreshImageActionState();
        refreshImageCountIfNeeded();
    }

    /** Operations to start Photo or Image capture Intents and saving the result **** START ***/

    /**
     * OnClickListener for sitePhotoView
     */
    private View.OnClickListener createSitePhotoViewOnClickListener() {
        View.OnClickListener retVal;

        retVal = view -> {
            if (hasMainImage()) {
                if (canOpenManageImages()) {
                    openManageImagesActivity();
                }
            } else {
                selectImage();
            }
        };
        return retVal;
    }

    /**
     * OnClickListener for mapIconToOpenActivityView ImageView
     * Opens {@link SelectLocationMapActivity}
     */
    private View.OnClickListener mapIconToOpenActivityViewOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectLocationMapActivity();
            }
        };
        return retVal;
    }

    static final int REQUEST_TAKE_PHOTO = 100;
    static final int REQUEST_GALLERY_PHOTO = 101;
    static final int REQUEST_MANAGE_IMAGES = 102;

    private File imagePhotoFile;
    private FileCompressor fileCompressor;
    private int currentImageCount = 0;
    private final ArrayList<String> localImagePaths = new ArrayList<>();
    private final ArrayList<File> pendingCreateImageUploads = new ArrayList<>();
    private CoffeeSite pendingCreatedCoffeeSite;
    private int pendingCreateImageUploadIndex = -1;
    private int pendingCreateImageUploadFailures = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    // Compress taken photo
                    try {
                        imagePhotoFile = fileCompressor.compressToFile(imagePhotoFile);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to compress photo file from camera.");
                    }
                    showSelectedMainImage(imagePhotoFile);
                }
                break;

                case REQUEST_GALLERY_PHOTO: {
                    Uri selectedImage = data.getData();
                    try {
                        File selectedImageFile = createImageFileFromUri(selectedImage);
                        if (selectedImageFile != null) {
                            imagePhotoFile = fileCompressor.compressToFile(selectedImageFile);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to process image file from gallery.", e);
                    }
                    showSelectedMainImage(imagePhotoFile);
                }
                break;

                case REQUEST_MANAGE_IMAGES: {
                    handleManagedImagesResult(data);
                }
                break;

                // Check which request we're responding to
                case GET_COFFEESITE_LOCATION_REQUEST: {
                    // It can changed when called from SelectedLocationMapActivity
                    selectedCoffeeSiteLocation = (LatLng) data.getExtras().get("selectedLocation");
                    // Location selected by user
                    // stop automatic location refresh in the respective TextInputs
                    // and insert selected coordinates in the input
                    if (selectedCoffeeSiteLocation != null) {
                        showLocationInView(selectedCoffeeSiteLocation.latitude, selectedCoffeeSiteLocation.longitude);
                        showCityStreetInView(selectedCoffeeSiteLocation.latitude, selectedCoffeeSiteLocation.longitude);
                        locationEnterAutomaticMode = false;
                        cityOrStreetEnterAutomaticMode = false;
                }
                break;
                }
                default: break;
            }
        }
    }

    /**
     * Creates empty file to store taken photo or selected picture from Gallery
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,  /* prefix */
                                   ".jpg",         /* suffix */
                                        storageDir      /* directory */
        );

        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Create image file failed.", ex);
        }
        if (photoFile != null) {
            imagePhotoFile = photoFile;
            Uri photoURI = FileProvider.getUriForFile(this,
                    getString(R.string.file_provider),
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.setClipData(ClipData.newRawUri("", photoURI));
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } catch (ActivityNotFoundException | SecurityException ex) {
                Log.e(TAG, "No camera app available for image capture.", ex);
                Toast.makeText(this, R.string.no_camera_available, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Operations to start Photo or Image capture Intents and saving the result **** END ***/

    /**
     * Select image from gallery
     */
    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickPhoto.addCategory(Intent.CATEGORY_OPENABLE);
        pickPhoto.setType("image/*");
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickPhoto.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    @Nullable
    private File createImageFileFromUri(@Nullable Uri imageUri) throws IOException {
        if (imageUri == null) {
            return null;
        }

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            File destinationFile = createImageFile();
            outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return destinationFile;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageCountIfNeeded();
    }

    @Override
    protected void onStop() {
        mDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        doUnbindUserAccountService();
        doUnbindCoffeeSiteCUDOperationsService();
        doUnbindCoffeeSiteStatusChangeService();
        super.onDestroy();
    }

    // -------- UserAccountService binding --------

    private void doBindUserAccountService() {
        userAccountServiceConnector = new UserAccountServiceConnector();
        userAccountServiceConnector.addUserAccountServiceConnectionListener(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserAccountService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserAccountService() {
        if (mShouldUnbindUserAccountService) {
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserAccountService = false;
        }
    }

    @Override
    public void onUserAccountServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        Log.i(TAG, "UserAccountService connected.");
    }

    /** Helper methods to correctly display lists of CoffeeSites properties/vlastnosti  **/
    /** which can be selected by user during new CoffeeSite creation **/

    /* ----- START ------- */

    private void showCoffeeSiteTypes() {
        // Typ zdroje
        SITE_TYPES_LOCAL = getResources().getStringArray(R.array.coffee_site_type);
        coffeeSiteTypesAvailable =  new ArrayList<>(Arrays.asList(SITE_TYPES_LOCAL));
        ArrayAdapter<String> siteTypesAdapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popoup_item,
                coffeeSiteTypesAvailable); // default values

        coffeeSiteEntitiesViewModel.getAllCoffeeSiteTypes().observe(this, new Observer<List<CoffeeSiteType>>() {
            @Override
            public void onChanged(@Nullable final List<CoffeeSiteType> coffeeSiteTypes) {
                // Update the cached copy of the words in the adapter by values from DB.
                coffeeSiteTypesAvailable.clear();
                for (CoffeeSiteType cst : coffeeSiteTypes) {
                    coffeeSiteTypesAvailable.add(cst.getCoffeeSiteType());
                }
            }
        });

        AutoCompleteTextView siteTypeDropdown = findViewById(R.id.site_type_dropdown);
        siteTypeDropdown.setAdapter(siteTypesAdapter);
        siteTypeDropdown.setValidator(new SiteTypeValidator());
        siteTypeDropdown.setOnFocusChangeListener(new SiteTypeInputFocusListener());
    }

    private void showLocationTypes() {
        // Typ lokality
        LOCATION_TYPES_LOCAL = getResources().getStringArray(R.array.location_type);
        siteLocationTypesAvailable = new ArrayList<>(Arrays.asList(LOCATION_TYPES_LOCAL));
        ArrayAdapter<String> locationTypesAdapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popoup_item,
                siteLocationTypesAvailable); // default values

        coffeeSiteEntitiesViewModel.getAllSiteLocationTypes().observe(this, new Observer<List<SiteLocationType>>() {
            @Override
            public void onChanged(@Nullable final List<SiteLocationType> siteLocationTypes) {
                // Update the cached copy of the words in the adapter by values from DB.
                siteLocationTypesAvailable.clear();
                for (SiteLocationType cslt : siteLocationTypes) {
                    siteLocationTypesAvailable.add(cslt.getLocationType());
                }
            }
        });

        AutoCompleteTextView locationTypesDropdown = findViewById(R.id.location_type_dropdown);
        locationTypesDropdown.setAdapter(locationTypesAdapter);
        locationTypesDropdown.setValidator(new LocationTypeValidator());
        locationTypesDropdown.setOnFocusChangeListener(new LocationTypeInputFocusListener());
    }

    private void showPriceRanges() {
        // Cenovy rozsah
        String[] PRICE_RANGES = getResources().getStringArray(R.array.cena_range);
        ArrayAdapter<String> priceRangesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                new ArrayList<>(Arrays.asList(PRICE_RANGES)));

        coffeeSiteEntitiesViewModel.getAllPriceRanges().observe(this, new Observer<List<PriceRange>>() {
            @Override
            public void onChanged(@Nullable final List<PriceRange> priceRanges) {
                // Update the cached copy of the words in the adapter.
                priceRangesAdapter.clear();
                for (PriceRange priceRange : priceRanges) {
                    priceRangesAdapter.add(priceRange.getPriceRange());
                }
            }
        });

        AutoCompleteTextView priceRangesDropdown = findViewById(R.id.price_range_dropdown);
        priceRangesDropdown.setAdapter(priceRangesAdapter);
    }

    /** Helper methods to correctly display lists of CoffeeSites properties/vlastnosti  **/
    /** which can be selected by user during new CoffeeSite creation **/

    /* ----- END ------- */


    /********* CoffeeSiteCUDOperationsService **************/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteCUDOperationsService;

    private void doBindCoffeeSiteCUDOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        coffeeSiteCUDOperationsServiceConnector = new CoffeeSiteServicesConnector<>();
        coffeeSiteCUDOperationsServiceConnector.addCoffeeSiteServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteCUDOperationsService.class),
                coffeeSiteCUDOperationsServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteCUDOperationsService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteLoadOperationsService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }


    private void doUnbindCoffeeSiteCUDOperationsService() {
        if (mShouldUnbindCoffeeSiteCUDOperationsService) {
            if (coffeeSiteCUDOperationsService != null) {
                coffeeSiteCUDOperationsService.removeCUDOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteCUDOperationsServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteCUDOperationsServiceConnector);
            mShouldUnbindCoffeeSiteCUDOperationsService = false;
        }
    }

    @Override
    public void onCoffeeSiteServiceConnected() {
        if (coffeeSiteCUDOperationsServiceConnector.getCoffeeSiteService() != null) {
            if (coffeeSiteCUDOperationsService == null) {
                coffeeSiteCUDOperationsService = coffeeSiteCUDOperationsServiceConnector.getCoffeeSiteService();
                coffeeSiteCUDOperationsService.addCUDOperationsListener(this);
            }
        }
        if (coffeeSiteStatusChangeServiceConnector.getCoffeeSiteService() != null) {
            if (coffeeSiteStatusChangeService == null) {
                coffeeSiteStatusChangeService = coffeeSiteStatusChangeServiceConnector.getCoffeeSiteService();
                coffeeSiteStatusChangeService.addCoffeeSiteStatusOperationsListener(this);
            }
        }
    }

    /********* CoffeeSiteStatusChangeService **************/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteStatusChangeService;

    private void doBindCoffeeSiteStatusChangeService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        coffeeSiteStatusChangeServiceConnector = new CoffeeSiteServicesConnector<>();
        coffeeSiteStatusChangeServiceConnector.addCoffeeSiteServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteStatusChangeService.class),
                coffeeSiteStatusChangeServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteStatusChangeService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteLoadOperationsService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindCoffeeSiteStatusChangeService() {
        if (mShouldUnbindCoffeeSiteStatusChangeService) {
            if (coffeeSiteStatusChangeService != null) {
                coffeeSiteStatusChangeService.removeCoffeeSiteStatusOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteStatusChangeServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteStatusChangeServiceConnector);
            mShouldUnbindCoffeeSiteStatusChangeService = false;
        }
    }


    /** Listener methods for Dialogs **/

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof SaveActivateCoffeeSiteDialogFragment) {
            saveCoffeeSiteAndActivate(currentCoffeeSite);
        }
        if (dialog instanceof DeleteCoffeeSiteImageDialogFragment) {
            if (imagePhotoFile != null) {
                imagePhotoFile.delete();
                imagePhotoFile = null;
                localImagePaths.clear();
                syncCurrentCoffeeSiteLocalImages();
                siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_outline_add_photo_alternate_36));
            }

            if (currentCoffeeSite != null) {
                if (!currentCoffeeSite.getMainImageFilePath().isEmpty()) {
                    ImageUtil.deleteCoffeeSiteImage(getApplicationContext(), currentCoffeeSite);
                    currentCoffeeSite.setMainImageFilePath("");
                    if (isLocalOnlyDraft(currentCoffeeSite)) {
                        currentCoffeeSite.setMainImageURL("");
                    }
                    currentImageCount = 0;
                    siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                            R.drawable.ic_outline_add_photo_alternate_36));
                    refreshImageActionState();
                }

                if (!currentCoffeeSite.getMainImageURL().isEmpty()) {
                    if (Utils.isOnline(getApplicationContext())) {
                        deleteCoffeeSiteImage(currentCoffeeSite);
                    } else {
                        // set a default icon to the siteFotoView
                        siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_outline_add_photo_alternate_36));
                        Utils.showNoInternetToast(getApplicationContext());
                    }
                } else if (currentCoffeeSite.getMainImageFilePath().isEmpty()) {
                    currentImageCount = 0;
                    refreshImageActionState();
                }
            } else {
                currentImageCount = 0;
                refreshImageActionState();
            }
        }
    }

    /**
     * Negative and Neutral are swapped here as we require to have
     * Neutral item as the second one in the Dialog's options list.
     *
     * @param dialog
     */
    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof SaveActivateCoffeeSiteDialogFragment) {
            saveCoffeeSite(currentCoffeeSite);
        }
    }

    /* ---- Methods to invoke CoffeeSIteService operation based on user's selection */

    /**
     * Entry point for actions selected by user in BottomNavigationMenu
     *
     * @param coffeeSite
     */
    private void saveCoffeeSite(CoffeeSite coffeeSite) {
        showProgressbarAndDisableMenuItems();
        saveAndActivateRequested = false;
        if (coffeeSiteCUDOperationsService != null) {
            // Set new CoffeeSite ID to 0 before saving on server
            coffeeSite.setId("");
            coffeeSiteCUDOperationsService.save(coffeeSite);
        }
    }

    private void saveCoffeeSiteToDB(CoffeeSite coffeeSite) {
        if (coffeeSiteCUDOperationsService != null) {
            syncCurrentCoffeeSiteLocalImages(coffeeSite);
            coffeeSiteCUDOperationsService.saveToDB(coffeeSite);
        }
    }

    private void updateCoffeeSiteInDB(CoffeeSite coffeeSite) {
        if (coffeeSiteCUDOperationsService != null) {
            syncCurrentCoffeeSiteLocalImages(coffeeSite);
            coffeeSiteCUDOperationsService.updateInDB(coffeeSite);
        }
    }

    private void saveCoffeeSiteAndActivate(CoffeeSite coffeeSite) {
        showProgressbarAndDisableMenuItems();
        saveAndActivateRequested = true;
        if (coffeeSiteCUDOperationsService != null) {
            coffeeSiteCUDOperationsService.save(coffeeSite);
        }
    }

    private void activateCoffeeSite(CoffeeSite coffeeSite) {
        showProgressbarAndDisableMenuItems();
        if (coffeeSiteStatusChangeService != null) {
            coffeeSiteStatusChangeService.activate(coffeeSite);
        }
    }

    /**
     * Main, starting update method called upon users click
     *
     * @param coffeeSite
     */
    private void updateCoffeeSite(CoffeeSite coffeeSite) {
        showProgressbarAndDisableMenuItems();
        // If there is a photoFile, save it first to be available
        // after CoffeeSite is returned after it's update
        if (imagePhotoFile != null) {
            uploadCoffeeSiteImage(imagePhotoFile, coffeeSite);
        } else {
            if (coffeeSiteCUDOperationsService != null) {
                coffeeSiteCUDOperationsService.update(coffeeSite);
            }
       }
    }

    private void deleteCoffeeSiteImage(CoffeeSite coffeeSite) {
        showProgressbarAndDisableMenuItems();
        pendingImageOperationCoffeeSite = coffeeSite;
        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            hideProgressbarAndEnableMenuItems();
            Toast.makeText(this, R.string.toast_new_login_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (coffeeSite == null || coffeeSite.getId().isEmpty()) {
            hideProgressbarAndEnableMenuItems();
            return;
        }
        ImageUtil.deleteCoffeeSiteImage(getApplicationContext(), coffeeSite);
        pendingImageDeleteLookup = true;
        new GetImageObjectAsyncTask(this, coffeeSite.getId()).execute();
    }

    private void uploadCoffeeSiteImage(File imageFile, CoffeeSite coffeeSite) {
        uploadCoffeeSiteImage(imageFile, coffeeSite, IMAGE_TYPE_MAIN);
    }

    private void uploadCoffeeSiteImage(File imageFile, CoffeeSite coffeeSite, String imageType) {
        showProgressbarAndDisableMenuItems();
        pendingImageOperationCoffeeSite = coffeeSite;
        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            hideProgressbarAndEnableMenuItems();
            Toast.makeText(this, R.string.toast_new_login_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageFile != null && imageFile.exists() && coffeeSite != null) {
            new ImageUploadNewApiAsyncTask(this, userAccountService, imageFile,
                    coffeeSite.getId(), imageType).execute();
        } else {
            hideProgressbarAndEnableMenuItems();
        }
    }

    private String buildMainImageUrl(String coffeeSiteId) {
        if (coffeeSiteId == null || coffeeSiteId.isEmpty()) {
            return "";
        }
        return BuildConfig.IMAGES_API_PUBLIC_URL
                + "bytes/object/?objectExtId=" + coffeeSiteId
                + "&type=main&size=mid";
    }

    @Nullable
    private ImageFile getMainImageFile(ImageObject imageObject) {
        if (imageObject == null || imageObject.getObjectImages().isEmpty()) {
            return null;
        }
        for (ImageFile imageFile : imageObject.getObjectImages()) {
            if (imageFile != null && "main".equalsIgnoreCase(imageFile.getImageType())) {
                return imageFile;
            }
        }
        return imageObject.getObjectImages().get(0);
    }

    /* ====== CALLBACK methods after REST call AsyncTasks finished ============= */

    /**
     * Callback after new CoffeeSite was saved.
     *
     * @param savedCoffeeSite
     * @param error
     */
    @Override
    public void onCoffeeSiteSaved(CoffeeSite savedCoffeeSite, String error) {
        hideProgressbarAndEnableMenuItems();
        Log.i(TAG, "Save OK?: " + error.isEmpty());

        if (error.isEmpty()) {
            // New CoffeeSite saved successfully
            showCoffeeSiteOperationSuccess(CoffeeSiteCUDOperationsService.CUDOperation.COFFEE_SITE_SAVE, "OK");

            if (startPendingCreateImageUploads(savedCoffeeSite)) {
                return;
            }
            finishCoffeeSiteCreateAfterSave(savedCoffeeSite);
        } else { // There was error saving CoffeeSite
            clearPendingCreateImageUploads();
            // Was the the current/edited CoffeeSite previously saved in DB because of Offline mode?
            // restore original, phone's DB, id of the edited CoffeeSite,
            // which has to be changed to 0 before saving on server
            // original ID is needed to edit further if saving failed
            if (!currentCoffeeSite.isSavedOnServer() && !currentCoffeeSite.isStatusZaznamuAvailable()) {
                currentCoffeeSite.restoreId();
            }
            showCoffeeSiteCreateFailure(error);
        }
    }

    @Override
    public void onCoffeeSiteUpdated(CoffeeSite updatedCoffeeSite, String error) {
        hideProgressbarAndEnableMenuItems();
        Log.i(TAG, "Update success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteUpdateFailure(error);
        }
        else {
            syncUpdatedCoffeeSiteImageState(updatedCoffeeSite);
            // successful return from CoffeeSite update REST call
            // If the CoffeeSite is updated, then it's image is updated too, we can proceed to MyCoffeeSitesListActivity
            showCoffeeSiteOperationSuccess(CoffeeSiteCUDOperationsService.CUDOperation.COFFEE_SITE_UPDATE, "OK");
            goBackAfterUpdate(updatedCoffeeSite);
        }
    }

    @Override
    public void onCoffeeSiteActivated(CoffeeSite activeCoffeeSite, String error) {
        hideProgressbarAndEnableMenuItems();
        Log.i(TAG, "Activate success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteActivateFailure(error);
        } else {
            showCoffeeSiteStatusChangeSuccess(COFFEE_SITE_ACTIVATE, "OK");
        }
        saveAndActivateRequested = false;
        goToMyCoffeeSitesActivity();
    }


    /** Methods to be called after Images API operations finished **/

    @Override
    public void onImageObjectLoaded(ImageObject imageObject) {
        currentImageCount = imageObject != null ? imageObject.getObjectImages().size() : 0;
        refreshImageActionState();
        if (!pendingImageDeleteLookup) {
            if (currentImageCount == 0) {
                if (currentCoffeeSite != null) {
                    currentCoffeeSite.setMainImageURL("");
                    currentCoffeeSite.setMainImageFilePath("");
                }
                imagePhotoFile = null;
                localImagePaths.clear();
                syncCurrentCoffeeSiteLocalImages();
                siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.ic_outline_add_photo_alternate_36));
            } else {
                syncCurrentCoffeeSiteMainImage(imageObject);
            }
            return;
        }
        pendingImageDeleteLookup = false;
        if (imageObject == null || imageObject.getObjectImages().isEmpty()) {
            hideProgressbarAndEnableMenuItems();
            Toast.makeText(this, R.string.manage_images_delete_failure, Toast.LENGTH_SHORT).show();
            pendingImageOperationCoffeeSite = null;
            return;
        }

        ImageFile mainImage = getMainImageFile(imageObject);
        if (mainImage == null || mainImage.getExternalId() == null || mainImage.getExternalId().isEmpty()) {
            hideProgressbarAndEnableMenuItems();
            Toast.makeText(this, R.string.manage_images_delete_failure, Toast.LENGTH_SHORT).show();
            pendingImageOperationCoffeeSite = null;
            return;
        }

        String objectExtId = imageObject.getExternalObjectId();
        if (objectExtId == null || objectExtId.isEmpty()) {
            objectExtId = pendingImageOperationCoffeeSite != null ? pendingImageOperationCoffeeSite.getId() : "";
        }
        if (objectExtId.isEmpty()) {
            hideProgressbarAndEnableMenuItems();
            Toast.makeText(this, R.string.manage_images_delete_failure, Toast.LENGTH_SHORT).show();
            pendingImageOperationCoffeeSite = null;
            return;
        }
        new ImageDeleteNewApiAsyncTask(this, userAccountService,
                objectExtId, mainImage.getExternalId()).execute();
    }

    @Override
    public void onImageObjectLoadFailed(Result.Error error) {
        if (pendingImageDeleteLookup) {
            pendingImageDeleteLookup = false;
            hideProgressbarAndEnableMenuItems();
            Toast.makeText(this, R.string.manage_images_delete_failure, Toast.LENGTH_SHORT).show();
            pendingImageOperationCoffeeSite = null;
        }
    }

    @Override
    public void onImageUploaded(String imageExtId) {
        if (isPendingCreateImageUploadInProgress()) {
            handlePendingCreateImageUploadSuccess();
            return;
        }
        CoffeeSite coffeeSite = pendingImageOperationCoffeeSite != null
                ? pendingImageOperationCoffeeSite
                : currentCoffeeSite;
        String imageUrl = buildMainImageUrl(coffeeSite != null ? coffeeSite.getId() : "");
        onImageSaveSuccess(coffeeSite, imageUrl);
        pendingImageOperationCoffeeSite = null;
    }

    @Override
    public void onImageUploadFailed(Result.Error error) {
        if (isPendingCreateImageUploadInProgress()) {
            handlePendingCreateImageUploadFailure(error);
            return;
        }
        CoffeeSite coffeeSite = pendingImageOperationCoffeeSite != null
                ? pendingImageOperationCoffeeSite
                : currentCoffeeSite;
        onImageSaveFailure(coffeeSite, error != null ? error.getDetail() : "");
        pendingImageOperationCoffeeSite = null;
    }

    @Override
    public void onImageDeleted(String imageExtId) {
        onImageDeleteSuccess(imageExtId);
        pendingImageOperationCoffeeSite = null;
    }

    @Override
    public void onImageDeleteFailed(Result.Error error) {
        onImageDeleteFailure(error != null ? error.getDetail() : "");
        pendingImageOperationCoffeeSite = null;
    }

    /**
     * After successful or failed image save we need to save updated
     * CoffeeSite itself.
     *
     * @param imageSaveResult - load URL of the newly uploaded image is expected
     */
    private void onImageSaveSuccess(CoffeeSite coffeeSite, String imageSaveResult) {
        hideProgressbarAndEnableMenuItems();
        currentImageCount = Math.max(currentImageCount, 1);
        if (currentCoffeeSite != null) {
            currentCoffeeSite.setMainImageURL(imageSaveResult);
        }
        if (coffeeSite != null) {
            coffeeSite.setMainImageURL(imageSaveResult);
        }
        refreshImageActionState();

        // if newly created CoffeeSite has now image saved too, we can delete it from DB
        cleanupOfflineDraftAfterServerSave();

        if (mode == MODE_MODIFY || mode == MODE_MODIFY_FROM_DETAILACTIVITY) {
            // If we are in MODIFY MODE, then Image was saved first
            // now the CoffeeSite itself has to be updated/saved too
            if (coffeeSiteCUDOperationsService != null && coffeeSite != null) {
                coffeeSiteCUDOperationsService.update(coffeeSite);
            }
            // Invalidate Picasso as the URL has not changed, but the image itself could
            if (coffeeSite != null && !coffeeSite.getMainImageURL().isEmpty()) {
                Picasso.get().invalidate(coffeeSite.getMainImageURL());
            }
            return;
        }
        // When in CREATE mode, coffeeSite was already saved, so Activate it, if requested
        if (saveAndActivateRequested && coffeeSite != null) {
            activateCoffeeSite(coffeeSite);
            return;
        }
        goToMyCoffeeSitesActivity();
    }

    private void onImageSaveFailure(CoffeeSite coffeeSite, String imageSaveResult) {
        hideProgressbarAndEnableMenuItems();
        Toast.makeText(getApplicationContext(),
                "Problém při ukládání obrázku.", Toast.LENGTH_SHORT)
                .show();
        if (mode == MODE_MODIFY || mode == MODE_MODIFY_FROM_DETAILACTIVITY) { // in MODE_MODIFY, the Image is saved first
            // Even if image save failed, we need to save CoffeeSite itself
            if (coffeeSiteCUDOperationsService != null && coffeeSite != null) {
                coffeeSiteCUDOperationsService.update(coffeeSite);
            }
        }
    }

    private boolean startPendingCreateImageUploads(CoffeeSite savedCoffeeSite) {
        clearPendingCreateImageUploads();
        for (String localImagePath : localImagePaths) {
            if (localImagePath == null || localImagePath.isEmpty()) {
                continue;
            }
            File localImageFile = ImageUtil.getImageFile(localImagePath);
            if (localImageFile.exists()) {
                pendingCreateImageUploads.add(localImageFile);
            } else {
                Log.w(TAG, "Draft image file for upload does not exist: " + localImagePath);
            }
        }
        if (pendingCreateImageUploads.isEmpty()) {
            clearPendingCreateImageUploads();
            return false;
        }
        pendingCreatedCoffeeSite = savedCoffeeSite;
        pendingCreateImageUploadIndex = 0;
        uploadNextPendingCreateImage();
        return true;
    }

    private boolean isPendingCreateImageUploadInProgress() {
        return pendingCreatedCoffeeSite != null
                && pendingCreateImageUploadIndex >= 0
                && pendingCreateImageUploadIndex < pendingCreateImageUploads.size();
    }

    private void uploadNextPendingCreateImage() {
        if (!isPendingCreateImageUploadInProgress()) {
            finishPendingCreateImageUploads();
            return;
        }
        uploadCoffeeSiteImage(pendingCreateImageUploads.get(pendingCreateImageUploadIndex),
                pendingCreatedCoffeeSite,
                pendingCreateImageUploadIndex == 0 ? IMAGE_TYPE_MAIN : IMAGE_TYPE_OTHER);
    }

    private void handlePendingCreateImageUploadSuccess() {
        if (pendingCreatedCoffeeSite == null || pendingCreateImageUploadIndex < 0) {
            clearPendingCreateImageUploads();
            hideProgressbarAndEnableMenuItems();
            return;
        }
        if (pendingCreateImageUploadIndex == 0) {
            String mainImageUrl = buildMainImageUrl(pendingCreatedCoffeeSite.getId());
            pendingCreatedCoffeeSite.setMainImageURL(mainImageUrl);
            if (currentCoffeeSite != null) {
                currentCoffeeSite.setMainImageURL(mainImageUrl);
            }
            currentImageCount = Math.max(currentImageCount, 1);
            refreshImageActionState();
        }
        pendingCreateImageUploadIndex++;
        uploadNextPendingCreateImage();
    }

    private void handlePendingCreateImageUploadFailure(@Nullable Result.Error error) {
        pendingCreateImageUploadFailures++;
        Log.e(TAG, "Draft image upload failed: "
                + (error != null && error.getDetail() != null ? error.getDetail() : ""));
        pendingCreateImageUploadIndex++;
        uploadNextPendingCreateImage();
    }

    private void finishPendingCreateImageUploads() {
        CoffeeSite savedCoffeeSite = pendingCreatedCoffeeSite;
        int uploadFailures = pendingCreateImageUploadFailures;
        clearPendingCreateImageUploads();
        hideProgressbarAndEnableMenuItems();
        if (savedCoffeeSite != null && uploadFailures == 0) {
            deleteLocalDraftImages();
            finishCoffeeSiteCreateAfterSave(savedCoffeeSite);
            return;
        }
        if (savedCoffeeSite != null) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.manage_images_upload_failure),
                    Toast.LENGTH_SHORT).show();
            finishCoffeeSiteCreateAfterSave(savedCoffeeSite);
        }
    }

    private void finishCoffeeSiteCreateAfterSave(CoffeeSite savedCoffeeSite) {
        cleanupOfflineDraftAfterServerSave();
        if (saveAndActivateRequested) {
            activateCoffeeSite(savedCoffeeSite);
            return;
        }
        goToMyCoffeeSitesActivity();
    }

    private void cleanupOfflineDraftAfterServerSave() {
        if (currentCoffeeSite == null
                || currentCoffeeSite.isSavedOnServer()
                || currentCoffeeSite.isStatusZaznamuAvailable()) {
            return;
        }
        currentCoffeeSite.restoreId();
        updateCoffeeSiteInDB(currentCoffeeSite);
        coffeeSiteCUDOperationsService.deleteFromDB(currentCoffeeSite);
        deleteLocalDraftImages();
    }

    private void deleteLocalDraftImages() {
        for (String localImagePath : localImagePaths) {
            if (localImagePath == null || localImagePath.isEmpty()) {
                continue;
            }
            File localImageFile = ImageUtil.getImageFile(localImagePath);
            if (localImageFile.exists()) {
                localImageFile.delete();
            }
        }
        localImagePaths.clear();
        imagePhotoFile = null;
        syncCurrentCoffeeSiteLocalImages();
    }

    private void clearPendingCreateImageUploads() {
        pendingCreateImageUploads.clear();
        pendingCreatedCoffeeSite = null;
        pendingCreateImageUploadIndex = -1;
        pendingCreateImageUploadFailures = 0;
    }

    /**
     * Called when REST Image deleteUser request was successful.
     * Show the inform Toast, then.
     * Disable imageDeleteButton as there is no image to deleteUser, now
     * Clear imageView and set the default icon to it.
     *
     * @param imageDeleteResult external ID of the deleted image
     */
    private void onImageDeleteSuccess(String imageDeleteResult) {
        hideProgressbarAndEnableMenuItems();
        String text = getString(R.string.image_delete_success);

        Toast toast = Toast.makeText(getApplicationContext(),
                text,
                Toast.LENGTH_SHORT);
        toast.show();
        currentImageCount = 0;
        if (currentCoffeeSite != null) {
            currentCoffeeSite.setMainImageURL("");
            currentCoffeeSite.setMainImageFilePath("");
        }
        imagePhotoFile = null;
        localImagePaths.clear();
        syncCurrentCoffeeSiteLocalImages();
        refreshImageActionState();
        siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_outline_add_photo_alternate_36));
    }

    private void onImageDeleteFailure(String imageDeleteResult) {
        hideProgressbarAndEnableMenuItems();
        Toast toast = Toast.makeText(getApplicationContext(),
                imageDeleteResult != null && !imageDeleteResult.isEmpty()
                        ? imageDeleteResult
                        : getString(R.string.manage_images_delete_failure),
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void syncCurrentCoffeeSiteMainImage(@Nullable ImageObject imageObject) {
        if (currentCoffeeSite == null || imageObject == null) {
            return;
        }
        ImageFile mainImage = getMainImageFile(imageObject);
        if (mainImage == null) {
            return;
        }
        String mainImageUrl = mainImage.getBytesUrl("mid");
        if (mainImageUrl.isEmpty()) {
            mainImageUrl = buildMainImageUrl(currentCoffeeSite.getId());
        }
        currentCoffeeSite.setMainImageURL(mainImageUrl);
        currentCoffeeSite.setMainImageFilePath("");
        imagePhotoFile = null;
        Picasso.get().invalidate(mainImageUrl);
        Picasso.get().load(mainImageUrl)
                .resize(0, siteFotoView.getMaxHeight())
                .placeholder(R.drawable.ic_outline_add_photo_alternate_36)
                .into(siteFotoView);
    }

    private void syncUpdatedCoffeeSiteImageState(@Nullable CoffeeSite updatedCoffeeSite) {
        if (updatedCoffeeSite == null || currentCoffeeSite == null) {
            return;
        }
        updatedCoffeeSite.setMainImageURL(currentCoffeeSite.getMainImageURL());
        updatedCoffeeSite.setMainImageFilePath(currentCoffeeSite.getMainImageFilePath());
        if (!updatedCoffeeSite.getMainImageURL().isEmpty()) {
            Picasso.get().invalidate(updatedCoffeeSite.getMainImageURL());
        }
    }


    /* ===== Methods to follow after successful REST call or DB save/update ======= */

    /**
     * Starts MyCoffeeSitesListActivity or CoffeeSiteDetailActivity after editing was called from there
     */
    private void goBackAfterUpdate(CoffeeSite updatedCoffeeSite) {
        if (mode == MODE_MODIFY_FROM_DETAILACTIVITY) {
            goToCoffeeSiteDetailActivityAfterUpdate(updatedCoffeeSite);
            return;
        }
        Intent activityIntent = new Intent(CreateCoffeeSiteActivity.this, MyCoffeeSitesListActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) updatedCoffeeSite);
        activityIntent.putExtra("coffeeSitePosition", coffeeSitePositionInRecyclerView);

        setResult(Activity.RESULT_OK, activityIntent);
        finishActivity(EDIT_COFFEESITE_REQUEST);
        finish();
    }

    /**
     * Starts CoffeeSiteDetailActivity if this activity was called from there
     */
    private void  goToCoffeeSiteDetailActivityAfterUpdate(CoffeeSite updatedCoffeeSite) {
        Intent activityIntent = new Intent(CreateCoffeeSiteActivity.this, CoffeeSiteDetailActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) updatedCoffeeSite);
        // finishActivity(EDIT_COFFEESITE_REQUEST); do not pass requestCode to CoffeeSiteDetailActivity, but WHY???
        activityIntent.putExtra("requestCode", EDIT_COFFEESITE_REQUEST);

        setResult(Activity.RESULT_OK, activityIntent);
        finishActivity(EDIT_COFFEESITE_REQUEST);
        finish();
    }


    /**
     * Starts MyCoffeeSitesListActivity
     * Used in case this Activity was called from MainActivity, so the user created
     * new CoffeeSites and saved it only. Then we pass him to the list of his/her
     * CoffeeSites in MyCoffeeSitesListActivity.
     */
    private void goToMyCoffeeSitesActivity() {
        Intent activityIntent = new Intent(CreateCoffeeSiteActivity.this, MyCoffeeSitesListActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra("mode", mode);
        setResult(Activity.RESULT_OK, activityIntent);
        if (mode == MODE_CREATE) { // new CoffeeSite called from MainActivity
            this.startActivity(activityIntent);
        }
        if (mode == MODE_CREATE_FROM_MYCOFFEESITESACTIVITY) { // new CoffeeSite called from MyCoffeeSitesListActivity
             finishActivity(CREATE_COFFEESITE_REQUEST);
        }
        if (mode == MODE_MODIFY) { // edit CoffeeSite called from MyCoffeeSitesListActivity
            finishActivity(EDIT_COFFEESITE_REQUEST);
        }
        finish();
    }

    /* ********************** ****************************** */

    /**
     * Setup locationService listeners ...<br>
     * If we are in MODIFY CoffeeSite mode, wait until user deletes both location info
     * to activate locationEnterManualMode and cityOrStreetEnterManualMode.
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
        if (locationService != null) {
            currentLocation = locationService.getPosledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
            locationService.addPropertyChangeListener(this);
        }
        // Initiate coffeeSite location selected by user
        if (currentLocation != null) {
            firstLocationDetection = false;
            selectedCoffeeSiteLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (locationEnterAutomaticMode) {
                showLocationInView(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            if (cityOrStreetEnterAutomaticMode) {
                showCityStreetInView(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
        }
    }


    /**
     * Show TimePicker dialog to select Opening hours From/To
     * @param v
     */
    public boolean showTimePickerDialog(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            timeFromToPicker = newInstance((TextInputEditText) v);
            timeFromToPicker.show(getSupportFragmentManager(), "timePicker");
            return true;
        }
        return false;
    }

    public static TimePickerFragment newInstance(TextInputEditText callingInputEditText) {
        TimePickerFragment f = new TimePickerFragment();
        f.setCallingInputEditText(callingInputEditText);
        return f;
    }

    @Override
    public void onEnterTimeDialogPositiveClick(TextInputEditText callingInputEditText, int hourOfDay, int minute) {
        callingInputEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
    }

    /**
     * To fill View by current data from CoffeeSite to be edited
     *
     * @param coffeeSite
     */
    private void fillViewWithCoffeeSiteData(CoffeeSite coffeeSite) {
        if (coffeeSite == null) {
            return;
        }
        Log.i(TAG, "fillViewWithCoffeeSiteData() start");

        coffeeSiteNameEditText.setText(coffeeSite.getName());

        latitudeEditText.setText(String.valueOf(coffeeSite.getLatitude()));
        longitudeEditText.setText(String.valueOf(coffeeSite.getLongitude()));

        streetEditText.setText(coffeeSite.getUliceCP());
        cityEditText.setText(coffeeSite.getMesto());

        sourceTypeEditText.setText(coffeeSite.getTypPodniku().toString());
        locationTypeEditText.setText(coffeeSite.getTypLokality().toString());

        if (coffeeSite.getCena() != null) {
            priceRangeEditText.setText(coffeeSite.getCena().toString());
        }

        selectChipsInGroupAccordingText(coffeeSortsChipGroup, coffeeSite.getCoffeeSorts());

        selectChipsInGroupAccordingText(otherOfferChipGroup, coffeeSite.getOtherOffers());

        openingDaysEditText.setText(coffeeSite.getOteviraciDobaDny());

        if (!coffeeSite.getOteviraciDobaHod().isEmpty() && coffeeSite.getOteviraciDobaHod().contains("-")
            && coffeeSite.getOteviraciDobaHod().split("-").length > 1) {
            openingFromTimeEditText.setText(coffeeSite.getOteviraciDobaHod().split("-")[0]);
            openingToTimeEditText.setText(coffeeSite.getOteviraciDobaHod().split("-")[1]);
        }

        // Show photo if available
        boolean isOnline = Utils.isOnline(getApplicationContext());
        File localMainImageFile = getLocalMainImageFile(coffeeSite);

        if (localMainImageFile != null && localMainImageFile.exists()) {
            Picasso.get().load(localMainImageFile)
                    .resize(0, siteFotoView.getMaxHeight()).placeholder(R.drawable.ic_outline_add_photo_alternate_36)
                    .into(siteFotoView);
            Log.i("CreateCoffeeSiteAct", "CoffeeSite created");
            return;
        }

        if (coffeeSite.getMainImageURL().isEmpty() && !isLocalOnlyDraft(coffeeSite)) {
            coffeeSite.setMainImageURL(buildMainImageUrl(coffeeSite.getId()));
        }

        if (isOnline && !coffeeSite.getMainImageURL().isEmpty()) {
            Picasso.get().load(coffeeSite.getMainImageURL())
                    .resize(0, siteFotoView.getMaxHeight()).placeholder(R.drawable.ic_outline_add_photo_alternate_36)
                    .into(siteFotoView);
        }
        if (!isOnline || coffeeSite.getMainImageURL().isEmpty()) {
            File coffeeSiteImageFile = ImageUtil.getCoffeeSiteImageFile(getApplicationContext(), coffeeSite);
            if (coffeeSiteImageFile.exists()) {
                Picasso.get().load(coffeeSiteImageFile)
                        .resize(0, siteFotoView.getMaxHeight()).placeholder(R.drawable.ic_outline_add_photo_alternate_36)
                        .into(siteFotoView);
            } else {
                siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.ic_outline_add_photo_alternate_36));
            }
        }
        Log.i("CreateCoffeeSiteAct", "CoffeeSite created");
    }

    /**
     * Sets selected chips according Strings inserted. If inserted string matches
     * a chip name (Chip of the group), then it is selected
     */
    private void selectChipsInGroupAccordingText(ChipGroup chipGroup, List<? extends CoffeeSiteEntity> strings) {
        chipGroup.clearCheck();
        if (strings == null) {
            return;
        }
        for (CoffeeSiteEntity str : strings) {
            String chipSelectionValue = getChipSelectionValue(str);
            if (chipSelectionValue.isEmpty()) {
                continue;
            }
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip && areChipValuesEquivalent(chipSelectionValue, ((Chip) child).getText().toString())) {
                    ((Chip) child).setChecked(true);
                }
            }
        }
    }

    @NonNull
    private String getChipSelectionValue(@Nullable CoffeeSiteEntity entity) {
        if (entity == null) {
            return "";
        }

        String entityValue = entity.toString();
        if (!normalizeChipValue(entityValue).isEmpty()) {
            return entityValue;
        }

        if (entity instanceof OtherOffer) {
            List<OtherOffer> otherOffers = coffeeSiteEntitiesViewModel.getOtherOffers();
            if (otherOffers == null) {
                return "";
            }
            for (OtherOffer otherOffer : otherOffers) {
                if (otherOffer != null && otherOffer.getId().equals(entity.getId())) {
                    return otherOffer.getOtherOffer();
                }
            }
        }

        if (entity instanceof CoffeeSort) {
            List<CoffeeSort> coffeeSorts = coffeeSiteEntitiesViewModel.getCoffeeSorts();
            if (coffeeSorts == null) {
                return "";
            }
            for (CoffeeSort coffeeSort : coffeeSorts) {
                if (coffeeSort != null && coffeeSort.getId().equals(entity.getId())) {
                    return coffeeSort.getCoffeeSort();
                }
            }
        }

        return "";
    }

    private boolean areChipValuesEquivalent(String entityValue, String chipValue) {
        String normalizedEntityValue = normalizeChipValue(entityValue);
        String normalizedChipValue = normalizeChipValue(chipValue);

        return normalizedEntityValue.equals(normalizedChipValue);
    }

    private String normalizeChipValue(String value) {
        if (value == null) {
            return "";
        }
        String normalizedValue = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalizedValue.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * Creates CoffeeSite instance based on data from this Activity View data
     * or updates inserted CoffeeSite.
     */
    private CoffeeSite createOrUpdateCoffeeSiteFromViewModel(final CoffeeSite coffeeSiteToUpdate, final CoffeeSiteEntitiesViewModel coffeeSiteEntitiesViewModel) {
        Log.i(TAG, "Create CoffeeSiteFromViewModel() start");

        CoffeeSite coffeeSite;
        if (coffeeSiteToUpdate == null) {
            coffeeSite = new CoffeeSite();
            coffeeSite.setId(java.util.UUID.randomUUID().toString());
            coffeeSite.setCreatedOn(new Date());
        } else {
            coffeeSite = coffeeSiteToUpdate;
        }

        coffeeSite.setName(coffeeSiteNameEditText.getText().toString().trim());

        coffeeSite.setLatitude(Double.parseDouble(latitudeEditText.getText().toString()));
        coffeeSite.setLongitude(Double.parseDouble(longitudeEditText.getText().toString()));
        coffeeSite.setUliceCP(streetEditText.getText().toString().trim());
        coffeeSite.setMesto(cityEditText.getText().toString().trim());

        String typPodniku = sourceTypeEditText.getText().toString();
        typPodniku = !typPodniku.isEmpty() ? typPodniku : SITE_TYPES_LOCAL[0];

        coffeeSite.setTypPodniku(coffeeSiteEntitiesViewModel.getCoffeeSiteType(typPodniku));

        String typLokality = locationTypeEditText.getText().toString();
        typLokality  = !typLokality .isEmpty() ? typLokality  : LOCATION_TYPES_LOCAL[0];

        coffeeSite.setTypLokality(coffeeSiteEntitiesViewModel.getSiteLocationType(typLokality));

        coffeeSite.setCena(coffeeSiteEntitiesViewModel.getPriceRange(priceRangeEditText.getText().toString()));

        String[] selectedCoffeeSorts = getSelectedChipsStrings(coffeeSortsChipGroup);
        coffeeSite.setCoffeeSorts(coffeeSiteEntitiesViewModel.createCoffeeSortsList(selectedCoffeeSorts));

        String[] selectedOtherOffer = getSelectedChipsStrings(otherOfferChipGroup);
        coffeeSite.setOtherOffers(coffeeSiteEntitiesViewModel.createOtherOffersList(selectedOtherOffer));

        if (!openingDaysEditText.getText().toString().isEmpty()) {
            coffeeSite.setOteviraciDobaDny(openingDaysEditText.getText().toString());
        }

        if (!openingFromTimeEditText.getText().toString().isEmpty() && !openingToTimeEditText.getText().toString().isEmpty()) {
            coffeeSite.setOteviraciDobaHod(openingFromTimeEditText.getText().toString() + "-" + openingToTimeEditText.getText().toString());
        }

        coffeeSite.setStatusZarizeni(coffeeSiteEntitiesViewModel.getCoffeeSiteStatus("V provozu"));

        Log.i(TAG, "CoffeeSite created/updated");
        return coffeeSite;
    }

    /**
     * Creates or updates CoffeeSite from this Activity form data, saves it to DB
     * and goes to MyCoffeeSitesActivity.
     * <p>
     * The creation of the CoffeeSite must be called in separate thread of the AsyncTask
     * as the CoffeeSiteEntitiesViewModel uses repositories Single.observe() requests (to get
     * correct values of some CS entities), which are not invoked in main UI thread.
     * <p>
     * Used in OFFLINE mode only as in ONLINE the REST call AsyncTask does the job.
     */
    private class CreateUpdateCoffeeSiteAsyncTask {

        private final CoffeeSiteEntitiesViewModel model;

        CreateUpdateCoffeeSiteAsyncTask(CoffeeSiteEntitiesViewModel model) {
            this.model = model;
        }

        // coffeeSite to update/save
        public void execute(final CoffeeSite coffeeSite) {
            AsyncRunner.runInBackground(() -> {
                CoffeeSite result = createOrUpdateCoffeeSiteFromViewModel(coffeeSite, model);
                AsyncRunner.runOnMainThread(() -> {
                    currentCoffeeSite = result;
                    if (imagePhotoFile != null) {
                        currentCoffeeSite.setMainImageFilePath(imagePhotoFile.getPath());
                    }
                    saveCoffeeSiteToDB(result);
                    // New CoffeeSite saved successfully to DB
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.coffeesite_saved_to_db),
                            Toast.LENGTH_SHORT);
                    toast.show();
                    if (coffeeSite == null) {
                        goToMyCoffeeSitesActivity();
                    } else {
                        goBackAfterUpdate(result);
                    }
                });
            });
        }
    }

    /**
     * Inserts all data from DB to Create CoffeeSite form of this activity
     */
    private class InsertEntitiesDataFromDBAsyncTask {

        InsertEntitiesDataFromDBAsyncTask() {
        }

        // coffeeSite to update/save
        public void execute() {
            AsyncRunner.runInBackground(() -> {
                coffeeSiteEntitiesViewModel = new CoffeeSiteEntitiesViewModel(getApplication());
                showCoffeeSiteTypes();
                showLocationTypes();
                showPriceRanges();
            });
        }
    }



    private String[] getSelectedChipsStrings(ChipGroup chipGroup) {
        List<String> selectedChipValues = new ArrayList<>();
        for (int chipId : chipGroup.getCheckedChipIds()) {
            Chip chip = (Chip) findViewById(chipId);
            if (chip == null) {
                continue;
            }
            String chipValue = chip.getText() != null ? chip.getText().toString().trim() : "";
            if (!chipValue.isEmpty()) {
                selectedChipValues.add(chipValue);
            }
        }
        return selectedChipValues.toArray(new String[0]);
    }

    /**
     * Method which is invoked when current Location of equipment has changed.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (locationService != null) {
            Location newLocation = locationService.getCurrentLocation();
            if (newLocation != null) {
                if (locationEnterAutomaticMode) {
                    showLocationInView(newLocation.getLatitude(), newLocation.getLongitude());
                }
                if (cityOrStreetEnterAutomaticMode) {
                    // revoke Address if the change distance is more then 25 m
                    // Avoid to many requests to Geolocation API if phone is moving
                    long newDistance = 0;
                    if (currentLocation != null) {
                        newDistance = locationService.getDistanceFromCurrentLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    }
                    if (newDistance >= 25 || firstLocationDetection) {
                        showCityStreetInView(newLocation.getLatitude(), newLocation.getLongitude());
                    }
                }
                currentLocation = newLocation;
                firstLocationDetection = false;
            }
        }
    }

    /**
     * Shows location selected by user from Map in the respective latitude and longitude view
     */
    private void showLocationInView(double latitude, double longitude) {

        if (latitude >= -180 && latitude <= 180
               && longitude >= -180 && longitude <= 180 ) {

            String latitudeString = String.valueOf(latitude);
            if (latitudeString.length() > 12)
                latitudeString = latitudeString.substring(0, 11);
            String longitudeString = String.valueOf(longitude);
            if (longitudeString.length() > 12)
                longitudeString = longitudeString.substring(0, 11);

            latitudeEditText.setTag("latitudeChangedProgrammatically");
            longitudeEditText.setTag( "longitudeChangedProgrammatically" );
            latitudeEditText.setText(latitudeString);
            longitudeEditText.setText(longitudeString);
            latitudeEditText.setTag( null);
            longitudeEditText.setTag( null);
        }
    }

    /**
     * Shows location selected by user from Map in the respective latitude and longitude view
     */
    private void showCityStreetInView(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            Log.d(TAG, "Looking for address start ...");
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            Log.e(TAG, "Error looking for address: " + e.getMessage());
        }

        if (addresses != null && addresses.size() > 0) {
            Log.d(TAG, "Address found");
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String[] adresa = addresses.get(0).getAddressLine(0).split(",");
            String street = adresa[0]; // getAddressLine(0) obvykle ve tvaru: ulice s CP, PSC Mesto, Stat
            // Street nemuze zacinat cislem nebo slovy "Unnamed" ...
            if (street != null && (street.startsWith("Unnamed") || street.matches("^\\d.*"))) {
                street = "";
            }
            // Derive mesto from addressline withoyt PSC number (getAddressLine(0) obvykle ve tvaru: ulice s CP, PSC Mesto, Stat)
            String mesto = adresa[1].replaceFirst("^ \\d{3} \\d{2} ", "");

            cityEditText.setTag("cityChangedProgrammatically");
            streetEditText.setTag("streetChangedProgrammatically");
            cityEditText.setText(mesto);
            streetEditText.setText(street);
            cityEditText.setTag(null);
            streetEditText.setTag(null);
        }
    }

    private void showCoffeeSiteCreateFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_create_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteUpdateFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_update_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteActivateFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_activate_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteCancelFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_cancel_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }


    private void showCoffeeSiteStatusChangeSuccess(CoffeeSiteStatusChangeService.StatusChangeOperation status, String result) {

        switch (status) {
            case COFFEE_SITE_CANCEL: {
                result = !(result.isEmpty() || "OK".equals(result)) ? result : getString(R.string.coffeesite_canceled_successfuly);
            } break;
            case COFFEE_SITE_ACTIVATE: {
                result = !(result.isEmpty() || "OK".equals(result)) ? result : getString(R.string.coffeesite_activated_successfuly);
            } break;
            case COFFEE_SITE_DEACTIVATE: {
                result = !(result.isEmpty() || "OK".equals(result)) ? result : getString(R.string.coffeesite_deactivated_successfuly);
            } break;
            default: break;
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                result,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCoffeeSiteOperationSuccess(CoffeeSiteCUDOperationsService.CUDOperation operation, String result) {
        // If not equal only OK, then there is something interesting, otherwise get default
        switch (operation) {
            case COFFEE_SITE_SAVE: {
                result = !(result.isEmpty() || "OK".equals(result)) ? result : getString(R.string.coffeesite_created_successfuly);
            } break;
            case COFFEE_SITE_UPDATE: {
                result = !(result.isEmpty() || "OK".equals(result)) ? result : getString(R.string.coffeesite_updated_successfuly);
            } break;
            default: break;
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                result,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void showProgressbarAndDisableMenuItems() {
        saveMenuItem.setEnabled(false);
        imageDeleteMenuItem.setEnabled(false);
        saveCoffeeSiteProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbarAndEnableMenuItems() {
        saveMenuItem.setEnabled(true);
        refreshImageActionState();
        saveCoffeeSiteProgressBar.setVisibility(View.GONE);
    }

    private boolean hasMainImage() {
        boolean hasLocalImage = imagePhotoFile != null && imagePhotoFile.exists();
        boolean hasCurrentCoffeeSiteImage = currentCoffeeSite != null
                && (!currentCoffeeSite.getMainImageFilePath().isEmpty()
                || (!isLocalOnlyDraft(currentCoffeeSite) && !currentCoffeeSite.getMainImageURL().isEmpty()));
        return hasLocalImage || hasCurrentCoffeeSiteImage;
    }

    private boolean isLocalOnlyDraft(@Nullable CoffeeSite coffeeSite) {
        return coffeeSite != null
                && !coffeeSite.isSavedOnServer()
                && !coffeeSite.isStatusZaznamuAvailable();
    }

    private boolean isCreateMode() {
        return mode == MODE_CREATE || mode == MODE_CREATE_FROM_MYCOFFEESITESACTIVITY;
    }

    private boolean canOpenManageImages() {
        if (!Utils.isOnline(getApplicationContext())) {
            return false;
        }
        if (currentCoffeeSite == null) {
            return isCreateMode();
        }
        if (currentCoffeeSite.getId().isEmpty()) {
            return isCreateMode();
        }
        if (mode == MODE_MODIFY || mode == MODE_MODIFY_FROM_DETAILACTIVITY) {
            return true;
        }
        return currentCoffeeSite.isSavedOnServer() || currentCoffeeSite.isStatusZaznamuAvailable();
    }

    private void openManageImagesActivity() {
        if (!hasMainImage() || !canOpenManageImages()) {
            return;
        }
        Intent manageImagesIntent = new Intent(CreateCoffeeSiteActivity.this, CoffeeSiteImagesActivity.class);
        manageImagesIntent.putExtra("coffeeSite", (Parcelable) getCoffeeSiteForManageImages());
        manageImagesIntent.putStringArrayListExtra(CoffeeSiteImagesActivity.EXTRA_LOCAL_IMAGE_PATHS,
                new ArrayList<>(localImagePaths));
        startActivityForResult(manageImagesIntent, REQUEST_MANAGE_IMAGES);
    }

    private void refreshImageCountIfNeeded() {
        if (canOpenManageImages()
                && currentCoffeeSite != null
                && !currentCoffeeSite.getId().isEmpty()) {
            new GetImageObjectAsyncTask(this, currentCoffeeSite.getId()).execute();
        } else {
            if (!localImagePaths.isEmpty()) {
                currentImageCount = localImagePaths.size();
                refreshImageActionState();
                return;
            }
            boolean hasUnknownRemoteImages = currentCoffeeSite != null
                    && !Utils.isOnline(getApplicationContext())
                    && !currentCoffeeSite.getId().isEmpty()
                    && currentCoffeeSite.getMainImageFilePath().isEmpty()
                    && !currentCoffeeSite.getMainImageURL().isEmpty();
            currentImageCount = hasUnknownRemoteImages ? 0 : (hasMainImage() ? 1 : 0);
            refreshImageActionState();
        }
    }

    private void refreshImageActionState() {
        boolean hasMainImage = hasMainImage();
        int effectiveImageCount = currentImageCount > 0 ? currentImageCount : (hasMainImage ? 1 : 0);
        imageDeleteMenuItem.setEnabled(effectiveImageCount == 1);
        manageImagesMenuItem.setEnabled(effectiveImageCount > 0 && canOpenManageImages());
    }

    private void showSelectedMainImage(File mainImageFile) {
        if (mainImageFile == null) {
            return;
        }
        Picasso.get().load(mainImageFile).into(siteFotoView);
        replaceLocalImagesWithMainImage(mainImageFile);
        syncCurrentCoffeeSiteLocalImages();
        currentImageCount = 1;
        refreshImageActionState();
    }

    private void handleManagedImagesResult(@Nullable Intent data) {
        if (data == null) {
            return;
        }
        ArrayList<String> updatedLocalImagePaths = data.getStringArrayListExtra(
                CoffeeSiteImagesActivity.EXTRA_LOCAL_IMAGE_PATHS);
        if (updatedLocalImagePaths == null) {
            return;
        }
        localImagePaths.clear();
        localImagePaths.addAll(updatedLocalImagePaths);
        applyLocalImagesState();
    }

    private CoffeeSite getCoffeeSiteForManageImages() {
        if (currentCoffeeSite != null) {
            return currentCoffeeSite;
        }
        CoffeeSite draftCoffeeSite = new CoffeeSite();
        draftCoffeeSite.setName(coffeeSiteNameEditText.getText() != null
                ? coffeeSiteNameEditText.getText().toString().trim()
                : "");
        return draftCoffeeSite;
    }

    private void replaceLocalImagesWithMainImage(File mainImageFile) {
        localImagePaths.clear();
        if (mainImageFile != null) {
            localImagePaths.add(mainImageFile.getPath());
        }
        syncCurrentCoffeeSiteLocalImages();
    }

    private void applyLocalImagesState() {
        currentImageCount = localImagePaths.size();
        if (localImagePaths.isEmpty()) {
            imagePhotoFile = null;
            syncCurrentCoffeeSiteLocalImages();
            siteFotoView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_outline_add_photo_alternate_36));
            refreshImageActionState();
            return;
        }

        imagePhotoFile = new File(localImagePaths.get(0));
        syncCurrentCoffeeSiteLocalImages();
        Picasso.get().load(imagePhotoFile).into(siteFotoView);
        refreshImageActionState();
    }

    private void restoreLocalImagesFromCoffeeSite(@Nullable CoffeeSite coffeeSite) {
        localImagePaths.clear();
        imagePhotoFile = null;
        if (coffeeSite == null) {
            return;
        }

        List<String> storedLocalImagePaths = coffeeSite.getLocalImagePaths();
        for (String localImagePath : storedLocalImagePaths) {
            if (localImagePath != null && !localImagePath.isEmpty()) {
                localImagePaths.add(localImagePath);
                break;
            }
        }

        if (localImagePaths.isEmpty()
                && coffeeSite.getMainImageFilePath() != null
                && !coffeeSite.getMainImageFilePath().isEmpty()) {
            localImagePaths.add(coffeeSite.getMainImageFilePath());
        }

        if (!localImagePaths.isEmpty()) {
            imagePhotoFile = new File(localImagePaths.get(0));
        }
        syncCurrentCoffeeSiteLocalImages(coffeeSite);
    }

    private void syncCurrentCoffeeSiteLocalImages() {
        syncCurrentCoffeeSiteLocalImages(currentCoffeeSite);
    }

    private void syncCurrentCoffeeSiteLocalImages(@Nullable CoffeeSite coffeeSite) {
        if (coffeeSite == null) {
            return;
        }
        if (isLocalOnlyDraft(coffeeSite)) {
            coffeeSite.setMainImageURL("");
        }
        if (localImagePaths.isEmpty()) {
            coffeeSite.setLocalImagePaths(new ArrayList<>());
            coffeeSite.setMainImageFilePath("");
            return;
        }
        ArrayList<String> singleLocalImagePath = new ArrayList<>();
        singleLocalImagePath.add(localImagePaths.get(0));
        coffeeSite.setLocalImagePaths(singleLocalImagePath);
        coffeeSite.setMainImageFilePath(localImagePaths.get(0));
    }

    @Nullable
    private File getLocalMainImageFile(@Nullable CoffeeSite coffeeSite) {
        if (coffeeSite == null) {
            return null;
        }

        List<String> storedLocalImagePaths = coffeeSite.getLocalImagePaths();
        if (!storedLocalImagePaths.isEmpty()) {
            File localMainImageFile = new File(storedLocalImagePaths.get(0));
            if (localMainImageFile.exists()) {
                return localMainImageFile;
            }
        }

        if (coffeeSite.getMainImageFilePath() != null && !coffeeSite.getMainImageFilePath().isEmpty()) {
            File localMainImageFile = new File(coffeeSite.getMainImageFilePath());
            if (localMainImageFile.exists()) {
                return localMainImageFile;
            }
        }

        return null;
    }

    /**
     * Alert dialog for capture or select from gallery
     */
    private void selectImage() {
        final CharSequence[] items = { "Foto", "Vybrat z galerie", "Zrušit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateCoffeeSiteActivity.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Foto")) {
                requestStoragePermission(true);
            } else if (items[item].equals("Vybrat z galerie")) {
                dispatchGalleryIntent();
            } else if (items[item].equals("Zrušit")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Starts MapsActivity to select geo location of a new CoffeeSite
     */
    private void openSelectLocationMapActivity() {
        double lat;
        double lon;
        // Read current coordinates from respective input fields
        try {
            lat = Double.parseDouble(latitudeEditText.getText().toString());
            lon = Double.parseDouble(longitudeEditText.getText().toString());
            if (lat >= -180 && lat <= 180
                    && lon >= -180 && lon <= 180) {
                selectedCoffeeSiteLocation = new LatLng(lat, lon);
                Intent mapIntent = new Intent(this, SelectLocationMapActivity.class);
                mapIntent.putExtra("selectedLocation", selectedCoffeeSiteLocation);
                startActivityForResult(mapIntent, GET_COFFEESITE_LOCATION_REQUEST);
            }
        } catch (NumberFormatException ex) {
            Log.e(TAG, "Latitude and/or longitude not available for start SelectLocationMapActivity");
        }
    }


    /***** Requesting PERMISSIONS on User's action demand ******************/

    /** From https://androidwave.com/capture-image-from-camera-gallery/ */
    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private void requestStoragePermission(boolean isCamera) {
        if (!isCamera) {
            dispatchGalleryIntent();
            return;
        }

        Dexter.withContext(this)
              .withPermissions(Manifest.permission.CAMERA)
              .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (isCamera) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryIntent();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
              })
              .withErrorListener(error -> Toast.makeText(getApplicationContext(), "Chyba oprávnění! ", Toast.LENGTH_SHORT)
                                               .show())
               .onSameThread()
               .check();
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permissions_camera_storage_title);
        builder.setMessage(
                R.string.permissions_camera_storage_request);
        builder.setPositiveButton(R.string.permissions_camera_storage_settings, (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(R.string.permissions_camera_storage_canel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /*  -------------- INNER CLASSES ------------------- */

    /**
     * Helper inner class to check if CoffeeSite's type dropdown input field
     * is correctly entered as it can be entered manually by user.
     * If not correct, then it is fixed providing first location type item
     * from {@link SITE_TYPES_LOCAL} list.
     * <p></>
     * Probably not needed in this Activity as the list of available items are inserted
     * into list of only selectable items of {@link AutoCompleteTextView} sourceTypeEditText
     */
    class SiteTypeValidator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {
            Log.v("Test", "Checking if valid: " + text);
            return coffeeSiteTypesAvailable.contains(text.toString());
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            Log.v("Test", "Returning fixed text");
            return SITE_TYPES_LOCAL[0];
        }
    }

    static class SiteTypeInputFocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.v("Test", "Focus changed");
            if (v.getId() == R.id.site_type_dropdown && !hasFocus) {
                Log.v("Test", "Performing validation");
                ((AutoCompleteTextView) v).performValidation();
            }
        }
    }

    /**
     * Helper inner class to check if CoffeeSite's location type dropdown input field
     * is correctly entered as it can be entered manually by user.
     * If not correct, then it is fixed providing first location type item
     * from {@link LOCATION_TYPES_LOCAL} list.
     * <p></>
     * Probably not needed in this Activity as the list of available items are inserted
     * into list of only selectable items of {@link AutoCompleteTextView} locationTypeEditText
     */
    class LocationTypeValidator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {
            Log.v(TAG, "Checking if valid: " + text);
            return siteLocationTypesAvailable.contains(text.toString());
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            Log.v("Test", "Returning fixed text");
            return LOCATION_TYPES_LOCAL[0];
        }
    }

    class LocationTypeInputFocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.v(TAG, "Focus changed");
            if (v.getId() == R.id.location_type_dropdown && !hasFocus) {
                Log.v(TAG, "Performing dropdown input validation.");
                ((AutoCompleteTextView) v).performValidation();
            }
        }
    }

    /**
     * Helper inner class to achieve functionality of hiding keyboard, when the drop
     */
    private class HideKeyboardOnFocusListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if ((v.getId() == R.id.coffeesitename_input_edittext && !hasFocus)
                    || (v.getId() == R.id.city_edittext && !hasFocus)
                    || (v.getId() == R.id.street_edittext && !hasFocus)
                    || (v.getId() == R.id.latitude_input_edittext && !hasFocus)
                    || (v.getId() == R.id.longitude_input_edittext && !hasFocus)) {

                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    /**
     * Helper inner class to achieve functionality of hiding keyboard, when the inputTextView
     * loose the focus.
     */
    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if ((v.getId() == R.id.coffeesitename_input_edittext && !hasFocus)
                || (v.getId() == R.id.city_edittext && !hasFocus)
                || (v.getId() == R.id.street_edittext && !hasFocus)
                || (v.getId() == R.id.latitude_input_edittext && !hasFocus)
                || (v.getId() == R.id.longitude_input_edittext && !hasFocus)) {

                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

}
