package cz.fungisoft.coffeecompass.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cz.fungisoft.coffeecompass.R;

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
