package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;

public class CreateCoffeeSiteActivity extends AppCompatActivity implements TimePickerFragment.EnterTimeDialogListener {

    BottomNavigationView bottomNavigation;

    //TextInputEditText openingFromTimeEditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coffee_site);

        createCoffeeSiteViewModel = ViewModelProviders.of(this, new CoffeeSiteViewModelFactory())
                                                      .get(CoffeeSiteCreateModel.class);

        ButterKnife.bind(this);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Typ zdroje
        String[] SITE_TYPES = getResources().getStringArray(R.array.coffee_site_type);

        final ArrayAdapter<String> siteTypesAdapter = new ArrayAdapter(this,  R.layout.dropdown_menu_popoup_item,
                                                          SITE_TYPES);

        AutoCompleteTextView siteTypeDropdown = findViewById(R.id.site_type_dropdown);
        siteTypeDropdown.setAdapter(siteTypesAdapter);

        // Typ lokality
        String[] LOCATION_TYPES = getResources().getStringArray(R.array.location_type);

        final ArrayAdapter<String> locationTypesAdapter = new ArrayAdapter(this,  R.layout.dropdown_menu_popoup_item,
                                                                            LOCATION_TYPES);

        AutoCompleteTextView locationTypesDropdown = findViewById(R.id.location_type_dropdown);
        locationTypesDropdown.setAdapter(locationTypesAdapter);

        // Cenovy rozsah
        String[] PRICE_RANGES = getResources().getStringArray(R.array.cena_range);

        final ArrayAdapter<String> priceRangesAdapter = new ArrayAdapter(this,  R.layout.dropdown_menu_popoup_item,
                                                                          PRICE_RANGES);

        AutoCompleteTextView priceRangesDropdown = findViewById(R.id.price_range_dropdown);
        priceRangesDropdown.setAdapter(priceRangesAdapter);

        // Otviracka - dny
        String[] OTVIRACKA_DNY = getResources().getStringArray(R.array.otviracka_dny);

        final ArrayAdapter<String> otvirackaDnyAdapter = new ArrayAdapter(this,  R.layout.dropdown_menu_popoup_item,
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
                                CoffeeSite coffeeSite = createCoffeeSiteFromViewModel();
                                // Save it
                                saveCoffeeSite(coffeeSite);
                                return true;
                            case R.id.navigation_aktivovat:
                                //openFragment(SmsFragment.newInstance("", ""));
                                return true;
                            //case R.id.navigation_edit_site:
                                //openFragment(NotificationFragment.newInstance("", ""));
                             //   return true;
                            case R.id.navigation_delete:
                                //openFragment(NotificationFragment.newInstance("", ""));
                                return true;

                            case R.id.navigation_foto:
                                //openFragment(NotificationFragment.newInstance("", ""));
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

        // From and To opening times input
        //openingFromTimeEditText = findViewById(R.id.time_od_input_editText);
        //openingToTimeEditText = findViewById(R.id.time_do_input_editText);

        openingFromTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingFromTimeEditText, event) );
        openingToTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingToTimeEditText, event) );
    }

    /**
     * Calls service level to save the CoffeeSite instance to server or anywhere else
     * In this case, async task is created and called execute
     * After finish, Activate menu item is enabled. Also Delete menu item is enabled
     * <p>
     *
     * @param coffeeSite
     */
    private void saveCoffeeSite(CoffeeSite coffeeSite) {
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
     * Creates CoffeeSite instance based on data from this Activity View data.
     */
    private CoffeeSite createCoffeeSiteFromViewModel() {
        CoffeeSite coffeeSite = new CoffeeSite();

        coffeeSite.setName(coffeeSiteNameEditText.getText().toString());
        coffeeSite.setId(0);
        coffeeSite.setLatitude(Double.parseDouble(latitudeEditText.getText().toString()));
        coffeeSite.setLongitude(Double.parseDouble(longitudeEditText.getText().toString()));
        coffeeSite.setUliceCP(streetEditText.getText().toString());
        coffeeSite.setMesto(cityEditText.getText().toString());
        //coffeeSite.setTypPodniku(sourceTypeEditText.getText().toString());
        coffeeSite.setTypPodniku(CoffeeSiteEntitiesRepository.getCoffeeSiteType(sourceTypeEditText.getText().toString()));
        //coffeeSite.setTypLokality(locationTypeEditText.getText().toString());
        coffeeSite.setTypLokality(CoffeeSiteEntitiesRepository.getSiteLocationType(locationTypeEditText.getText().toString()));

        //coffeeSite.setCena(priceRangeEditText.getText().toString());
        coffeeSite.setCena(CoffeeSiteEntitiesRepository.getPriceRange(priceRangeEditText.getText().toString()));


        String[] selectedCoffeeType = getSelectedChipsStrings(coffeeTypesChipGroup);
        //coffeeSite.setCoffeeSorts(selectedCoffeeType);
        coffeeSite.setCoffeeSorts(CoffeeSiteEntitiesRepository.getCoffeeSortsList(selectedCoffeeType));

        String[] selectedOtherOffer = getSelectedChipsStrings(otherOfferChipGroup);
        coffeeSite.setOtherOffers(CoffeeSiteEntitiesRepository.getOtherOffersList(selectedOtherOffer));

        // TODO insert UserAcountService and get the logged-in user
        //coffeeSite.setCreatedByUserName();

        coffeeSite.setOteviraciDobaDny(openingDaysEditText.getText().toString());
        coffeeSite.setOteviraciDobaHod(openingFromTimeEditText.getText().toString() + "-" + openingToTimeEditText.getText().toString());

        coffeeSite.setStatusZarizeni(CoffeeSiteEntitiesRepository.getCoffeeSiteStatus("V provozu"));
        coffeeSite.setCreatedOn(new Date());

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

}
