package cz.fungisoft.coffeecompass2.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

    /* https://www.codexpedia.com/android/android-download-and-save-image-through-picasso/ */

    public static final String COFFEESITE_IMAGE_DIR = "coffeesitephotos";

    public static Target picassoImageTarget(Context context, final String imageDir, final String imageName) {
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        Log.d("picassoImageTarget: ", directory.getAbsolutePath() + imageName);
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d("onBitmapLoaded() ", imageDir + "/" + imageName);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Log.d("ImageTarget run() ", imageDir + "/" + imageName);
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                        Log.i(TAG, "image saved to >>>" + myImageFile.getAbsolutePath());

                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }

    public static File getImageFile(Context appContext, String imageDir, String imageFileName) {
        ContextWrapper cw = new ContextWrapper(appContext);
        File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
        return new File(directory, imageFileName);
    }


    /**
     * Loading image bytes by Picaso and saving it into file system app's work directory
     * as image JPG file.
     * Taken from https://stackoverflow.com/questions/53479820/save-image-in-external-storage-by-using-picasso-from-url-how-to-download-image
     *
     * @param context
     * @param myUrl
     * @param imageDir
     * @param imageFileName
     */
    public static void saveImage(final Context context, final String myUrl, final String imageDir, final String imageFileName) {

        //final ProgressDialog progress = new ProgressDialog(context);

        class SaveThisImage extends AsyncTask<Void, Void, Void> {

            private String myImageFileName;

            @Override
            protected void onPreExecute() {
//                super.onPreExecute();
//                progress.setTitle("Processing");
//                progress.setMessage("Please Wait...");
//                progress.setCancelable(false);
//                progress.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {

                try {

                    File sdCard = Environment.getExternalStorageDirectory();
                    //@SuppressLint("DefaultLocale")
                    //String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    ContextWrapper cw = new ContextWrapper(context);
                    final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
                    final File myImageFile = new File(directory, imageFileName);
                    //File dir = new File(sdCard.getAbsolutePath() + "/" + imageFileName);
                    //dir.mkdirs();
                    //final File myImageFile = new File(dir, imageFileName); // Create image file
                    myImageFileName = myImageFile.getAbsolutePath();
                    FileOutputStream fos = null;
                    try {
                        Bitmap bitmap = Picasso.get().load(myUrl).get();

                        fos = new FileOutputStream(myImageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

//                       Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                       intent.setData(Uri.fromFile(myImageFile));
//                       context.sendBroadcast(intent);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
//                super.onPostExecute(result);
//                if(progress.isShowing()){
//                    progress.dismiss();
//                }
//                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "image saved to >>> " + myImageFileName);
            }
        }
        SaveThisImage shareimg = new SaveThisImage();
        shareimg.execute();
    }

}
