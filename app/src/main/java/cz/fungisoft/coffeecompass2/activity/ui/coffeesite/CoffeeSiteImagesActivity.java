package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImageManageListener;
import cz.fungisoft.coffeecompass2.asynctask.image.GetImageObjectAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.image.ImageDeleteNewApiAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.image.ImageUploadNewApiAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.ImageFile;
import cz.fungisoft.coffeecompass2.entity.ImageObject;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.FileCompressor;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Activity for managing (viewing, uploading, deleting) multiple images
 * of a CoffeeSite. Displays existing images in a 2-column grid, allows
 * adding new images via camera or gallery, and deleting individual images.
 * <p>
 * Requires an existing CoffeeSite to be passed via Intent extra "coffeeSite".
 * Uses the new Images API for all operations.
 */
public class CoffeeSiteImagesActivity extends AppCompatActivity
        implements CoffeeSiteImageManageListener,
                   UserAccountServiceConnectionListener,
                   CoffeeSiteImageManageAdapter.OnImageDeleteClickListener {

    private static final String TAG = "CoffeeSiteImagesAct";
    public static final String EXTRA_LOCAL_IMAGE_PATHS = "localImagePaths";

    private static final int REQUEST_TAKE_PHOTO = 200;
    private static final int REQUEST_GALLERY_PHOTO = 201;
    private static final int MAX_IMAGES = 10;
    private static final String IMAGE_TYPE_OTHER = "other";

    private CoffeeSite coffeeSite;
    private final ArrayList<String> localImagePaths = new ArrayList<>();
    private boolean isDraftMode = false;

    private RecyclerView recyclerView;
    private CoffeeSiteImageManageAdapter adapter;
    private ProgressBar progressBar;
    private View progressOverlay;
    private TextView progressTextView;
    private TextView countLabel;
    private com.google.android.material.button.MaterialButton addButton;

    private static File imagePhotoFile;
    private static FileCompressor fileCompressor;
    private CompressImageForUploadAsyncTask compressImageForUploadAsyncTask;

    // UserAccountService binding
    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;
    private boolean mShouldUnbindUserAccountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_images);

        readIntentData();
        isDraftMode = coffeeSite == null || coffeeSite.getId().isEmpty() || !coffeeSite.isSavedOnServer() && !coffeeSite.isStatusZaznamuAvailable();
        if (coffeeSite == null && localImagePaths.isEmpty()) {
            Log.e(TAG, "No CoffeeSite provided. Finishing.");
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.manage_images_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.manage_images_title);
            toolbar.setSubtitle(coffeeSite != null ? coffeeSite.getName() : "");
        }

        progressBar = findViewById(R.id.manage_images_progress_bar);
        progressOverlay = findViewById(R.id.manage_images_progress_overlay);
        progressTextView = findViewById(R.id.manage_images_progress_text);
        countLabel = findViewById(R.id.manage_images_count_label);
        addButton = findViewById(R.id.manage_images_add_button);

        // Setup RecyclerView with 2-column grid
        recyclerView = findViewById(R.id.manage_images_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CoffeeSiteImageManageAdapter(this);
        recyclerView.setAdapter(adapter);

        // Add button click
        addButton.setOnClickListener(v -> {
            if (adapter.getImageCount() >= MAX_IMAGES) {
                Toast.makeText(this, R.string.manage_images_max_reached, Toast.LENGTH_SHORT).show();
                return;
            }
            selectImage();
        });

        fileCompressor = new FileCompressor(this);

        initializeImagesSource();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadImagesForCurrentMode();
    }

    @Override
    protected void onDestroy() {
        if (compressImageForUploadAsyncTask != null) {
            compressImageForUploadAsyncTask.cancel(true);
        }
        super.onDestroy();
        if (!isDraftMode) {
            doUnbindUserAccountService();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -------- Image loading --------

    private void loadImages() {
        if (isDraftMode || coffeeSite == null || !Utils.isOnline(getApplicationContext())) {
            return;
        }
        showProgress(R.string.manage_images_loading_progress);
        new GetImageObjectAsyncTask(this, coffeeSite.getId()).execute();
    }

    @Override
    public void onImageObjectLoaded(ImageObject imageObject) {
        hideProgress();
        if (imageObject != null) {
            List<ImageFile> images = imageObject.getObjectImages();
            adapter.setImageFiles(images);
            updateCountLabel();
            updateAddButtonState();
        }
    }

    @Override
    public void onImageObjectLoadFailed(Result.Error error) {
        hideProgress();
        Log.e(TAG, "Load images failed: " + (error != null ? error.getDetail() : ""));
        Toast.makeText(this, R.string.manage_images_load_failure, Toast.LENGTH_SHORT).show();
    }

    // -------- Image upload --------

    private void uploadImage(File imageFile) {
        if (isDraftMode) {
            hideProgress();
            addDraftImage(imageFile);
            return;
        }
        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            hideProgress();
            Toast.makeText(this, R.string.toast_new_login_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress(R.string.manage_images_upload_progress);
        new ImageUploadNewApiAsyncTask(this, userAccountService, imageFile,
                coffeeSite.getId(), IMAGE_TYPE_OTHER).execute();
    }

    @Override
    public void onImageUploaded(String imageExtId) {
        hideProgress();
        Toast.makeText(this, R.string.manage_images_upload_success, Toast.LENGTH_SHORT).show();
        // Reload the grid to show the new image
        loadImages();
    }

    @Override
    public void onImageUploadFailed(Result.Error error) {
        hideProgress();
        Log.e(TAG, "Upload failed: " + (error != null ? error.getDetail() : ""));
        Toast.makeText(this, R.string.manage_images_upload_failure, Toast.LENGTH_SHORT).show();
    }

    // -------- Image delete --------

    @Override
    public void onImageDeleteClick(ImageFile imageFile, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_images_delete_confirm_title)
                .setMessage(R.string.manage_images_delete_confirm_message)
                .setPositiveButton(R.string.delete_image_dialog_confirm, (dialog, which) -> {
                    deleteImage(imageFile, position);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteImage(ImageFile imageFile, int position) {
        if (isDraftMode) {
            deleteDraftImage(position);
            return;
        }
        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            Toast.makeText(this, R.string.toast_new_login_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress(R.string.manage_images_delete_progress);
        // Store position for removal after success
        pendingDeletePosition = position;
        new ImageDeleteNewApiAsyncTask(this, userAccountService,
                coffeeSite.getId(), imageFile.getExternalId()).execute();
    }

    private int pendingDeletePosition = -1;

    @Override
    public void onImageDeleted(String imageExtId) {
        hideProgress();
        Toast.makeText(this, R.string.manage_images_delete_success, Toast.LENGTH_SHORT).show();
        if (pendingDeletePosition >= 0) {
            adapter.removeAt(pendingDeletePosition);
            pendingDeletePosition = -1;
        }
        updateCountLabel();
        updateAddButtonState();
    }

    @Override
    public void onImageDeleteFailed(Result.Error error) {
        hideProgress();
        pendingDeletePosition = -1;
        Log.e(TAG, "Delete failed: " + (error != null ? error.getDetail() : ""));
        Toast.makeText(this, R.string.manage_images_delete_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (isDraftMode) {
            setResult(RESULT_OK, buildDraftResultIntent());
        }
        super.onBackPressed();
    }

    // -------- Image picker (camera / gallery) --------

    private void selectImage() {
        final CharSequence[] items = {"Foto", "Vybrat z galerie", getString(R.string.cancel)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_images_add_photo)
                .setItems(items, (dialog, which) -> {
                    if (items[which].equals("Foto")) {
                        requestStoragePermission(true);
                    } else if (items[which].equals("Vybrat z galerie")) {
                        requestStoragePermission(false);
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void requestStoragePermission(final boolean isCamera) {
        String[] permissions;
        if (isCamera) {
            permissions = new String[] { Manifest.permission.CAMERA };
        } else {
            permissions = new String[] { getReadImagesPermission() };
        }

        Dexter.withContext(this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (isCamera) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryIntent();
                            }
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private String getReadImagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        }
        return Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Create image file failed.", ex);
        }
        if (photoFile != null) {
            imagePhotoFile = photoFile;
            Uri photoURI = FileProvider.getUriForFile(this,
                    getString(R.string.file_provider), photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.setClipData(ClipData.newRawUri("", photoURI));
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } catch (ActivityNotFoundException | SecurityException ex) {
                Log.e(TAG, "No camera app available for image capture.", ex);
                Toast.makeText(this, R.string.no_camera_available, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                startCompressAndUpload(null);
                break;

            case REQUEST_GALLERY_PHOTO:
                if (data != null && data.getData() != null) {
                    startCompressAndUpload(data.getData());
                }
                break;

            default:
                break;
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permissions_camera_storage_title)
                .setMessage(R.string.permissions_camera_storage_request)
                .setPositiveButton(R.string.permissions_camera_storage_settings, (dialog, which) -> {
                    dialog.cancel();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    // -------- UserAccountService binding --------

    private void doBindUserAccountService() {
        userAccountServiceConnector = new UserAccountServiceConnector();
        userAccountServiceConnector.addUserAccountServiceConnectionListener(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserAccountService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserAccountService() {
        if (mShouldUnbindUserAccountService) {
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserAccountService = false;
        }
    }

    @Override
    public void onUserAccountServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        Log.i(TAG, "UserAccountService connected.");
    }

    // -------- UI helpers --------

    private void startCompressAndUpload(@Nullable Uri selectedImageUri) {
        showProgress(R.string.manage_images_upload_progress);
        if (compressImageForUploadAsyncTask != null) {
            compressImageForUploadAsyncTask.cancel(true);
        }
        compressImageForUploadAsyncTask = new CompressImageForUploadAsyncTask(
                this,
                getApplicationContext(),
                fileCompressor,
                selectedImageUri,
                imagePhotoFile);
        compressImageForUploadAsyncTask.execute();
    }

    private void showProgress(@StringRes int progressMessageRes) {
        progressBar.setVisibility(View.VISIBLE);
        progressOverlay.setVisibility(View.VISIBLE);
        progressTextView.setText(progressMessageRes);
        addButton.setEnabled(false);
        recyclerView.setEnabled(false);
        recyclerView.setAlpha(0.6f);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        progressOverlay.setVisibility(View.GONE);
        progressTextView.setText(null);
        recyclerView.setEnabled(true);
        recyclerView.setAlpha(1.0f);
        updateAddButtonState();
    }

    private static final class CompressImageForUploadAsyncTask extends AsyncTask<Void, Void, File> {

        private final WeakReference<CoffeeSiteImagesActivity> activityReference;
        private final Context appContext;
        private final FileCompressor fileCompressor;
        @Nullable
        private final Uri selectedImageUri;
        @Nullable
        private final File sourceImageFile;
        @Nullable
        private Exception compressionException;

        private CompressImageForUploadAsyncTask(@NonNull CoffeeSiteImagesActivity activity,
                                                @NonNull Context appContext,
                                                @NonNull FileCompressor fileCompressor,
                                                @Nullable Uri selectedImageUri,
                                                @Nullable File sourceImageFile) {
            this.activityReference = new WeakReference<>(activity);
            this.appContext = appContext;
            this.fileCompressor = fileCompressor;
            this.selectedImageUri = selectedImageUri;
            this.sourceImageFile = sourceImageFile;
        }

        @Override
        protected File doInBackground(Void... voids) {
            try {
                if (selectedImageUri != null) {
                    String realPath = Utils.getRealPathFromUri(selectedImageUri, appContext);
                    if (realPath == null || realPath.isEmpty()) {
                        return null;
                    }
                    return fileCompressor.compressToFile(new File(realPath));
                }

                if (sourceImageFile == null || !sourceImageFile.exists()) {
                    return null;
                }

                return fileCompressor.compressToFile(sourceImageFile);
            } catch (Exception e) {
                compressionException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(File compressedImageFile) {
            CoffeeSiteImagesActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            activity.compressImageForUploadAsyncTask = null;

            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            if (compressedImageFile != null && compressedImageFile.exists()) {
                activity.imagePhotoFile = compressedImageFile;
                activity.uploadImage(compressedImageFile);
                return;
            }

            activity.hideProgress();
            if (compressionException != null) {
                Log.e(TAG, "Failed to prepare image for upload.", compressionException);
            }
            Toast.makeText(activity,
                    R.string.manage_images_upload_failure, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            CoffeeSiteImagesActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            activity.compressImageForUploadAsyncTask = null;
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.hideProgress();
            }
        }
    }

    private void updateCountLabel() {
        int count = adapter.getImageCount();
        countLabel.setText(getString(R.string.manage_images_count, count, MAX_IMAGES));
    }

    private void updateAddButtonState() {
        addButton.setEnabled(adapter.getImageCount() < MAX_IMAGES);
    }

    private void updateDraftImages() {
        adapter.setImageFiles(createDraftImageFiles());
        updateCountLabel();
        updateAddButtonState();
    }

    private void readIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        coffeeSite = extras.getParcelable("coffeeSite");
        ArrayList<String> paths = extras.getStringArrayList(EXTRA_LOCAL_IMAGE_PATHS);
        if (paths != null) {
            localImagePaths.addAll(paths);
        }
    }

    private void initializeImagesSource() {
        if (isDraftMode) {
            updateDraftImages();
        } else {
            doBindUserAccountService();
        }
    }

    private void loadImagesForCurrentMode() {
        if (isDraftMode) {
            updateDraftImages();
        } else {
            loadImages();
        }
    }

    private void addDraftImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return;
        }
        localImagePaths.add(imageFile.getPath());
        updateDraftImages();
        Toast.makeText(this, R.string.manage_images_upload_success, Toast.LENGTH_SHORT).show();
    }

    private void deleteDraftImage(int position) {
        if (position < 0 || position >= localImagePaths.size()) {
            return;
        }
        File localImageFile = new File(localImagePaths.remove(position));
        if (localImageFile.exists()) {
            localImageFile.delete();
        }
        updateDraftImages();
        Toast.makeText(this, R.string.manage_images_delete_success, Toast.LENGTH_SHORT).show();
    }

    private Intent buildDraftResultIntent() {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra(EXTRA_LOCAL_IMAGE_PATHS, new ArrayList<>(localImagePaths));
        return resultIntent;
    }

    private List<ImageFile> createDraftImageFiles() {
        List<ImageFile> localImages = new ArrayList<>();
        for (String path : localImagePaths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            ImageFile imageFile = new ImageFile();
            imageFile.setBaseBytesImageUrl(Uri.fromFile(new File(path)).toString());
            imageFile.setImageType(localImages.isEmpty() ? "main" : "other");
            localImages.add(imageFile);
        }
        return localImages;
    }
}
