package cz.fungisoft.coffeecompass2.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.asynctask.GetSizeOfCoffeeSitesWithImageToDownloadAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.GetSizeOfCoffeeSitesWithoutImagesToDownloadAsyncTask;
import cz.fungisoft.coffeecompass2.entity.DownloadDataOverview;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.DataDownloadSizeRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Activity to download all CoffeeSites,their Comments and Images. All such items are saved into DB
 * and used in case of OFFLINE mode.
 */
public class OfflineModeSelectionActivity extends AppCompatActivity implements CoffeeSiteEntitiesServiceConnectionListener,
                                                                               DataDownloadSizeRESTResultListener,
                                                                               CoffeeSiteEntitiesService.DataDownloadIndicatorListener {

    private static final String TAG = "OfflineModeSelectionAct";

    // Saves OFFLINE mode status
    private DataForOfflineModePreferenceHelper dataDownloadPreferenceHelper;

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

    @BindView(R.id.download_size_no_image_textView)
    TextView sizeOfDataWithoutImageTextView;

    @BindView(R.id.download_size_with_image_textView)
    TextView sizeOfDataWithImageTextView;

    private final SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM. yyyy, HH:mm");

    private boolean downloadInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_mode_selection);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.offline_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Offline");

        dataDownloadPreferenceHelper = new DataForOfflineModePreferenceHelper(this);
        hideDownloadOverview();

        lastLoadedStatusTextView.setVisibility(View.VISIBLE);
        // hide progress bar until the download is in progress
        downloadProgressBar.setVisibility(View.GONE);

        if (dataDownloadPreferenceHelper.getDownloaded()) {
            lastLoadedStatusTextView.setText(getString(R.string.last_offline_data_download_status, dateFormater.format(dataDownloadPreferenceHelper.getDownloadDate())));
            showDownloadOverview(dataDownloadPreferenceHelper.getDownloadOverview());
        } else {
            lastLoadedStatusTextView.setText(getString(R.string.last_offline_data_not_yet_downloaded));
        }

        origStatusColor =  downloadingStatusTextView.getTextColors(); // saves original color

        downloadingStatusTextView.setText("");

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline(getApplicationContext())) {
                    downloadProgressBar.setVisibility(View.VISIBLE);
                    downloadButton.setEnabled(false);
                    withImagesCheckBox.setEnabled(false);

                    downloadingStatusTextView.setTextColor(origStatusColor);
                    downloadingStatusTextView.setTypeface(downloadingStatusTextView.getTypeface(), Typeface.NORMAL);

                    coffeeSiteEntitiesService.addDataDownloadFinishedListener(OfflineModeSelectionActivity.this);
                    coffeeSiteEntitiesService.populateCoffeeSites(withImagesCheckBox.isChecked(), downloadProgressBar, downloadingStatusTextView);
                    downloadInProgress = true;
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });

        // Try to get sizes of data to be downloaded
        new GetSizeOfCoffeeSitesWithImageToDownloadAsyncTask(this).execute();
        new GetSizeOfCoffeeSitesWithoutImagesToDownloadAsyncTask(this).execute();
    }


    @Override
    protected void onStart() {
        super.onStart();
        doBindCoffeeSiteEntitiesService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindCoffeeSiteEntitiesService();
        coffeeSiteEntitiesService.removeDataDownloadFinishedListener(this);
    }

    /**
     * //TODO should resume status of Offline data download, which is running in Service, independently
     * on Activity presence. We need to show current status of Download, probably provided
     * by the CoffeeSiteEntitiesService
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * //TODO should process onPause() during ongoing download ... !!! Download should run independently,
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    /**
     * Back button available only if downloading is not in progress.
     * //TODO should be implemented that blocking is not needed
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (!downloadInProgress) {
            super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
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
        // (and thus won't be supporting component replacement by other applications).
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

        downloadingStatusTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
        downloadingStatusTextView.setTypeface(downloadingStatusTextView.getTypeface(), Typeface.BOLD);
        downloadingStatusTextView.setText(R.string.data_download_success);

        hideDownloadOverview();
        showDownloadOverview(dataOverview);
        lastLoadedStatusTextView.setText(getString(R.string.last_offline_data_download_status, dateFormater.format(new Date())));
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
        // Info in RED and Bold with note that download can be repeated later - stay on this Activity
        downloadingStatusTextView.setTextColor(Color.RED);
        downloadingStatusTextView.setTypeface(downloadingStatusTextView.getTypeface(), Typeface.BOLD);
        downloadingStatusTextView.setText(R.string.data_download_failure);
        // Allow download button
        downloadButton.setEnabled(true);
        withImagesCheckBox.setEnabled(true);
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

    @Override
    public void onSizeOfAllDataToDownload(Result<Integer> result) {
        if (result instanceof Result.Success) {
            int sizeOfAllDataToDownloadMB = ((Result.Success<Integer>) result).getData() / 1024;
            sizeOfDataWithImageTextView.setText(getString(R.string.all_data_to_download_size, sizeOfAllDataToDownloadMB));
        }
    }

    @Override
    public void onSizeOfAllDataWithoutImagesToDownload(Result<Integer> result) {
        if (result instanceof Result.Success) {
            int sizeOfDataWithoutImagesToDownloadKB = ((Result.Success<Integer>) result).getData();
            if (sizeOfDataWithoutImagesToDownloadKB < 1024) {
                float sizeOfDataWithoutImagesToDownloadMBFloat = ((Result.Success<Integer>) result).getData() / 1024f;
                DecimalFormat df = new DecimalFormat("#.#");
                sizeOfDataWithoutImageTextView.setText(getString(R.string.data_without_images_to_download_size, df.format(sizeOfDataWithoutImagesToDownloadMBFloat)));
            } else {
                int sizeOfDataWithoutImagesToDownloadMB = ((Result.Success<Integer>) result).getData() / 1024;
                sizeOfDataWithoutImageTextView.setText(getString(R.string.data_without_images_to_download_size, String.valueOf(sizeOfDataWithoutImagesToDownloadMB)));
            }

        }
    }

    @Override
    public void onSizeOfDataToDownloadError(Result.Error error) {
        if (error != null) {
            // TODO - show error Toast or not?
        }
    }
}