package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_ACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_CANCEL;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_DEACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_DELETE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_SAVE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_SAVE_AND_ACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_UPDATE;

/**
 * Activity to show View, where user can enter data for a new CoffeeSite or
 * where the data of already created CoffeeSite can be edited.
 * It offers 3 functions in every "mode" (MODE_CREATE and MODE_MODIFY)
 * in BottomNavigationMenu
 * The functions for MODE_CREATE are: Save, Save and Activate, Delete photo
 * The functions for MODE_CREATE are: Save, Cancel and Delete photo
 */
public class CreateCoffeeSiteActivity extends ActivityWithLocationService implements TimePickerFragment.EnterTimeDialogListener,
        PropertyChangeListener {

    private static final String TAG = "CreateCoffeeSiteAct";

    BottomNavigationView bottomNavigation;

    @BindView(R.id.time_od_input_editText) TextInputEditText openingFromTimeEditText;
    @BindView(R.id.time_do_input_editText) TextInputEditText openingToTimeEditText;

    TimePickerFragment timeFromToPicker;

    @BindView(R.id.coffeesitename_input_edittext) TextInputEditText coffeeSiteNameEditText;
    @BindView(R.id.latitude_input_edittext) TextInputEditText latitudeEditText;
    @BindView(R.id.longitude_input_edittext) TextInputEditText longitudeEditText;

    private CoffeeSiteCreateModel createCoffeeSiteViewModel;

    // All other View input elements needed to create CoffeeSite
    @BindView(R.id.city_edittext) TextInputEditText cityEditText;
    @BindView(R.id.street_edittext) TextInputEditText streetEditText;

    @BindView(R.id.site_type_dropdown) AutoCompleteTextView sourceTypeEditText;
    @BindView(R.id.location_type_dropdown) AutoCompleteTextView locationTypeEditText;

    @BindView(R.id.druhy_kavy_chip_group) ChipGroup coffeeTypesChipGroup;
    @BindView(R.id.dalsi_nabidka_chip_group) ChipGroup otherOfferChipGroup;

    @BindView(R.id.price_range_dropdown) AutoCompleteTextView priceRangeEditText;
    @BindView(R.id.otviracka_dny_dropdown) AutoCompleteTextView openingDaysEditText;

    private String currentUserName;

    private Location location;

    private static final long MAX_STARI_DAT = 1000 * 60;
    private static final float LAST_PRESNOST = 100.0f;

    /**
     * Receiver for the CoffeeSiteService
     */
    private CoffeeSiteEntitiesServiceReciever coffeeSiteEntitiesServiceReceiver;
    private CoffeeSiteServiceOperationsReceiver coffeeSiteServiceReciever;

    private CoffeeSite currentCoffeeSite;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coffee_site);

        contextView = findViewById(R.id.coffeesite_create_main_scrollview);

        createCoffeeSiteViewModel = ViewModelProviders.of(this, new CoffeeSiteViewModelFactory())
                                                      .get(CoffeeSiteCreateModel.class);

        currentUserName = (String) this.getIntent().getExtras().get("currentUserName");

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            currentCoffeeSite = (CoffeeSiteMovable) bundle.getParcelable("coffeeSite");
        }
        // We are editing site here - insert input values to View from currentCoffeeSite
        // "Edit mode"
        if (currentCoffeeSite != null && currentCoffeeSite.getId() != 0) {
            mode = MODE_MODIFY;
            fillViewByCoffeeSiteData(currentCoffeeSite);
        }

        ButterKnife.bind(this);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        /**
         * Based on mode, switch on or off some menu item
         */
        if (mode == MODE_CREATE) {
            bottomNavigation.getMenu().removeItem(R.id.navigation_cancel);
        }
        if (mode == MODE_MODIFY) {
            bottomNavigation.getMenu().removeItem(R.id.navigation_save_and_aktivovat);
        }

        if (!CoffeeSiteEntitiesRepository.isDataReadedFromServer()) {
            loadCoffeeSiteEntitiesFromServer();
        }

        showCoffeeSiteTypes();
        showLocationTypes();
        showPriceRanges();

        // Otviracka - dny
        String[] OTVIRACKA_DNY = getResources().getStringArray(R.array.otviracka_dny);
        final ArrayAdapter<String> otvirackaDnyAdapter  = new ArrayAdapter(this, R.layout.dropdown_menu_popoup_item,
                                                                           OTVIRACKA_DNY);

        AutoCompleteTextView otvirackaDnyDropdown = findViewById(R.id.otviracka_dny_dropdown);
        otvirackaDnyDropdown.setAdapter(otvirackaDnyAdapter);

        // Listener for selecting items from Bottom navigation menu
        BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_ulozit:
                                // Create CoffeeSite instance from model
                                if (mode == MODE_CREATE) {
                                    currentCoffeeSite = createOrUpdateCoffeeSiteFromViewModel(null);
                                    // Save it
                                    saveCoffeeSite(currentCoffeeSite);
                                }
                                if (mode == MODE_MODIFY) {
                                    currentCoffeeSite = createOrUpdateCoffeeSiteFromViewModel(currentCoffeeSite);
                                    updateCoffeeSite(currentCoffeeSite);
                                }

                                return true;
                            case R.id.navigation_save_and_aktivovat:
                                //openFragment(SmsFragment.newInstance("", ""));
                                saveCoffeeSiteAndActivate(currentCoffeeSite);
                                return true;
                            case R.id.navigation_cancel:
                                //openFragment(NotificationFragment.newInstance("", ""));
                                cancelCoffeeSite(currentCoffeeSite);
                                return true;

                            case R.id.navigation_foto_delete:
                                deleteCoffeeSiteImage(currentCoffeeSite);
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

                MenuItem saveMenuItem = bottomNavigation.getMenu().findItem(R.id.navigation_ulozit); // save
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
            }
        };
        coffeeSiteNameEditText.addTextChangedListener(afterTextChangedListener);
        longitudeEditText.addTextChangedListener(afterTextChangedListener);
        latitudeEditText.addTextChangedListener(afterTextChangedListener);

        openingFromTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingFromTimeEditText, event) );
        openingToTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingToTimeEditText, event) );

        /**
         * Registers receiver for all operations results performed by
         * CoffeeSiteService with current CoffeeSite of this Activity
         */
        registerCoffeeSiteOperationsReceiver();
    }

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

    /**
     * Start service operation loading all CoffeeSiteEntity instancies
     * from server.
     */
    private void loadCoffeeSiteEntitiesFromServer() {
        registerCoffeeSiteEntitiesLoadReceiver();
        startCoffeeSiteServiceForEntities();
    }

    public void startCoffeeSiteServiceForEntities() {

        if (Utils.isOnline()) {
            Intent cfServiceIntent = new Intent();
            cfServiceIntent.setClass(this, CoffeeSiteService.class);
            cfServiceIntent.putExtra("operation_type", CoffeeSiteService.COFFEE_SITE_ENTITIES_LOAD);
            startService(cfServiceIntent);
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    private void registerCoffeeSiteEntitiesLoadReceiver() {
        coffeeSiteEntitiesServiceReceiver = new CoffeeSiteEntitiesServiceReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoffeeSiteService.COFFEE_SITE_ENTITY );

        LocalBroadcastManager.getInstance(this).registerReceiver(coffeeSiteEntitiesServiceReceiver, intentFilter);
    }

    private class CoffeeSiteEntitiesServiceReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("operationResult");
            showCoffeeSiteTypes();
            showLocationTypes();
            showPriceRanges();
        }
    }

    /* ---- Methods to invoke CoffeeSIteService operation based on user's selection */

    /**
     * Entry point for actions selected by user in BottomNavigationMenu
     *
     * @param coffeeSite
     */
    private void saveCoffeeSite(CoffeeSite coffeeSite) {
        startCoffeeSiteServiceOperation(coffeeSite, COFFEE_SITE_SAVE);
    }

    private void saveCoffeeSiteAndActivate(CoffeeSite coffeeSite) {
        startCoffeeSiteServiceOperation(coffeeSite, COFFEE_SITE_SAVE_AND_ACTIVATE);
    }

    private void updateCoffeeSite(CoffeeSite coffeeSite) {
        startCoffeeSiteServiceOperation(coffeeSite, COFFEE_SITE_UPDATE);
    }

    private void cancelCoffeeSite(CoffeeSite coffeeSite) {
        startCoffeeSiteServiceOperation(coffeeSite, COFFEE_SITE_CANCEL);
    }

    //TODO - image service will be created
    private void deleteCoffeeSiteImage(CoffeeSite coffeeSite) {
        //startCoffeeSiteImageServiceDelete(coffeeSite);
    }


    private void startCoffeeSiteServiceOperation(CoffeeSite coffeeSite, int operation) {
        if (Utils.isOnline()) {
            Log.i("CreateCoffeeSiteAct", "startCoffeeSiteServiceOperation");
            Intent cfServiceIntent = new Intent();
            cfServiceIntent.setClass(this, CoffeeSiteService.class);
            cfServiceIntent.putExtra("operation_type", operation);
            cfServiceIntent.putExtra("coffeeSite", (Parcelable) coffeeSite);
            startService(cfServiceIntent);
            Log.i("CreateCoffeeSiteAct", "startCoffeeSiteServiceOperation, End");
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }


    private void registerCoffeeSiteOperationsReceiver() {
        Log.i("CreateCoffeeSiteAct", "registerCoffeeSiteOperationsReceiver(), start");
        coffeeSiteServiceReciever = new CoffeeSiteServiceOperationsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoffeeSiteService.COFFEE_SITE);

        LocalBroadcastManager.getInstance(this).registerReceiver(coffeeSiteServiceReciever, intentFilter);
        Log.i("CreateCoffeeSiteAct", "registerCoffeeSiteOperationsReceiver(), end");
    }

    /**
     * Receiver callbacks for CoffeeSiteService operations invoked earlier
     */
    private class CoffeeSiteServiceOperationsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("CreateCoffeeSiteAct", "onReceive start");
            String result = intent.getStringExtra("operationResult");
            String error = intent.getStringExtra("operationError");
            int operationType = intent.getIntExtra("operationType", 0);
            Log.i("CreateCoffeeSiteAct", "result: " + result + " error: " + error + ", operationType: " + operationType);

            Log.i("Operation type", String.valueOf(operationType));

            switch (operationType) {
                case COFFEE_SITE_SAVE: {}
                case COFFEE_SITE_SAVE_AND_ACTIVATE:{
                    Log.i("CoffeeSite operation", "Saving result: " + result);
                    if (!error.isEmpty()) {
                        showCoffeeSiteCreateFailure(error);
                    } else {
                        //TODO Show Toast
                        showCoffeeSiteCreateSuccess();
                        Log.i("CreateCoffeeSiteAct", "onReceive show siuccess");
                        //TODO go back to MainActivity or to ShowUsersCoffeeSitesActivity

                    }
                } break;

                case COFFEE_SITE_UPDATE:{
                    Log.i("CoffeeSite operation", "Update result: " + result);
                    if (!error.isEmpty()) {
                        showCoffeeSiteUpdateFailure(error);
                    }
                    else {
                        //TODO Show Toast
                        showCoffeeSiteUpdateSuccess();
                        //TODO go back to MainActivity or to ShowUsersCoffeeSitesActivity

                    }
                } break;
                case COFFEE_SITE_CANCEL:{
                    Log.i("CoffeeSite operation", "Cancel result: " + result);
                    if (!error.isEmpty()) {
                        showCoffeeSiteCancelFailure(error);
                    }
                    else {
                        //TODO Show Toast
                        showCoffeeSiteCancelSuccess();
                        //TODO go back to MainActivity or to ShowUsersCoffeeSitesActivity

                    }
                } break;
                case COFFEE_SITE_DELETE:{ //TODO in the futere, when ADMIN User will be allowed to delete
                    Log.i("CoffeeSite operation", "Delete result: " + result);
                } break;
                default: break;
            }
        }
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
    private void fillViewByCoffeeSiteData(CoffeeSite coffeeSite) {
        Log.i(TAG, "fillViewByCoffeeSiteData() start");

        coffeeSiteNameEditText.setText(coffeeSite.getName());

        latitudeEditText.setText(String.valueOf(coffeeSite.getLatitude()));
        longitudeEditText.setText(String.valueOf(coffeeSite.getLongitude()));

        streetEditText.setText(coffeeSite.getUliceCP());
        cityEditText.setText(coffeeSite.getMesto());

        sourceTypeEditText.setText(coffeeSite.getTypPodniku().toString());
        locationTypeEditText.setText(coffeeSite.getTypLokality().toString());

        priceRangeEditText.setText(coffeeSite.getCena().toString());

        selectChipsInGroupAccordingText(coffeeTypesChipGroup, coffeeSite.getCoffeeSorts());

        selectChipsInGroupAccordingText(otherOfferChipGroup, coffeeSite.getOtherOffers());

        openingDaysEditText.setText(coffeeSite.getOteviraciDobaDny());
        openingFromTimeEditText.setText(coffeeSite.getOteviraciDobaHod());

        //coffeeSite.setStatusZarizeni(CoffeeSiteEntitiesRepository.getCoffeeSiteStatus("V provozu"));

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
        coffeeSite.setName(coffeeSiteNameEditText.getText().toString());

        coffeeSite.setLatitude(Double.parseDouble(latitudeEditText.getText().toString()));
        coffeeSite.setLongitude(Double.parseDouble(longitudeEditText.getText().toString()));
        coffeeSite.setUliceCP(streetEditText.getText().toString());
        coffeeSite.setMesto(cityEditText.getText().toString());

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

        coffeeSite.setCreatedByUserName(currentUserName);

        coffeeSite.setOteviraciDobaDny(openingDaysEditText.getText().toString());
        coffeeSite.setOteviraciDobaHod(openingFromTimeEditText.getText().toString() + "-" + openingToTimeEditText.getText().toString());

        coffeeSite.setStatusZarizeni(CoffeeSiteEntitiesRepository.getCoffeeSiteStatus("V provozu"));

        Log.i(TAG, "CoffeeSite created");
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

    private void showCoffeeSiteCancelFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_cancel_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSiteCreateSuccess() {
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.coffeesite_created_successfuly,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCoffeeSiteUpdateSuccess() {
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.coffeesite_updated_successfuly,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCoffeeSiteCancelSuccess() {
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.coffeesite_canceled_successfuly,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Validators for CoffeeSiteType input and for CoffeeSiteLocationType input
     */
    class SiteTypeValidator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {
            Log.v("Test", "Checking if valid: "+ text);
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
            Log.v("Test", "Checking if valid: "+ text);
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
            Log.v("Test", "Focus changed");
            if (v.getId() == R.id.location_type_dropdown && !hasFocus) {
                Log.v("Test", "Performing validation");
                ((AutoCompleteTextView)v).performValidation();
            }
        }
    }

}
