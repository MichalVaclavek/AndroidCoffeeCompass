package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.text.SimpleDateFormat;
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

    private static final int REQUEST_TAKE_PHOTO = 200;
    private static final int REQUEST_GALLERY_PHOTO = 201;
    private static final int MAX_IMAGES = 10;

    private CoffeeSite coffeeSite;

    private RecyclerView recyclerView;
    private CoffeeSiteImageManageAdapter adapter;
    private ProgressBar progressBar;
    private TextView countLabel;
    private com.google.android.material.button.MaterialButton addButton;

    private File imagePhotoFile;
    private FileCompressor fileCompressor;

    // UserAccountService binding
    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;
    private boolean mShouldUnbindUserAccountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_images);

        // Read CoffeeSite from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            coffeeSite = extras.getParcelable("coffeeSite");
        }
        if (coffeeSite == null) {
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
            toolbar.setSubtitle(coffeeSite.getName());
        }

        progressBar = findViewById(R.id.manage_images_progress_bar);
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

        // Bind UserAccountService for auth tokens
        doBindUserAccountService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load images when activity becomes visible
        loadImages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindUserAccountService();
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
        if (coffeeSite == null || !Utils.isOnline(getApplicationContext())) {
            return;
        }
        showProgress();
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
        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            Toast.makeText(this, R.string.toast_new_login_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress();
        new ImageUploadNewApiAsyncTask(this, userAccountService, imageFile, coffeeSite.getId()).execute();
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
        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            Toast.makeText(this, R.string.toast_new_login_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress();
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
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
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
                try {
                    imagePhotoFile = fileCompressor.compressToFile(imagePhotoFile);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to compress photo from camera.", e);
                }
                if (imagePhotoFile != null && imagePhotoFile.exists()) {
                    uploadImage(imagePhotoFile);
                }
                break;

            case REQUEST_GALLERY_PHOTO:
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    try {
                        imagePhotoFile = fileCompressor.compressToFile(
                                new File(Utils.getRealPathFromUri(selectedImage, this)));
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to compress image from gallery.", e);
                    }
                    if (imagePhotoFile != null && imagePhotoFile.exists()) {
                        uploadImage(imagePhotoFile);
                    }
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

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void updateCountLabel() {
        int count = adapter.getImageCount();
        countLabel.setText(getString(R.string.manage_images_count, count, MAX_IMAGES));
    }

    private void updateAddButtonState() {
        addButton.setEnabled(adapter.getImageCount() < MAX_IMAGES);
    }
}
