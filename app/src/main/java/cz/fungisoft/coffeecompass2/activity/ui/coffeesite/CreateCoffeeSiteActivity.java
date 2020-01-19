package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import cz.fungisoft.coffeecompass2.R;

public class CreateCoffeeSiteActivity extends AppCompatActivity implements TimePickerFragment.EnterTimeDialogListener {

    BottomNavigationView bottomNavigation;

    //TextInputEditText openingFromTimeEditText;
    EditText openingFromTimeEditText;
    TextInputEditText openingToTimeEditText;

    TimePickerFragment timeFromToPicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_coffee_site);

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

        BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_ulozit:
                                //openFragment(HomeFragment.newInstance("", ""));
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

        // From and To opening times input
        openingFromTimeEditText = findViewById(R.id.time_od_input_editText);
        openingToTimeEditText = findViewById(R.id.time_do_input_editText);

        openingFromTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingFromTimeEditText, event) );
        openingToTimeEditText.setOnTouchListener((view, event) -> showTimePickerDialog(openingToTimeEditText, event) );
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


}
