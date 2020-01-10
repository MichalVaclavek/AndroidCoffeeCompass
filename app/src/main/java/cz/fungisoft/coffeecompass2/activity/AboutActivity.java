package cz.fungisoft.coffeecompass2.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import cz.fungisoft.coffeecompass2.R;

/**
 * Shows the About app. info, author, version, aim of the app.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
