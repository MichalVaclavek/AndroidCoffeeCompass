package cz.fungisoft.coffeecompass2.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.util.Log;
import android.widget.ProgressBar;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImagesApiRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Copy from https://androidwave.com/capture-image-from-camera-gallery/
 */
public class ImageUtil {

    private static final String TAG = "ImageUtil: ";

    private ImageUtil() {
    }

    private static ImageUtil instance;
    private Picasso offlineDownloadPicasso;

    /**
     * If instance is requiered
     *
     * @return
     */
    public static ImageUtil getInstance() {
        if (instance == null) {
            instance = new ImageUtil();
        }

        return instance;
    }

    private synchronized Picasso getOfflineDownloadPicasso(Context context) {
        if (offlineDownloadPicasso == null) {
            offlineDownloadPicasso = new Picasso.Builder(context.getApplicationContext())
                    .downloader(new OkHttp3Downloader(RetrofitClientProvider.getInstance().getClient()))
                    .build();
        }
        return offlineDownloadPicasso;
    }

    static File compressImage(File imageFile, int reqWidth, int reqHeight,
                              Bitmap.CompressFormat compressFormat, int quality, String destinationPath)
            throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            // write the compressed bitmap at the destination specified by destinationPath
            decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(compressFormat, quality,
                    fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        return new File(destinationPath);
    }

