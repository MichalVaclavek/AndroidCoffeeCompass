package cz.fungisoft.coffeecompass2.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Copy from https://androidwave.com/capture-image-from-camera-gallery/
 */
public class ImageUtil {

    private static final String TAG = "ImageUtil: ";

    private ImageUtil() {
    }

    private static ImageUtil instance;

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
            // write the compressed bitmap at the destination specified by destinationPath.
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

    private int alreadySavedImagesCounter = 0;

    private int numberOfImagesRequestedToDownload = 0;

    public void setNumberOfImagesRequestedToDownload(int numberOfImagesRequestedToDownload) {
        this.numberOfImagesRequestedToDownload = numberOfImagesRequestedToDownload;
    }


    private ProgressBar mProgressBar;

    public void resetAlreadySavedImagesCounter() {
        alreadySavedImagesCounter = 0;
        numberOfImagesRequestedToDownload = 0;
    }

    public int getAlreadySavedImagesCounter() {
        return alreadySavedImagesCounter;
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

        class SaveThisImage extends AsyncTask<Void, Void, Void> {

            private String myImageFileName;

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... arg0) {

                try {
                    //File sdCard = Environment.getExternalStorageDirectory();
                    //@SuppressLint("DefaultLocale")
                    //String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    ContextWrapper cw = new ContextWrapper(context);
                    final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
                    final File myImageFile = new File(directory, imageFileName);
                    //File dir = new File(sdCard.getAbsolutePath() + "/" + imageFileName);
                    //dir.mkdirs();
                    //final File myImageFile = new File(dir, imageFileName); // Create image file
                    myImageFileName = myImageFile.getAbsolutePath();
                    Bitmap bitmap = Picasso.get().load(myUrl).get();
                    try (FileOutputStream fos = new FileOutputStream(myImageFile)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                        alreadySavedImagesCounter++;
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                mProgressBar.setProgress(alreadySavedImagesCounter);
                Log.i(TAG, "image saved to >>> " + myImageFileName + ". Saved/requested files: " + alreadySavedImagesCounter + "/" + numberOfImagesRequestedToDownload);
                if (numberOfImagesRequestedToDownload == alreadySavedImagesCounter) {
                    numberOfDownloadsListener.onRequestedNumberOfImagesToDownloadReached();
                }
            }
        }

        SaveThisImage saveThisImage = new SaveThisImage();
        saveThisImage.execute();
    }

}
