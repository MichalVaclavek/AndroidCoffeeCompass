package cz.fungisoft.coffeecompass2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CommentRepository;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

public class OfflineModeSelectionActivity extends AppCompatActivity implements CoffeeSiteEntitiesServiceConnectionListener {

    private static final String TAG = "OfflineModeSelectionAct";

    @BindView(R.id.textOfflineDownloadStatus)
    TextView downloadingStatusTextView;

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

        ButterKnife.bind(this);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    downloadProgressBar.setVisibility(View.VISIBLE);

                    coffeeSiteEntitiesService.populateCoffeeSites(withImagesCheckBox.isChecked(), downloadProgressBar, downloadingStatusTextView);

                    //Comments are always downloaded
//                    CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(getApplicationContext());
//                    CommentRepository commentRepository = CommentRepository.getInstance(db);
//                    commentRepository.populateComments();
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
    protected void onStart() {
        super.onStart();
        doBindCoffeeSiteEntitiesService();
    }


    @Override
    protected void onStop() {
        doUnbindCoffeeSiteEntitiesService();
        super.onStop();
    }

}