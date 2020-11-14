package cz.fungisoft.coffeecompass2.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModeDownloadPreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentsPageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteEntitiesServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsPageLoadOperationListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetAllCoffeeSitesAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetAllCoffeeSitesPaginatedAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ReadCoffeeSiteEntitiesAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.DownloadDataOverview;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntityRepositories;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.entity.repository.CommentRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesLoadRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.NetworkStateReceiver;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_ENTITIES_LOAD;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_LOAD_ALL;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_LOAD_ALL_FIRST_PAGE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_LOAD_ALL_NEXT_PAGE;

/**
 * A Service class to hold CoffeeSite entities classes.
 * Is able to load all available instancies of CoffeeSite entities
 * and save them into EntitiesRepository.
 *
 * It has its own Service connector and is not part of other
 * CoffeeSiteServices with their common service connector.
 */
public class CoffeeSiteEntitiesService extends LifecycleService
                                       implements CoffeeSiteEntitiesLoadRESTResultListener,
                                                  CoffeeSitesRESTResultListener,
                                                  CoffeeSiteDatabase.DbDeleteEndListener,
                                                  CommentsPageLoadOperationListener,
                                                  ImageUtil.BunchOfImagesDownloadListener {

    static final String TAG = "CoffeeSiteEntitiesSrv";

    private final ImageUtil imageUtil = ImageUtil.getInstance();

    private CoffeeSiteDatabase db;

    private static CoffeeSiteRepository coffeeSiteRepository;

    // Saves OFFLINE mode status
    private DataForOfflineModeDownloadPreferenceHelper dataDownloadPreferenceHelper;

    private boolean downloadInProgress = false;

    public boolean isDownloadInProgress() {
        return downloadInProgress;
    }


    /**
     * To indicate, that downloading of all data needed for OFFLINE mode finished
     */
    public interface DataDownloadIndicatorListener {
        void onAllDataForOfflineModeDownloaded(DownloadDataOverview dataOverview);

        void onDataForOfflineModeDownloadFailed();
    }

    private List<DataDownloadIndicatorListener> dataDownloadFinishedListeners = new ArrayList<>();

    public void addDataDownloadFinishedListener(DataDownloadIndicatorListener listener) {
        if (!dataDownloadFinishedListeners.contains(listener)) {
            dataDownloadFinishedListeners.add(listener);
        }
    }
    public void removeDataDownloadFinishedListener(DataDownloadIndicatorListener listener) {
        dataDownloadFinishedListeners.remove(listener);
    }

    /**
     * Detector of internet connection change
     */
    private final NetworkStateReceiver networkChangeStateReceiver = new NetworkStateReceiver();


    // Listeners, usually Activities, which called respective service method
    // and wants to be informed about result later, as all the operations are Async
    private List<CoffeeSiteEntitiesServiceOperationsListener> coffeeSiteEntitiesOperationsListeners = new ArrayList<>();

    public void addCoffeeSiteEntitiesOperationsListener(CoffeeSiteEntitiesServiceOperationsListener listener) {
        if (!coffeeSiteEntitiesOperationsListeners.contains(listener)) {
            coffeeSiteEntitiesOperationsListeners.add(listener);
        }
    }

    public void removeCoffeeSiteEntitiesOperationsListener(CoffeeSiteEntitiesServiceOperationsListener listener) {
        coffeeSiteEntitiesOperationsListeners.remove(listener);
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteEntitiesService.LocalBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSiteEntitiesService getService() {
            return CoffeeSiteEntitiesService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeStateReceiver, filter);

        db = CoffeeSiteDatabase.getDatabase(getApplicationContext());
        db.addDbDeleteEndListener(this);
        coffeeSiteRepository = new CoffeeSiteRepository(db);
        db.getOpenHelper().getWritableDatabase(); // to invoke onOpen() of the DB

        dataDownloadPreferenceHelper = new DataForOfflineModeDownloadPreferenceHelper(this);

        Log.d(TAG, "Service started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeStateReceiver);
        db.removeDbDeleteEndListener(this);
        Log.d(TAG, "Service destroyed.");
    }

    // Indication that data are available in the repository i.e. where read from server
    public boolean isDataReadFromServer() {
        return CoffeeSiteEntityRepositories.isDataSaved();
    }

    /**
     * Usually called, when MainActivity is destroyed.
     * This leads to new load of CoffeeSite entities, when the app. runs again.
     *
     * @param dataRead
     */
    public void resetDataReadFromServer() {
        CoffeeSiteEntityRepositories.setDataSaved(false);
    }

    /**
     * Deletes current CS entities data from DB and loads and saves new ones
     */
    public void populateCSEntities() {
        downloadInProgress = true;
        db.deleteCSEntitiesAsync();
    }

    @Override
    public void onCSEntitiesDeletedEnd() {
        CoffeeSiteEntityRepositories.setDataSaved(false);
        readAndSaveAllEntitiesFromServer();
    }

    /**
     * Methods to start running AsyncTask
     **/

    private void readAndSaveAllEntitiesFromServer() {
        if (Utils.isOnline()) {
            downloadInProgress = true;
            CoffeeSiteEntityRepositories entitiesRepository = CoffeeSiteEntityRepositories.getInstance(db);
            new ReadCoffeeSiteEntitiesAsyncTask(COFFEE_SITE_ENTITIES_LOAD, this, entitiesRepository).execute();
        }
    }

    @Override
    public void onCoffeeSiteEntitiesLoaded(Result<Boolean> result) {
        if (result instanceof Result.Success) {
            downloadInProgress = false;
            informClientAboutCSEntitiesLoadResult( ((Result.Success<Boolean>) result).getData());
        }
    }

    private void informClientAboutCSEntitiesLoadResult(Boolean result) {
        for (CoffeeSiteEntitiesServiceOperationsListener listener : coffeeSiteEntitiesOperationsListeners) {
            listener.onCoffeeSiteEntitiesLoaded(result);
        }
    }

    /**
     * If images should be read with CoffeeSites
     */
    private boolean includingImages;

    /**
     * To show progress bar of the download
     */
    private ProgressBar downloadProgressBar;

    private TextView downloadingStatusTextView;


    /**
     * Deletes current CoffeeSites data from DB and loads and saves new ones.
     * CoffeeSite's data includes images (if requested) and Comments, which are
     * downloaded subsequently after main CoffeeSite data.
     */
    public void populateCoffeeSites(boolean includingImages, ProgressBar downloadProgressBar, TextView downloadingStatusTextView) {
        this.includingImages = includingImages;
        this.downloadProgressBar = downloadProgressBar;
        this.downloadingStatusTextView = downloadingStatusTextView;
        this.downloadingStatusTextView.setText("Stahování dat o lokacích ...");
        db.deleteCoffeeSitesAsync();
    }

    @Override
    public void onCoffeeSitesDeletedEnd() {
        readAndSaveAllCoffeeSitesPaginatedFromServer();
    }

    private void readAndSaveAllCoffeeSitesFromServer() {
        if (Utils.isOnline()) {
            downloadInProgress = true;
            new GetAllCoffeeSitesAsyncTask(COFFEE_SITE_LOAD_ALL, this).execute();
        }
    }

    private int requestedPage;
    private boolean isLastPage = false;;
    public static final int PAGE_SIZE = 20;
    private int sitesAlreadyDownloaded = 0;

    private int numOfSitesWithImages = 0;


    private void readAndSaveAllCoffeeSitesPaginatedFromServer() {
        if (Utils.isOnline()) {
            requestedPage = 1;
            sitesAlreadyDownloaded = 0;
            numOfSitesWithImages = 0;
            downloadProgressBar.setProgress(0);
            downloadInProgress = true;
            new GetAllCoffeeSitesPaginatedAsyncTask(COFFEE_SITE_LOAD_ALL_FIRST_PAGE, requestedPage, PAGE_SIZE, this).execute();
        }
    }

    /**
     * On all CoffeeSites returned from server.
     *
     * @param oper identifier of REST operation which lead to call this method
     * @param result - success or error result of the operation. If success, then List<CoffeeSite> is returned in result = new Result.Success<>(coffeeSites);
     */
    @Override
    public void onCoffeeSitesReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {
        downloadInProgress = false;
        if (result instanceof Result.Success) {

            List<CoffeeSite> allCoffeeSites = ((Result.Success<List<CoffeeSite>>) result).getData();

            coffeeSiteRepository.insertAll(allCoffeeSites);

            // We do not need to wait until insertAll() finishes as it is faster then downloading the first image
            if (this.includingImages) {
                for (CoffeeSite cs : allCoffeeSites) {
                    if (!cs.getMainImageURL().isEmpty()) {
                        numOfSitesWithImages++;
                        imageUtil.downloadAndSaveImage(getApplicationContext(), cs.getMainImageURL(), ImageUtil.COFFEESITE_IMAGE_DIR, cs.getMainImageFileName());
                    }
                }
            } else {
                informClientAboutAllCoffeeSitesLoadResult(false);
            }
        } else {
            informClientAboutAllCoffeeSitesLoadResult(false);
        }
    }

    /**
     * On CoffeeSites Page returned from server.
     *
     * @param oper identifier of REST operation which lead to call this method
     * @param result - success or error result of the operation. If success, then List<CoffeeSite> is returned in result = new Result.Success<>(coffeeSites);
     */
    @Override
    public void onCoffeeSitesPageReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {
        downloadInProgress = false;

        if (result instanceof Result.Success) {
            if (((Result.Success<List<CoffeeSite>>) result).getData() instanceof CoffeeSitePageEnvelope) {

                CoffeeSitePageEnvelope coffeeSitesPage = ((Result.Success<CoffeeSitePageEnvelope>) result).getData();

                if (coffeeSitesPage != null) {
                    List<CoffeeSite> coffeeSitesList = coffeeSitesPage.getContent();

                    for (CoffeeSite cs : coffeeSitesList) {
                        if (!cs.getMainImageURL().isEmpty() ) {
                            numOfSitesWithImages++;
                        }
                    }

                    isLastPage = coffeeSitesPage.getLast();

                    coffeeSiteRepository.insertAll(coffeeSitesList);

                    // Update progress bar
                    if (coffeeSitesPage.getFirst()) {
                        downloadProgressBar.setMax(coffeeSitesPage.getTotalElements());
                    }
                    sitesAlreadyDownloaded += coffeeSitesPage.getNumberOfElements();
                    downloadProgressBar.setProgress(sitesAlreadyDownloaded);

                    // We do not need to wait until insertAll() finishes as it is faster then downloading the first image
                    if (!isLastPage) {
                        requestedPage++;
                        new GetAllCoffeeSitesPaginatedAsyncTask(COFFEE_SITE_LOAD_ALL_NEXT_PAGE, requestedPage, PAGE_SIZE, this).execute();
                    } else { // all pages downloaded
                        downloadComments();
                    }
                }
            }
        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                informClientAboutAllCoffeeSitesLoadResult(false);
                Log.e(TAG, "Error when obtaining coffee sites. " +  error.getDetail());
            }
        }
    }

    private void downloadImages() {
        this.downloadingStatusTextView.setText("Stahování fotek lokací ...");
        downloadProgressBar.setProgress(0);
        downloadProgressBar.setMax(numOfSitesWithImages);

        imageUtil.setProgressBar(downloadProgressBar);
        imageUtil.resetAlreadySavedImagesCounter();
        imageUtil.setNumberOfImagesRequestedToDownload(numOfSitesWithImages);
        imageUtil.setNumberOfDownloadsListener(this);

        coffeeSiteRepository.getAllCoffeeSitesWithImage().observe(this, new Observer<List<CoffeeSite>>() {
            @Override
            public void onChanged(@Nullable final List<CoffeeSite> coffeeSitesWithImage) {
                // Download images
                if (includingImages) {
                    downloadInProgress = true;
                    for (CoffeeSite cs : coffeeSitesWithImage) {
                        if (cs != null && !cs.getMainImageFileName().isEmpty()) {
                            imageUtil.downloadAndSaveImage(getApplicationContext(), cs.getMainImageURL(), ImageUtil.COFFEESITE_IMAGE_DIR, cs.getMainImageFileName());
                        }
                    }
                }
            }
        });
    }

    private CommentRepository commentRepository;

    private final int COMMENTS_PAGE_SIZE = 10;

    private int alreadyDownloadedComments = 0;

    private void downloadComments() {

        this.downloadingStatusTextView.setText("Stahování komentářů k lokacím ...");
        downloadProgressBar.setProgress(0);
        alreadyDownloadedComments = 0;

        downloadInProgress = true;
        commentRepository = CommentRepository.getInstance(db);
        commentRepository.populateCommentsByPages(this, COMMENTS_PAGE_SIZE);
        Log.i(TAG, "Start of Comments download.");
    }

    /**
     * Called by CommentsRepository
     *
     * @param comments
     */
    @Override
    public void onCommentsPageLoaded(CommentsPageEnvelope comments) {
        downloadInProgress = false;
        downloadProgressBar.setMax(comments.getTotalElements());
        alreadyDownloadedComments += comments.getNumberOfElements();
        downloadProgressBar.setProgress(alreadyDownloadedComments);

        // Continue loading images
        if (comments.getLast()) {
            if (includingImages) {
                downloadImages();
            } else { // all Comments downloaded, we can return to OfflineModeSelectionActivity as download of images were not requested
                informClientAboutAllCoffeeSitesLoadResult(true);
            }
        }
    }

    /**
     * This should finish all downloads (if images download was also requested), we can indicate successful download and return to OfflineModeSelectionActivity
     */
    @Override
    public void onRequestedNumberOfImagesToDownloadReached() {
        downloadInProgress = false;
        informClientAboutAllCoffeeSitesLoadResult(true);
    }

    private void informClientAboutAllCoffeeSitesLoadResult(Boolean result) {
        for (CoffeeSiteEntitiesServiceOperationsListener listener : coffeeSiteEntitiesOperationsListeners) {
            listener.onAllCoffeeSitesLoaded(result);
        }
        for (DataDownloadIndicatorListener listener : dataDownloadFinishedListeners) {
            if (result) {
                DownloadDataOverview overview = new DownloadDataOverview(sitesAlreadyDownloaded, alreadyDownloadedComments, includingImages ? numOfSitesWithImages : 0);
                listener.onAllDataForOfflineModeDownloaded(overview);
                // Saves status of data download
                dataDownloadPreferenceHelper.putDownloadOverview(overview);
                dataDownloadPreferenceHelper.putDownloaded(true);
                dataDownloadPreferenceHelper.putDownloadDate(new Date());
            } else {
                listener.onDataForOfflineModeDownloadFailed();
            }
        }
    }

}
