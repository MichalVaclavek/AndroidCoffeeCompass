package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteEntitiesServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteServiceStatusOperationsListener;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesServiceConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageServiceConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceCallResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.utils.FileCompressor;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService.StatusChangeOperation.COFFEE_SITE_ACTIVATE;


/**
 * Activity to show View, where user can enter data for a new CoffeeSite or
 * where the data of already created CoffeeSite can be edited.
 * It offers 3 functions in every "mode" (MODE_CREATE and MODE_MODIFY)
 * in BottomNavigationMenu.
 * The functions for MODE_CREATE are: Save, Save and Activate, Delete photo
 * The functions for MODE_MODIFY are: Save and Delete photo
 */
public class CreateCoffeeSiteActivity extends ActivityWithLocationService
                                      implements CoffeeSiteImageServiceCallResultListener,
                                                 CoffeeSiteImageServiceConnectionListener,
                                                 TimePickerFragment.EnterTimeDialogListener,
                                                 SaveActivateCoffeeSiteDialogFragment.SaveActivateCoffeeSiteDialogListener,
                                                 DeleteCoffeeSiteImageDialogFragment.DeleteCoffeeSiteImageDialogListener,
                                                 PropertyChangeListener,
                                                 CoffeeSiteEntitiesServiceConnectionListener,
                                                 CoffeeSiteEntitiesServiceOperationsListener,
                                                 CoffeeSiteServicesConnectionListener,
                                                 CoffeeSiteServiceCUDOperationsListener,
                                                 CoffeeSiteServiceStatusOperationsListener {

    private static final String TAG = "CreateCoffeeSiteAct";

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

    private CoffeeSiteCreateModel createCoffeeSiteViewModel;

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
    ChipGroup coffeeTypesChipGroup;
    @BindView(R.id.dalsi_nabidka_chip_group)
    ChipGroup otherOfferChipGroup;

    @BindView(R.id.price_range_dropdown)
    AutoCompleteTextView priceRangeEditText;
    @BindView(R.id.otviracka_dny_dropdown)
    AutoCompleteTextView openingDaysEditText;

    @BindView(R.id.coffeeSite_imageView)
    ImageView siteFotoView;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;

    MenuItem imageDeleteMenuItem;
    MenuItem saveMenuItem; // save and/or activate

    private Location location;

    private static final long MAX_STARI_DAT = 1000 * 60;
    private static final float LAST_PRESNOST = 100.0f;


    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteImageService;

    /**
     * Service for uploading and deleting CoffeeSite's image over REST
     */
    protected cz.fungisoft.coffeecompass2.services.CoffeeSiteImageService coffeeSiteImageService;
    private CoffeeSiteImageServiceConnector coffeeSiteImageServiceConnector;


    protected CoffeeSiteCUDOperationsService coffeeSiteCUDOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteCUDOperationsService> coffeeSiteCUDOperationsServiceConnector;

    protected CoffeeSiteStatusChangeService coffeeSiteStatusChangeService;
    private CoffeeSiteServicesConnector<CoffeeSiteStatusChangeService> coffeeSiteStatusChangeServiceConnector;


    protected CoffeeSiteEntitiesService coffeeSiteEntitiesService;
    private CoffeeSiteEntitiesServiceConnector coffeeSiteEntitiesServiceConnector;


    private CoffeeSite currentCoffeeSite;

    private int coffeeSitePositionInRecyclerView;

    private String[] SITE_TYPES;
    private String[] LOCATION_TYPES;

    /**
     * To detect if the CoffeeSite/Activity is in
     * mode for Creation of a new CoffeeSite or
     * if it modifies already created CoffeeSites.
     */
    private static final int MODE_CREATE = 0;
    private static final int MODE_MODIFY = 1;

    /**
     * Default mode is create
     */
    private int mode = MODE_CREATE;

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
     * Progress ba to be shown during saving of a new or
     * modified CoffeeSite and/or CofeeSite's Image
     */
    @BindView(R.id.create_modify_progressBar)
    ProgressBar saveCoffeeSiteProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coffee_site);

        ButterKnife.bind(this);

        contextView = findViewById(R.id.coffeesite_create_main_scrollview);

        createCoffeeSiteViewModel = ViewModelProviders.of(this, new CoffeeSiteViewModelFactory())
                .get(CoffeeSiteCreateModel.class);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) { // When called from MyCoffeeSitesListActivity ...
            currentCoffeeSite = (CoffeeSite) bundle.getParcelable("coffeeSite");
            coffeeSitePositionInRecyclerView = bundle.getInt("coffeeSitePosition");
        }

        //bottomNavigation = findViewById(R.id.bottom_navigation);
        // Enable/disable bottom navigation menu items
        imageDeleteMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_foto_delete);
        saveMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_ulozit_and_or_aktivovat); // save and/or activate

        // Enable bottom navigation menu items
        saveMenuItem.setEnabled(true);
        saveMenuItem.setTitle(R.string.coffeesite_save_activate);

        // We are editing site here - insert input values to View from currentCoffeeSite
        // "Edit mode"
        if (currentCoffeeSite != null && currentCoffeeSite.getId() != 0) {
            mode = MODE_MODIFY;
            fillViewWithCoffeeSiteData(currentCoffeeSite);

            saveMenuItem.setTitle(R.string.save_coffeesite_updated);

             // delete image button
            imageDeleteMenuItem.setEnabled(!currentCoffeeSite.getMainImageURL().isEmpty());

            if (!currentCoffeeSite.getMainImageURL().isEmpty()) {
                Picasso.get().load(currentCoffeeSite.getMainImageURL()).into(siteFotoView);
            }
        }

        siteFotoView.setOnClickListener(createSitePhotoViewOnClickListener());

        showCoffeeSiteTypes();
        showLocationTypes();
        showPriceRanges();

        // Otviracka - dny
        String[] OTVIRACKA_DNY = getResources().getStringArray(R.array.otviracka_dny);
        final ArrayAdapter<String> otvirackaDnyAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                OTVIRACKA_DNY);

        AutoCompleteTextView otvirackaDnyDropdown = findViewById(R.id.otviracka_dny_dropdown);
        otvirackaDnyDropdown.setAdapter(otvirackaDnyAdapter);

        // Listener for selecting items from Bottom navigation menu
        BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_ulozit_and_or_aktivovat:
                                // Create CoffeeSite instance from model
                                if (mode == MODE_CREATE) {
                                    // Check if locationType and sourceType are selected
                                    boolean errorInInput = false;
                                    if (locationTypeEditText.getText().toString().isEmpty()) {
                                        //locationTypeEditText.setBackgroundColor(R.color.design_default_color_error);
                                        locationTypeTextInputLayout.setError(getString(R.string.invalid_locationType));
                                        errorInInput = true;
                                    }
                                    if (sourceTypeEditText.getText().toString().isEmpty()) {
                                        //sourceTypeEditText.setBackgroundColor(R.color.design_default_color_error);
                                        //sourceTypeTextInputLayout.setEr
                                        sourceTypeTextInputLayout.setError(getString(R.string.invalid_coffeesite_type));
                                        errorInInput = true;
                                    }

                                    if (errorInInput) return true;

                                    currentCoffeeSite = createOrUpdateCoffeeSiteFromViewModel(null);

                                    // Show dialog to choose if the CoffeeSite should be only saved
                                    // or saved and activated
                                    SaveActivateCoffeeSiteDialogFragment dialog = new SaveActivateCoffeeSiteDialogFragment();
                                    dialog.show(getSupportFragmentManager(), "SaveActivateCoffeeSiteDialogFragment");

                                }
                                if (mode == MODE_MODIFY) {
                                    currentCoffeeSite = createOrUpdateCoffeeSiteFromViewModel(currentCoffeeSite);
                                    updateCoffeeSite(currentCoffeeSite);
                                }

                                return true;

                            case R.id.navigation_foto_delete:
                                // Show dialog to choose if the CoffeeSite should be cancelled or not
                                DeleteCoffeeSiteImageDialogFragment dialog = new DeleteCoffeeSiteImageDialogFragment();
                                dialog.show(getSupportFragmentManager(), "DeleteCoffeeSiteImageDialogFragment");

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
                }
                if (createCoffeeSiteFormState.getLatitudeError() != null) {
                    latitudeEditText.setError(getString(createCoffeeSiteFormState.getLatitudeError()));
                }
                if (createCoffeeSiteFormState.getLongitudeError() != null) {
                    longitudeEditText.setError(getString(createCoffeeSiteFormState.getLongitudeError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            // Validate input in loginViewModel
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

        openingFromTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingFromTimeEditText, event));
        openingToTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingToTimeEditText, event));

        //Listenrs to inputTextFields to ensure keyboard hiding when focus lost
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        coffeeSiteNameEditText.setOnFocusChangeListener(ofcListener);
        cityEditText.setOnFocusChangeListener(ofcListener);
        streetEditText.setOnFocusChangeListener(ofcListener);
        latitudeEditText.setOnFocusChangeListener(ofcListener);
        longitudeEditText.setOnFocusChangeListener(ofcListener);

        doBindCoffeeSiteImageService();

        // To compress image/photo files of the CoffeeSIte
        fileCompressor = new FileCompressor(this);
        // To detect, that user did not choose new image file yet
        imagePhotoFile = null;
    }


    /** Operations to start Photo or Image capture Intents and saving the result **** START ***/

    /**
     * OnClickListener for sitePhotoView
     */
    private View.OnClickListener createSitePhotoViewOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        };
        return retVal;
    }

    static final int REQUEST_TAKE_PHOTO = 100;
    static final int REQUEST_GALLERY_PHOTO = 101;

    private Uri photoURI;
    private File imagePhotoFile;
    private FileCompressor fileCompressor;

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
                    // Show it in View
                    Picasso.get().load(imagePhotoFile).into(siteFotoView);
                    imageDeleteMenuItem.setEnabled(true);

                }
                break;
                case REQUEST_GALLERY_PHOTO: {
                    Uri selectedImage = data.getData();
                    try {
                        // Compress chosen image file
                        imagePhotoFile = fileCompressor.compressToFile(new File(Utils.getRealPathFromUri(selectedImage, this)));
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to compress image file from gallery.");
                    }
                    Picasso.get().load(imagePhotoFile).into(siteFotoView);
                    imageDeleteMenuItem.setEnabled(true);
                }
                break;
                default: break;
            }
        }
    }

    /**
     * Creates empty file to store taken photo or selected picture from Gall
     * ery
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = getFilesDir();
        File image = File.createTempFile(imageFileName,  /* prefix */
                                   ".jpg",         /* suffix */
                                        storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imagePhotoFile = image;
        String currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Create image file failed.");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        //"cz.fungisoft.coffeecompass2.fileprovider",
                        getString(R.string.file_provider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /** Operations to start Photo or Image capture Intents and saving the result **** END ***/

    /**
     * Select image from gallery
     */
    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Lets bind CoffeeSiteEtitiesService and load all CoffeeSiteEntities
        // in onCoffeeSiteEntitiesConnected() method
        // TODO - Verify, if calling this in onResume() would be more convenient
        doBindCoffeeSiteEntitiesService();

        doBindCoffeeSiteCUDOperationsService();
        doBindCoffeeSiteStatusChangeService();
    }

    @Override
    protected void onStop() {
        doUnbindCoffeeSiteEntitiesService();
        doUnbindCoffeeSiteCUDOperationsService();
        doUnbindCoffeeSiteStatusChangeService();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    /******************* CoffeeSiteImageService ***************************************/

    private void doBindCoffeeSiteImageService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        coffeeSiteImageServiceConnector = new CoffeeSiteImageServiceConnector();
        coffeeSiteImageServiceConnector.addCoffeeSiteImageServiceConnectionListener(this);

        if (bindService(new Intent(this, CoffeeSiteImageService.class),
                coffeeSiteImageServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteImageService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteImageService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }


    /**
     * Called when CoffeeSiteImageService is Connected
     */
    @Override
    public void onCoffeeSiteImageServiceConnected() {
        coffeeSiteImageService = coffeeSiteImageServiceConnector.getCoffeeSiteImageService();
        if (coffeeSiteImageService != null) {
            coffeeSiteImageService.addImageOperationsResultListener(this);
        }
    }

    private void doUnbindCoffeeSiteImageService() {
        if (mShouldUnbindCoffeeSiteImageService) {
            if (coffeeSiteImageService != null) {
                coffeeSiteImageService.removeImageOperationsResultListener(this);
            }
            // Release information about the service's state.
            coffeeSiteImageServiceConnector.removeCoffeeSiteImageServiceConnectionListener(this);
            unbindService(coffeeSiteImageServiceConnector);
            mShouldUnbindCoffeeSiteImageService = false;
        }
    }

    /** Helper methods to correctly display lists of CoffeeSites properties/vlastnosti  **/
    /** which can be slected by user during new CoffeeSite creation **/
    /* ----- START ------- */

    private void showCoffeeSiteTypes() {
        // Typ zdroje
        SITE_TYPES = getResources().getStringArray(R.array.coffee_site_type);

        ArrayAdapter<String> siteTypesAdapter;
        if (CoffeeSiteEntitiesRepository.getAllCoffeeSiteTypes().size() > 0) {
            siteTypesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                    CoffeeSiteEntitiesRepository.getAllCoffeeSiteTypes());
        } else {
            siteTypesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                    SITE_TYPES);
        }

        AutoCompleteTextView siteTypeDropdown = findViewById(R.id.site_type_dropdown);
        siteTypeDropdown.setAdapter(siteTypesAdapter);
        siteTypeDropdown.setValidator(new SiteTypeValidator());
        siteTypeDropdown.setOnFocusChangeListener(new SiteTypeInputFocusListener());
    }

    private void showLocationTypes() {
        // Typ lokality
        LOCATION_TYPES = getResources().getStringArray(R.array.location_type);
        ArrayAdapter<String> locationTypesAdapter;

        if (CoffeeSiteEntitiesRepository.getAllSiteLocationTypes().size() > 0) {
            locationTypesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                    CoffeeSiteEntitiesRepository.getAllSiteLocationTypes());
        } else {
            locationTypesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                    LOCATION_TYPES);
        }

        AutoCompleteTextView locationTypesDropdown = findViewById(R.id.location_type_dropdown);
        locationTypesDropdown.setAdapter(locationTypesAdapter);
        locationTypesDropdown.setValidator(new LocationTypeValidator());
        locationTypesDropdown.setOnFocusChangeListener(new LocationTypeInputFocusListener());
    }

    private void showPriceRanges() {
        // Cenovy rozsah
        String[] PRICE_RANGES = getResources().getStringArray(R.array.cena_range);
        ArrayAdapter<String> priceRangesAdapter;
        if (CoffeeSiteEntitiesRepository.getAllPriceRanges().size() > 0) {
            priceRangesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                    CoffeeSiteEntitiesRepository.getAllPriceRanges());
        } else {
            priceRangesAdapter = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                    PRICE_RANGES);
        }

        AutoCompleteTextView priceRangesDropdown = findViewById(R.id.price_range_dropdown);
        priceRangesDropdown.setAdapter(priceRangesAdapter);
    }

    /** Helper methods to correctly display lists of CoffeeSites properties/vlastnosti  **/
    /** which can be selected by user during new CoffeeSite creation **/
    /* ----- END ------- */


    /*** CoffeeSiteEntitiesService ***/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteEntitiesService;

    private void doBindCoffeeSiteEntitiesService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        coffeeSiteEntitiesServiceConnector = new CoffeeSiteEntitiesServiceConnector();
        coffeeSiteEntitiesServiceConnector.addCoffeeSiteImageServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteEntitiesService.class),
                coffeeSiteEntitiesServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteEntitiesService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteEntitiesService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindCoffeeSiteEntitiesService() {
        if (mShouldUnbindCoffeeSiteEntitiesService) {
            if (coffeeSiteEntitiesService != null) {
                coffeeSiteEntitiesService.removeCoffeeSiteEntitiesOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteEntitiesServiceConnector.removeCoffeeSiteImageServiceConnectionListener(this);
            unbindService( coffeeSiteEntitiesServiceConnector);
            mShouldUnbindCoffeeSiteEntitiesService = false;
        }
    }


    @Override
    public void onCoffeeSiteEntitiesServiceConnected() {
        coffeeSiteEntitiesService = coffeeSiteEntitiesServiceConnector.getCoffeeSiteImageService();
        if (coffeeSiteEntitiesService != null) {
            coffeeSiteEntitiesService.addCoffeeSiteEntitiesOperationsListener(this);
            startLoadingCoffeeSiteEntities();
        }
    }

    public void startLoadingCoffeeSiteEntities() {
        //showProgressbar();
        if (coffeeSiteEntitiesService != null) {
            coffeeSiteEntitiesService.readAndSaveAllEntitiesFromServer();
        }
    }


    /********* CoffeeSiteCUDOperationsService **************/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteCUDOperationsService;

    private void doBindCoffeeSiteCUDOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
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
        // (and thus won't be supporting component replacement by other
        // applications).
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
                deleteCoffeeSiteImageLocally(imagePhotoFile);
                imagePhotoFile = null;
            }
            if (currentCoffeeSite != null && !currentCoffeeSite.getMainImageURL().isEmpty()) {
                deleteCoffeeSiteImage(currentCoffeeSite);
            }
        }
    }

    /**
     * Negative and Neutral are swapped here as we require to have
     * Neutral item as second one in the Dialog.
     * @param dialog
     */
    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        //saveCoffeeSite(currentCoffeeSite);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof SaveActivateCoffeeSiteDialogFragment) {
            saveCoffeeSite(currentCoffeeSite);
        }
    }

    /** Methods to be called after Service request for uploading, **/
    /** or deleting image is finished **/

    /**
     * After succeessful or failed image save we need to save updated
     * CoffeeSite itsels.
     *
     * @param imageSaveResult - load URL of the newly uploaded image is expected
     */
    @Override
    public void onImageSaveSuccess(String imageSaveResult) {
        hideProgressbar();
        if (mode == MODE_MODIFY) {
            //startCoffeeSiteServiceOperation(currentCoffeeSite, COFFEE_SITE_UPDATE);
            // If we are in MODIFY MODE, the Image was saved first, now
            // the CoffeeSite itself has to be updated/saved
            if (coffeeSiteCUDOperationsService != null) {
                coffeeSiteCUDOperationsService.update(currentCoffeeSite);
            }
        }
        // Image saved, button to delete it can be enabled, but probably
        // not needed as after Save we are going to MyCoffeeSiteActivity
        imageDeleteMenuItem.setEnabled(true);
        // Image saved to be shown in siteFotoView (should be same as already
        // shown image, but as aproval, that it was saved correctly)
        currentCoffeeSite.setMainImageURL(imageSaveResult);
        if (!currentCoffeeSite.getMainImageURL().isEmpty()) {
            Picasso.get().invalidate(Uri.parse(currentCoffeeSite.getMainImageURL()));
            Picasso.get().load(currentCoffeeSite.getMainImageURL()).into(siteFotoView);
        }
    }

    @Override
    public void onImageSaveFailure(String imageSaveResult) {
        hideProgressbar();
        if (mode == MODE_MODIFY) {
            // Even if image save failed, we need to save CoffeeSite itself
            if (coffeeSiteCUDOperationsService != null) {
                coffeeSiteCUDOperationsService.update(currentCoffeeSite);
            }
        }
    }

    /**
     * Called when REST Image delete request was successful.
     * Show the inform Toast, then
     * Disable imageDeleteButton as there is no image to delete, now
     * Clear imageView and set the default icon to it.
     *
     * @param imageDeleteResult melo by byt Site id. kteremu se smazal Image
     */
    @Override
    public void onImageDeleteSuccess(String imageDeleteResult) {
        hideProgressbar();
        String text = getString(R.string.image_delete_ok);;
        if (Long.valueOf(imageDeleteResult) == currentCoffeeSite.getId()) {
            text = getString(R.string.image_delete_success);
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                                    text,
                                    Toast.LENGTH_SHORT);
        toast.show();
        // No image here, disable delete button
        imageDeleteMenuItem.setEnabled(false);
        siteFotoView.setImageDrawable(getDrawable(R.drawable.outline_add_photo_alternate_white_36));
    }

    @Override
    public void onImageDeleteFailure(String imageDeleteResult) {
        hideProgressbar();
        Toast toast = Toast.makeText(getApplicationContext(),
                imageDeleteResult,
                Toast.LENGTH_SHORT);
        toast.show();
    }


    /* ---- Methods to invoke CoffeeSIteService operation based on user's selection */

    /**
     * Entry point for actions selected by user in BottomNavigationMenu
     *
     * @param coffeeSite
     */
    private void saveCoffeeSite(CoffeeSite coffeeSite) {
        showProgressbar();
        saveAndActivateRequested = false;
        if (coffeeSiteCUDOperationsService != null) {
            coffeeSiteCUDOperationsService.save(coffeeSite);
        }
    }

    private void saveCoffeeSiteAndActivate(CoffeeSite coffeeSite) {
        showProgressbar();
        saveAndActivateRequested = true;
        if (coffeeSiteCUDOperationsService != null) {
            coffeeSiteCUDOperationsService.save(coffeeSite);
        }
    }

    private void activateCoffeeSite(CoffeeSite coffeeSite) {
        showProgressbar();
        if (coffeeSiteStatusChangeService != null) {
            coffeeSiteStatusChangeService.activate(coffeeSite);
        }
    }

    private void updateCoffeeSite(CoffeeSite coffeeSite) {
        showProgressbar();
        // If there is a photoFile, save it first to be available
        // after CoffeeSite is returned after it's update
        if (imagePhotoFile != null) {
            uploadCoffeeSiteImage(imagePhotoFile, coffeeSite.getId());
        } else {
            if (coffeeSiteCUDOperationsService != null) {
                coffeeSiteCUDOperationsService.update(currentCoffeeSite);
            }
       }
    }

    private void deleteCoffeeSiteImage(CoffeeSite coffeeSite) {
        showProgressbar();
        if (coffeeSiteImageService != null) {
            coffeeSiteImageService.deleteImage(coffeeSite.getId());
        }
    }

    private void uploadCoffeeSiteImage(File imageFile, int coffeeSiteId) {
        showProgressbar();
        if (coffeeSiteImageService != null) {
            coffeeSiteImageService.uploadImage(imageFile, coffeeSiteId);
        }
    }

    /**
     * Deletes imageFile and disable image delete button
     * and sets the default icon to the siteFotoView
     * @param imageFile
     */
    private void deleteCoffeeSiteImageLocally(File imageFile) {
        imageFile.delete();
        imageDeleteMenuItem.setEnabled(false); // nothing to delete now
        // set a default icon to the siteFotoView
        siteFotoView.setImageDrawable(getDrawable(R.drawable.outline_add_photo_alternate_white_36));
    }


    @Override
    public void onCoffeeSiteSaved(CoffeeSite savedCoffeeSite, String error) {
        hideProgressbar();
        Log.i(TAG, "Save OK?: " + error.isEmpty());
        if (error.isEmpty()) {
            // New CoffeeSite saved successfuly
            showCoffeeSiteOperationSuccess(CoffeeSiteCUDOperationsService.CUDOperation.COFFEE_SITE_SAVE, "OK");
            // If there is a photoFile, save it too
            if (imagePhotoFile != null) {
                uploadCoffeeSiteImage(imagePhotoFile, savedCoffeeSite.getId());
            }
            // Was also activation requested?
            if (saveAndActivateRequested) {
                activateCoffeeSite(savedCoffeeSite);
            } else {
                goToMyCoffeeSitesActivity();
            }
        } else {
            showCoffeeSiteCreateFailure(error);
        }
    }

    @Override
    public void onCoffeeSiteUpdated(CoffeeSite updatedCoffeeSite, String error) {
        hideProgressbar();
        Log.i(TAG, "Update success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteUpdateFailure(error);
        }
        else {
            // successful return from CoffeeSite update REST call
            showCoffeeSiteOperationSuccess(CoffeeSiteCUDOperationsService.CUDOperation.COFFEE_SITE_UPDATE, "OK");
            goToMyCoffeeSitesActivityAfterUpdate(updatedCoffeeSite);
        }
    }

    @Override
    public void onCoffeeSiteActivated(CoffeeSite activeCoffeeSite, String error) {
        hideProgressbar();
        Log.i(TAG, "Activate success?: " + error.isEmpty());
        if (!error.isEmpty()) {
            showCoffeeSiteActivateFailure(error);
        }
        else {
            showCoffeeSiteStatusChangeSuccess(COFFEE_SITE_ACTIVATE, "OK");
        }
        goToMyCoffeeSitesActivity();
    }


    /**
     * Starts MyCoffeeSitesListActivity
     */
    private void goToMyCoffeeSitesActivityAfterUpdate(CoffeeSite updatedCoffeeSite) {
        Intent activityIntent = new Intent(CreateCoffeeSiteActivity.this, MyCoffeeSitesListActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) updatedCoffeeSite);
        activityIntent.putExtra("coffeeSitePosition", coffeeSitePositionInRecyclerView);
        //this.startActivity(activityIntent);
        setResult(Activity.RESULT_OK, activityIntent);
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
        //activityIntent.putExtra("currentUserName", userAccountService.getLoggedInUser().getUserName());
        //activityIntent.putExtra("coffeeSite", (Parcelable) currentCoffeeSite);
        setResult(Activity.RESULT_OK, activityIntent);
        this.startActivity(activityIntent);
        finish();
    }

    /* ********************** ****************************** */

    /**
     * Setup locationService listeners ...
     * If we are in MODIFY CoffeeSite mode,
     * don't update LatitudeAndLongitudeView
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();

        location = locationService.posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        locationService.addPropertyChangeListener(this);
        showCurrentLocationInView();
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

        selectChipsInGroupAccordingText(coffeeTypesChipGroup, coffeeSite.getCoffeeSorts());

        selectChipsInGroupAccordingText(otherOfferChipGroup, coffeeSite.getOtherOffers());

        openingDaysEditText.setText(coffeeSite.getOteviraciDobaDny());

        if (!coffeeSite.getOteviraciDobaHod().isEmpty() && coffeeSite.getOteviraciDobaHod().contains("-")
            && coffeeSite.getOteviraciDobaHod().split("-").length > 1) {
            openingFromTimeEditText.setText(coffeeSite.getOteviraciDobaHod().split("-")[0]);
            openingToTimeEditText.setText(coffeeSite.getOteviraciDobaHod().split("-")[1]);
        }

        Log.i("CreateCoffeeSiteAct", "CoffeeSite created");
    }

    /**
     * Sets selected chips according Strings inserted. If inserted string matches
     * a chip name (Chip of the group), then it is selected
     */
    private void selectChipsInGroupAccordingText(ChipGroup chipGroup, List<? extends CoffeeSiteEntity> strings) {
        for (CoffeeSiteEntity str : strings) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip && str.toString().equalsIgnoreCase(((Chip) child).getText().toString())) {
                    ((Chip) child).setChecked(true);
                }
            }
        }
    }

    /**
     * Creates CoffeeSite instance based on data from this Activity View data.
     * or updates inserted CoffeeSite
     */
    private CoffeeSite createOrUpdateCoffeeSiteFromViewModel(CoffeeSite coffeeSiteToUpdate) {

        Log.i(TAG, "Create CoffeeSiteFromViewModel() start");

        CoffeeSite coffeeSite = (coffeeSiteToUpdate == null) ? new CoffeeSite() : coffeeSiteToUpdate;

        if (coffeeSiteToUpdate == null) {
            coffeeSite.setId(0);
        }
        coffeeSite.setName(coffeeSiteNameEditText.getText().toString().trim());

        coffeeSite.setLatitude(Double.parseDouble(latitudeEditText.getText().toString()));
        coffeeSite.setLongitude(Double.parseDouble(longitudeEditText.getText().toString()));
        coffeeSite.setUliceCP(streetEditText.getText().toString().trim());
        coffeeSite.setMesto(cityEditText.getText().toString().trim());

        String typPodniku = sourceTypeEditText.getText().toString();
        typPodniku = !typPodniku.isEmpty() ? typPodniku : SITE_TYPES[0];
        coffeeSite.setTypPodniku(CoffeeSiteEntitiesRepository.getCoffeeSiteType(typPodniku));

        String typLokality = locationTypeEditText.getText().toString();
        typLokality  = !typLokality .isEmpty() ? typLokality  : LOCATION_TYPES[0];
        coffeeSite.setTypLokality(CoffeeSiteEntitiesRepository.getSiteLocationType(typLokality));

        coffeeSite.setCena(CoffeeSiteEntitiesRepository.getPriceRange(priceRangeEditText.getText().toString()));

        String[] selectedCoffeeType = getSelectedChipsStrings(coffeeTypesChipGroup);
        coffeeSite.setCoffeeSorts(CoffeeSiteEntitiesRepository.getCoffeeSortsList(selectedCoffeeType));

        String[] selectedOtherOffer = getSelectedChipsStrings(otherOfferChipGroup);
        coffeeSite.setOtherOffers(CoffeeSiteEntitiesRepository.getOtherOffersList(selectedOtherOffer));

        coffeeSite.setOteviraciDobaDny(openingDaysEditText.getText().toString());
        coffeeSite.setOteviraciDobaHod(openingFromTimeEditText.getText().toString() + "-" + openingToTimeEditText.getText().toString());

        if (mode == MODE_CREATE) {
            coffeeSite.setStatusZarizeni(CoffeeSiteEntitiesRepository.getCoffeeSiteStatus("V provozu"));
        }

        Log.i(TAG, "CoffeeSite created/updated");
        return coffeeSite;
    }


    private String[] getSelectedChipsStrings(ChipGroup chipGroup) {
        String[] retVal = new String[chipGroup.getCheckedChipIds().size()];

        int i = 0;
        for (int chipId : chipGroup.getCheckedChipIds()) {
            Chip chip = (Chip) findViewById(chipId);
            retVal[i] = chip.getText().toString();
            i++;
        }

        return retVal;
    }

    /**
     * Method which is invoked when Location is changed.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (locationService != null) {
            location = locationService.getCurrentLocation();
        }
        showCurrentLocationInView();
    }

    /**
     * Shows current location in the respective latitude and longitude view
     * if location service is available and we are in MODE_CREATE
     */
    private void showCurrentLocationInView() {
        if (location != null && mode == MODE_CREATE) {
            String latitude = String.valueOf(location.getLatitude());
            if (latitude.length() > 12)
                latitude = latitude.substring(0, 11);
            String longitude = String.valueOf(location.getLongitude());
            if (longitude.length() > 12)
                longitude = longitude.substring(0, 11);

            latitudeEditText.setText(latitude);
            longitudeEditText.setText(longitude);
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
//            case COFFEE_SITE_ACTIVATE: {
//                result = !(result.isEmpty() || "OK".equals(result)) ? result : getString(R.string.coffeesite_activated_successfuly);
//            } break;
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
    public void showProgressbar() {
        saveCoffeeSiteProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbar() {
        saveCoffeeSiteProgressBar.setVisibility(View.GONE);
    }


    /**
     * Validators for CoffeeSiteType input and for CoffeeSiteLocationType input
     */
    class SiteTypeValidator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {
            Log.v("Test", "Checking if valid: " + text);
            Arrays.sort(SITE_TYPES);
            if (Arrays.binarySearch(SITE_TYPES, text.toString()) > 0) {
                return true;
            }

            return false;
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            Log.v("Test", "Returning fixed text");

            return SITE_TYPES[0];
        }
    }

    class SiteTypeInputFocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.v("Test", "Focus changed");
            if (v.getId() == R.id.site_type_dropdown && !hasFocus) {
                Log.v("Test", "Performing validation");
                ((AutoCompleteTextView)v).performValidation();
            }
        }
    }


    class LocationTypeValidator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {
            Log.v("Test", "Checking if valid: " + text);
            Arrays.sort(LOCATION_TYPES);
            if (Arrays.binarySearch(LOCATION_TYPES, text.toString()) > 0) {
                return true;
            }

            return false;
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            Log.v("Test", "Returning fixed text");

            return LOCATION_TYPES[0];
        }
    }

    class LocationTypeInputFocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.v(TAG, "Focus changed");
            if (v.getId() == R.id.location_type_dropdown && !hasFocus) {
                Log.v(TAG, "Performing dropdown input validation.");
                ((AutoCompleteTextView)v).performValidation();
            }
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindCoffeeSiteImageService();
        super.onDestroy();
    }

    /**
     * Alert dialog for capture or select from galley
     */
    private void selectImage() {
        final CharSequence[] items = {
                "Foto", "Vybrat z galerie",
                "Zrušit"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateCoffeeSiteActivity.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Foto")) {
                requestStoragePermission(true);
            } else if (items[item].equals("Vybrat z galerie")) {
                requestStoragePermission(false);
            } else if (items[item].equals("Zrušit")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /***** Requesting PERMISIONS on User's action demand ******************/

    /** From https://androidwave.com/capture-image-from-camera-gallery/ */
    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private void requestStoragePermission(boolean isCamera) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
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
                .withErrorListener(
                        error -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
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

    /**
     * Helper inner class to achieve functionality of hiding keyboaard when the inputTextView
     * loose the focus.
     */
    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if((v.getId() == R.id.coffeesitename_input_edittext && !hasFocus)
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
