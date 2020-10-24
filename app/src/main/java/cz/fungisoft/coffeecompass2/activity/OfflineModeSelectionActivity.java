package cz.fungisoft.coffeecompass2.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModeDownloadPreferenceHelper;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Activity to download all CoffeeSites,their Comments and Images. All such items are saved into DB
 * and used in case of OFFLINE mode.
 */
public class OfflineModeSelectionActivity extends AppCompatActivity implements CoffeeSiteEntitiesServiceConnectionListener,
                                                                               CoffeeSiteEntitiesService.DataDownloadIndicatorListener {

    private static final String TAG = "OfflineModeSelectionAct";

    // Saves OFFLINE mode status
    private DataForOfflineModeDownloadPreferenceHelper dataDownloadPreferenceHelper;
    private boolean offlineModeOn = false;

    @BindView(R.id.mainOfflineLayout)
    ConstraintLayout mainOfflineLayout;

    @BindView(R.id.offlineActivityMainLinearLayout)
    LinearLayout mainLinearLayout;

    @BindView(R.id.textOfflineDownloadStatus)
    TextView downloadingStatusTextView;

    ColorStateList origStatusColor;

    @BindView(R.id.buttonOfflineDownload)
    Button downloadButton;

    @BindView(R.id.progressBarOfflineDownload)
    ProgressBar downloadProgressBar;

    @BindView(R.id.checkBoxOfflinePicturesIncluded)
    CheckBox withImagesCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_mode_selection);

        dataDownloadPreferenceHelper = new DataForOfflineModeDownloadPreferenceHelper(this);

        ButterKnife.bind(this);

        origStatusColor =  downloadingStatusTextView.getTextColors(); //saves original color

        downloadButton.setEnabled(true);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    downloadProgressBar.setVisibility(View.VISIBLE);
                    downloadButton.setEnabled(false);
                    downloadingStatusTextView.setTextColor(origStatusColor);
                    coffeeSiteEntitiesService.addDataDownloadFinishedListener(OfflineModeSelectionActivity.this);
                    coffeeSiteEntitiesService.populateCoffeeSites(withImagesCheckBox.isChecked(), downloadProgressBar, downloadingStatusTextView);
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });
    }

    // ** CoffeeSiteEntitiesService connection/disconnection ** //

    protected CoffeeSiteEntitiesService coffeeSiteEntitiesService;
    private CoffeeSiteEntitiesServiceConnector coffeeSiteEntitiesServiceConnector;

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
        coffeeSiteEntitiesServiceConnector.addCoffeeSiteEntitiesServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteEntitiesService.class),
                coffeeSiteEntitiesServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteEntitiesService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteEntitiesService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    @Override
    public void onCoffeeSiteEntitiesServiceConnected() {
        coffeeSiteEntitiesService = coffeeSiteEntitiesServiceConnector.getCoffeeSiteEntitiesService();
    }

    private void doUnbindCoffeeSiteEntitiesService() {
        if (mShouldUnbindCoffeeSiteEntitiesService) {
            // Release information about the service's state.
            coffeeSiteEntitiesServiceConnector.removeCoffeeSiteEntitiesServiceConnectionListener(this);
            unbindService( coffeeSiteEntitiesServiceConnector);
            mShouldUnbindCoffeeSiteEntitiesService = false;
        }
    }

    @Override
    public void onAllDataForOfflineModeDownloaded() {
        // Info () and return to MainActivity, i.e. go back
        Toast toast = Toast.makeText(getApplicationContext(),
                "Data stažena úspěšně.",
                Toast.LENGTH_LONG);
        toast.show();

        dataDownloadPreferenceHelper.putDownloaded(true);
        dataDownloadPreferenceHelper.putDownloadDate(new Date());

        this.onBackPressed();
    }

    @Override
    public void onDataForOfflineModeDownloadFailed() {
        //Info in RED with note that download can be repeated later - stay on this Activity
        downloadingStatusTextView.setTextColor(Color.RED);
        downloadingStatusTextView.setText("Data se nepodařilo stáhnout. Zkontrolujte připojení k internetu nebo zkuste později ...");
        // Allow download button
        downloadButton.setEnabled(true);
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(OfflineModeSelectionActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindCoffeeSiteEntitiesService();
    }


    @Override
    protected void onStop() {
        doUnbindCoffeeSiteEntitiesService();
        coffeeSiteEntitiesService.removeDataDownloadFinishedListener(this);
        super.onStop();
    }

}