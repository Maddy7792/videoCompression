package com.dbst.assignment.activites;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dbst.assignment.R;
import com.dbst.assignment.Utils.CompressVideoTask;
import com.dbst.assignment.Utils.Constants;
import com.dbst.assignment.Utils.FileUtils;
import com.dbst.assignment.Utils.Utils;
import com.dbst.assignment.VideosActivity;
import com.dbst.assignment.adapters.GalleryAdapter;
import com.dbst.assignment.models.Images;
import com.dbst.assignment.models.Videos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import id.zelory.compressor.Compressor;


public class GalleryActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    ArrayList<Images> imagesList;
    private GalleryAdapter galleryAdapter;
    RecyclerView rv_Gallery;
    private ProgressDialog progressDialog;
    private Button btnSelectImage;
    private Button btnSelectVideo;
    private Button btnUpload;
    private String videoFormat = "video/*";
    private static final int RESULT_LOAD_VIDEO = 200;
    private String imageFormat = "image/*";
    private static final int RESULT_LOAD_IMAGE = 201;
    private Uri filePath = Uri.EMPTY;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference imageFolder;
    private UploadTask uploadTask;
    private CompressVideoTask compressVideoTask;
    private ProgressDialog compressDialog;
    private double progress;
    private ProgressBar progressBar;
    private static final int RC_HANDLE_READ_EXTERNAL_STORAGE_PERM = 3;
    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM = 4;
    private boolean isGrant = false;
    private DatabaseReference videoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestReadPermission();
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        databaseReference = database.getReference("Images");
        videoRef = database.getReference("videos");
        btnSelectImage = findViewById(R.id.btn_image);
        btnSelectVideo = findViewById(R.id.btn_video);
        btnUpload = findViewById(R.id.btn_upload);
        rv_Gallery = findViewById(R.id.rv_gallery);
        progressBar = (ProgressBar) findViewById(R.id.pgb_sub);
        compressVideoTask = new CompressVideoTask(this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        rv_Gallery.setHasFixedSize(true);
        rv_Gallery.setLayoutManager(layoutManager);
        imagesList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        compressDialog = new ProgressDialog(this);
        compressDialog.setMessage("Compressing Video...Please Wait");
        compressDialog.setCancelable(false);

        btnSelectImage.setOnClickListener(this);
        btnSelectVideo.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        Log.d("IsGrant-1", "" + isGrant);
        if (Utils.getKey(this)) {
            getImages();
        }

    }

    private void getImages() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    imagesList.add(ds.getValue(Images.class));
                }
                setGalleryImages(imagesList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void setGalleryImages(ArrayList<Images> imagesList) {

        galleryAdapter = new GalleryAdapter(this);
        galleryAdapter.setData(imagesList);
        rv_Gallery.setAdapter(galleryAdapter);
        galleryAdapter.notifyDataSetChanged();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_image:
                uploadMediaContent("GALLERY");
                break;
            case R.id.btn_upload:
               /* Intent intent = new Intent(GalleryActivity.this, VideosActivity.class);
                startActivity(intent);*/
                break;
            case R.id.btn_video:
                uploadMediaContent("VIDEO");
                break;
        }
    }

    private void uploadMediaContent(String type) {
        if (type.equals("GALLERY")) {
            pickImageFromGallery();
        } else if (type.equals("VIDEO")) {
            pickVideoFromGallery();
        }
    }

    private void pickVideoFromGallery() {
        Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        videoIntent.setType(videoFormat);
        startActivityForResult(videoIntent, RESULT_LOAD_VIDEO);
    }

    private void pickImageFromGallery() {
        Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imageIntent.setType(imageFormat);
        startActivityForResult(Intent.createChooser(imageIntent, "Select Picture"), RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri filePath = data.getData();
                getFilePath(filePath, Constants.IMAGE);
                Log.d("File-Path-Images", "" + filePath);
            }

        } else if (requestCode == RESULT_LOAD_VIDEO) {
            if (resultCode == RESULT_OK && data != null) {
                filePath = data.getData();
                getFilePath(filePath, Constants.VIDEO);
                compressDialog.show();
            }
        }
    }

    private void getFilePath(Uri filePath, String type) {
        try {
            if (type.equals(Constants.IMAGE)) {
                // File compressedImageFile = new Compressor(this).compressToFile(new File(filePath.getPath()));
                // Log.d("Compress-Image",""+compressedImageFile);
                uploadImageToFirebase(filePath);
            } else if (type.equals(Constants.VIDEO)) {
                compressVideoTask.compressVideo(FileUtils.getPath(this, filePath),
                        new CompressVideoTask.VideoCompressResponse() {
                            @Override
                            public void onCompressResponse(String response, String compressedFilePath) {
                                if (response.equals(Constants.SUCCESS)) {
                                    compressDialog.dismiss();
                                    uploadVideoToFirebase(compressedFilePath);
                                } else if (response.equals(Constants.FINISH)) {
                                    compressDialog.dismiss();
                                }
                                Log.d("Compress-Video-Path", "" + compressedFilePath);

                            }
                        });
                compressVideoTask.execute();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void uploadImageToFirebase(Uri filePath) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        progressBar.setVisibility(View.VISIBLE);
        final String imageName = UUID.randomUUID().toString();
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        StorageReference imageFolder = storageReference.child("images/" + imageName);
        UploadTask uploadTask = imageFolder.putFile(filePath, metadata);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("prog", "Success");
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("prog", "3...coming");

            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Paused", "PAUSED");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressBar.setProgress((int) progress);

            }
        });
    }


    private void uploadVideoToFirebase(String filePath) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("File-Path", "" + filePath);
        final String imageName = UUID.randomUUID().toString();
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/mp4")
                .build();
       final StorageReference imageFolder = storageReference.child("Videos/" + imageName +".mp4");
      UploadTask  uploadTask = imageFolder.putFile(Uri.fromFile(new File(filePath)), metadata);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Videos videos = new Videos();
                        videos.setSteamUrl(uri.toString());
                        videoRef.child(String.valueOf(System.currentTimeMillis())).setValue(videos);
                    }
                });
                Log.d("prog", "Success");
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("prog", "3...coming");

            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Paused", "PAUSED");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressBar.setProgress((int) progress);

            }
        });
    }


    private void requestReadPermission() {

        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RC_HANDLE_READ_EXTERNAL_STORAGE_PERM);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RC_HANDLE_READ_EXTERNAL_STORAGE_PERM);
            }

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_READ_EXTERNAL_STORAGE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImages();
                    isGrant = true;
                    Log.d("IsGrant-2", "" + isGrant);
                    Utils.storeKey(this, isGrant);
                    //Toast.makeText(this, "Accept", Toast.LENGTH_SHORT).show();

                } else {
                    //Toast.makeText(this, "Reject", Toast.LENGTH_SHORT).show();
                    isGrant = false;
                    requestReadPermission();
                }
                break;
            case RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }


    private void requestWritePermission() {

        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM);
            }

        } else {

        }


    }
}






