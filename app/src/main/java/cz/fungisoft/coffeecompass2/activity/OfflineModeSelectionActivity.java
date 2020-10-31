package cz.fungisoft.coffeecompass2.activity;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModeDownloadPreferenceHelper;
import cz.fungisoft.coffeecompass2.entity.DownloadDataOverview;
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

    @BindView(R.id.last_loaded_offline_status_TextView)
    TextView lastLoadedStatusTextView;

    @BindView(R.id.downloaded_sites)
    TextView downloadedSitesOverview;

    @BindView(R.id.downloaded_offline_comments)
    TextView downloadedCommentsOverview;

    @BindView(R.id.downloaded_offline_images)
    TextView downloadedImagesOverview;

    private final SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM. yyyy, HH:mm");

    private boolean downloadInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_mode_selection);

        ButterKnife.bind(this);

        dataDownloadPreferenceHelper = new DataForOfflineModeDownloadPreferenceHelper(this);
        hideDownloadOverview();

        if (dataDownloadPreferenceHelper.getDownloaded()) {
            lastLoadedStatusTextView.setText(getString(R.string.last_offline_data_download_status, dateFormater.format(dataDownloadPreferenceHelper.getDownloadDate())));
            showDownloadOverview(dataDownloadPreferenceHelper.getDownloadOverview());
        } else {
            lastLoadedStatusTextView.setText(getString(R.string.last_offline_data_not_yet_downloaded));
        }

        origStatusColor =  downloadingStatusTextView.getTextColors(); //saves original color

        downloadingStatusTextView.setText("");

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    downloadProgressBar.setVisibility(View.VISIBLE);
                    downloadButton.setEnabled(false);
                    downloadingStatusTextView.setTextColor(origStatusColor);
                    coffeeSiteEntitiesService.addDataDownloadFinishedListener(OfflineModeSelectionActivity.this);
                    coffeeSiteEntitiesService.populateCoffeeSites(withImagesCheckBox.isChecked(), downloadProgressBar, downloadingStatusTextView);
                    //clearDownloadOverview();
                    downloadInProgress = true;
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

//        activityState.p("downloadInProgress", downloadInProgress);
//        activityState.putBoolean("includingImages", withImagesCheckBox.isChecked());
//        onSaveInstanceState(activityState);
    }

    /**
     * Back button available only if downloading is not in progress.
     * //TODO should be implemented that blocking is not needed
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (!downloadInProgress) {
            goToMainActivity();
            //super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
        }
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
        downloadButton.setEnabled(!coffeeSiteEntitiesService.isDownloadInProgress());
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
    public void onAllDataForOfflineModeDownloaded(DownloadDataOverview dataOverview) {
        downloadInProgress = false;

        downloadingStatusTextView.setText( R.string.data_download_success);

        hideDownloadOverview();
        showDownloadOverview(dataOverview);
        lastLoadedStatusTextView.setText(getString(R.string.last_offline_data_download_status, dateFormater.format(new Date())));

        downloadButton.setEnabled(true);
    }

    private void showDownloadOverview(DownloadDataOverview dataOverview) {
        lastLoadedStatusTextView.setVisibility(View.VISIBLE);

        if (dataOverview.numOfSitesDownloaded > 0) {
            downloadedSitesOverview.setVisibility(View.VISIBLE);
            downloadedSitesOverview.setText(getString(R.string.offline_data_downloaded_sites, String.valueOf(dataOverview.numOfSitesDownloaded)));
        }
        if (dataOverview.numOfCommentsDownloaded > 0) {
            downloadedCommentsOverview.setVisibility(View.VISIBLE);
            downloadedCommentsOverview.setText(getString(R.string.offline_data_downloaded_comments, String.valueOf(dataOverview.numOfCommentsDownloaded)));
        }
        if (dataOverview.numOfImagesDownloaded > 0) {
            downloadedImagesOverview.setVisibility(View.VISIBLE);
            downloadedImagesOverview.setText(getString(R.string.offline_data_downloaded_images, String.valueOf(dataOverview.numOfImagesDownloaded)));
        }
    }

    @Override
    public void onDataForOfflineModeDownloadFailed() {
        downloadInProgress = false;
        //Info in RED with note that download can be repeated later - stay on this Activity
        downloadingStatusTextView.setTextColor(Color.RED);
        downloadingStatusTextView.setText(R.string.data_download_failure);
        // Allow download button
        downloadButton.setEnabled(true);
    }

    private void hideDownloadOverview() {
        lastLoadedStatusTextView.setVisibility(View.GONE);
        downloadedSitesOverview.setVisibility(View.GONE);
        downloadedCommentsOverview.setVisibility(View.GONE);
        downloadedImagesOverview.setVisibility(View.GONE);
    }

    private void clearDownloadOverview() {
        lastLoadedStatusTextView.setText("");
        downloadedSitesOverview.setText("");
        downloadedCommentsOverview.setText("");
        downloadedImagesOverview.setText("");
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(OfflineModeSelectionActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        //finish();
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