    static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight)
            throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        //check the rotation of the image and display it properly
        ExifInterface exif;
        exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        scaledBitmap =
                Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                        matrix, true);
        return scaledBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                             int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public static final String COFFEESITE_IMAGE_DIR = "coffeesitephotos";

    public static File getImageFile(Context appContext, String imageDir, String imageFileName) {
        ContextWrapper cw = new ContextWrapper(appContext);
        File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
        return new File(directory, imageFileName);
    }

    public static File getImageFile(String imageFilePath) {
        return new File(imageFilePath);
    }

    public static File getCoffeeSiteImageFile(Context appContext, CoffeeSite coffeeSite) {
        ContextWrapper cw = new ContextWrapper(appContext);
        if (!coffeeSite.getImageFileName().isEmpty()) {
            return new File(cw.getDir(COFFEESITE_IMAGE_DIR, Context.MODE_PRIVATE), coffeeSite.getImageFileName());
        }
        return getImageFile(coffeeSite.getMainImageFilePath());
    }

    public static void deleteCoffeeSiteImage(Context appContext, CoffeeSite coffeeSite) {
        ContextWrapper cw = new ContextWrapper(appContext);
        if (!coffeeSite.getImageFileName().isEmpty()) {
            File target = new File(cw.getDir(COFFEESITE_IMAGE_DIR, Context.MODE_PRIVATE), coffeeSite.getImageFileName());
            deleteImageFile(target);
        }
        if (!coffeeSite.getMainImageFilePath().isEmpty()) {
            File target = new File(coffeeSite.getMainImageFilePath());
            deleteImageFile(target);
        }
    }

    private static void deleteImageFile(File imageFile) {
        if (imageFile.exists() && imageFile.isFile() && imageFile.canWrite()) {
            imageFile.delete();
            Log.d("delete_file: ", imageFile.getName());
        }
    }

    /* ============== DOWNLOAD and SAVING IMAGE using Picasso and FileOutputStream =========== */

    /**
     * To indicate, that number of images already downloaded reached the number
     * of images requested to download.
     */
    public interface BunchOfImagesDownloadListener {
        void onRequestedNumberOfImagesToDownloadReached();
    }

    public void setNumberOfDownloadsListener(BunchOfImagesDownloadListener aNumberOfDownloadsListener) {
        numberOfDownloadsListener = aNumberOfDownloadsListener;
    }

    /**
     * Only one listener is enough to listen, that all requested images has been downloaded.
     * It is expected, that only one process will react on this event
     */
    private BunchOfImagesDownloadListener numberOfDownloadsListener;

    private AtomicInteger alreadySavedImagesCounter = new AtomicInteger(0);

    /**
     * Counter for all processed image download attempts (successful or not).
     * Used to track progress and detect when all requested downloads have been attempted.
     */
    private AtomicInteger alreadyProcessedImagesCounter = new AtomicInteger(0);

    private static final int MAX_DOWNLOAD_RETRIES = 3;
    private static final long DOWNLOAD_RETRY_DELAY_MS = 1_500L;

    private int numberOfImagesRequestedToDownload = 0;

    public void setNumberOfImagesRequestedToDownload(int numberOfImagesRequestedToDownload) {
        this.numberOfImagesRequestedToDownload = numberOfImagesRequestedToDownload;
    }


    private ProgressBar mProgressBar;

    public void resetAlreadySavedImagesCounter() {
        alreadySavedImagesCounter = new AtomicInteger(0);
        alreadyProcessedImagesCounter = new AtomicInteger(0);
        numberOfImagesRequestedToDownload = 0;
    }

    public int getAlreadySavedImagesCounter() {
        return alreadySavedImagesCounter.get();
    }

    public int getAlreadyProcessedImagesCounter() {
        return alreadyProcessedImagesCounter.get();
    }

    /**
     * To indicate number of already saved image files. Expects, that max is already set
     * in this progressBar.
     *
     * @param progressBar
     */
    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    /**
     * Loading image bytes by Picasso and saving it into file system app's work directory
     * as image JPG file.<br>
     * Taken from https://stackoverflow.com/questions/53479820/save-image-in-external-storage-by-using-picasso-from-url-how-to-download-image
     * and adjusted.
     *
     * @param context
     * @param myUrl
     * @param imageDir
     * @param imageFileName
     */
    public void downloadAndSaveImage(final Context context, final String myUrl, final String imageDir, final String imageFileName) {
        downloadAndSaveImage(context, myUrl, imageDir, imageFileName, 1);
    }

    private void downloadAndSaveImage(final Context context,
                                      final String myUrl,
                                      final String imageDir,
                                      final String imageFileName,
                                      final int attemptNumber) {
        AsyncRunner.runInBackground(() -> {
            String myImageFileName;
            boolean downloadSucceeded = false;
            try {
                ContextWrapper cw = new ContextWrapper(context);
                final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
                final File myImageFile = new File(directory, imageFileName);
                myImageFileName = myImageFile.getAbsolutePath();
                Bitmap bitmap = getOfflineDownloadPicasso(context).load(myUrl).get();
                try (FileOutputStream fos = new FileOutputStream(myImageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    Log.i(TAG, "image size: " + bitmap.getByteCount());
                    Log.i(TAG, "image saved to >>> " + myImageFileName);
                    alreadySavedImagesCounter.incrementAndGet();
                    downloadSucceeded = true;
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save image on attempt " + attemptNumber + " for URL " + myUrl, e);
                    myImageFileName = "";
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to download image on attempt " + attemptNumber + " for URL " + myUrl, e);
                myImageFileName = "";
            }

            if (!downloadSucceeded && attemptNumber < MAX_DOWNLOAD_RETRIES) {
                Log.w(TAG, "Retrying image download " + attemptNumber + "/" + MAX_DOWNLOAD_RETRIES
                        + " for URL " + myUrl);
                try {
                    Thread.sleep(DOWNLOAD_RETRY_DELAY_MS * attemptNumber);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.w(TAG, "Image download retry interrupted for URL " + myUrl, e);
                }
                downloadAndSaveImage(context, myUrl, imageDir, imageFileName, attemptNumber + 1);
                return;
            }

            alreadyProcessedImagesCounter.incrementAndGet();
            String finalImageFileName = myImageFileName;
            AsyncRunner.runOnMainThread(() -> {
                mProgressBar.setProgress(alreadyProcessedImagesCounter.get());
                Log.i(TAG, "image processed: " + finalImageFileName
                        + ". Saved/processed/requested: " + alreadySavedImagesCounter.get()
                        + "/" + alreadyProcessedImagesCounter.get()
                        + "/" + numberOfImagesRequestedToDownload);
                if (numberOfImagesRequestedToDownload == alreadyProcessedImagesCounter.get()) {
                    numberOfDownloadsListener.onRequestedNumberOfImagesToDownloadReached();
                }
            });
        });
    }

    /* ============== DOWNLOAD and SAVING IMAGE using new Images API (Retrofit) =========== */

    /**
     * Lazily initialized Retrofit instance for the new Images API.
     */
    private ImagesApiRESTInterface imagesApi;

    private ImagesApiRESTInterface getImagesApi() {
        if (imagesApi == null) {
            imagesApi = RetrofitClientProvider.getInstance()
                    .getRetrofit(ImagesApiRESTInterface.IMAGES_API_BASE_URL)
                    .create(ImagesApiRESTInterface.class);
        }
        return imagesApi;
    }

    /**
     * Downloads the main image of a CoffeeSite in the specified size from the new Images API
     * and saves it as a JPEG file into the app's private directory.<br>
     * Uses the endpoint: GET /bytes/object/?objectExtId={coffeeSiteId}&type=main&size={imageSize}
     * <p>
     * This method runs on a background thread and reports progress on the main thread.
     *
     * @param context         application context
     * @param coffeeSiteExtId external ID of the CoffeeSite (used as objectExtId in the Images API)
     * @param imageSize       requested image size: "original", "hd", "large", "mid", "small"
     * @param imageDir        directory name for saving image files
     * @param imageFileName   file name for the saved image
     */
    public void downloadAndSaveImageFromApi(final Context context,
                                            final String coffeeSiteExtId,
                                            final String imageSize,
                                            final String imageDir,
                                            final String imageFileName) {
        AsyncRunner.runInBackground(() -> {
            String myImageFileName;
            try {
                ContextWrapper cw = new ContextWrapper(context);
                final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
                final File myImageFile = new File(directory, imageFileName);
                myImageFileName = myImageFile.getAbsolutePath();

                // Call the new Images API synchronously (we are already on a background thread)
                Response<ResponseBody> response = getImagesApi()
                        .getImageOfTypeAsBytes(coffeeSiteExtId, "main", imageSize)
                        .execute();

                if (response.isSuccessful() && response.body() != null) {
                    long totalBytesWritten = 0;
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream fos = new FileOutputStream(myImageFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalBytesWritten += bytesRead;
                        }
                        fos.flush();
                    }
                    if (totalBytesWritten > 0) {
                        alreadySavedImagesCounter.incrementAndGet();
                    } else {
                        // Empty response body - delete the empty file and do not count as saved
                        myImageFile.delete();
                        Log.w(TAG, "Empty image received for coffeeSite " + coffeeSiteExtId + ", skipping.");
                        myImageFileName = "";
                    }
                } else {
                    Log.e(TAG, "Failed to download image for coffeeSite " + coffeeSiteExtId
                            + ". Response code: " + response.code());
                    myImageFileName = "";
                }
            } catch (Exception e) {
                Log.e(TAG, "Error downloading image for coffeeSite " + coffeeSiteExtId
                        + ": " + e.getMessage());
                myImageFileName = "";
            }

            alreadyProcessedImagesCounter.incrementAndGet();
            String finalImageFileName = myImageFileName;
            AsyncRunner.runOnMainThread(() -> {
                mProgressBar.setProgress(alreadyProcessedImagesCounter.get());
                Log.i(TAG, "image processed >>> " + finalImageFileName
                        + ". Saved/processed/requested: " + alreadySavedImagesCounter
                        + "/" + alreadyProcessedImagesCounter
                        + "/" + numberOfImagesRequestedToDownload);
                if (numberOfImagesRequestedToDownload == alreadyProcessedImagesCounter.get()) {
                    numberOfDownloadsListener.onRequestedNumberOfImagesToDownloadReached();
                }
            });
        });
    }
}
