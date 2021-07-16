package com.videocuttter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.Toast;

import com.deep.videotrimmer.DeepVideoTrimmer;
import com.deep.videotrimmer.interfaces.OnTrimVideoListener;
import com.deep.videotrimmer.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnTrimVideoListener {

    AppCompatButton btUpload;
    private DeepVideoTrimmer mVideoTrimmer;
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btUpload = findViewById(R.id.btUpload);
        mVideoTrimmer = findViewById(R.id.timeLine);
        btUpload.setOnClickListener(view -> {
            if (checkAndRequestPermissions(this)) {
                showVideoChooserDialog();
            }

        });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
//                            if (!result.getData().getStringExtra("REQUEST_VIDEO_TRIMMER").equals("")) {
                            Uri selectedUri = data.getData();
                            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, selectedUri);
                            long duration = mediaPlayer.getDuration();
                            mediaPlayer.release();
                            if (TimeUnit.MILLISECONDS.toSeconds(duration) > TimeUnit.MILLISECONDS.toSeconds(60000)) {
                                   /* registrationStep4View.video_view.setVisibility(View.INVISIBLE);
                                    alertDialog.showdialog("Upload a video less than a minute!");
                                    alertDialog.show();*/
                                Toast.makeText(MainActivity.this, "Upload a short video", Toast.LENGTH_SHORT).show();
                            } else {
//                                    registration4Controller.initializePlayer(videoUri);
                                mVideoTrimmer.setVisibility(View.VISIBLE);
                                if (mVideoTrimmer != null && FileUtils.getPath(MainActivity.this, selectedUri) != null) {
                                    mVideoTrimmer.setMaxDuration(100);
                                    mVideoTrimmer.setOnTrimVideoListener(MainActivity.this);
                                    mVideoTrimmer.setVideoURI(Uri.parse(FileUtils.getPath(MainActivity.this, selectedUri)));
                                }

                            }

//                            }

                        }
                    }
                });
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    public boolean checkAndRequestPermissions(final Activity context) {
        int ExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (ExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public void requestPermissions(final Activity context) {
        int ExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (ExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
//            return false;
        }
//        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                finish();
//                requestPermissions(this);
//                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_ID_MULTIPLE_PERMISSIONS);
            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                finish();
//                requestPermissions(this);
//                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_ID_MULTIPLE_PERMISSIONS);
            } else {
                showVideoChooserDialog();
            }
        }
    }



    public void showVideoChooserDialog() {

        final CharSequence[] options = {"From Camera", "From Gallery",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("From Camera")) {
                captureVideo();
            } else if (options[item].equals("From Gallery")) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                intent.putExtra("REQUEST_VIDEO_TRIMMER", REQUEST_VIDEO_TRIMMER);
                someActivityResultLauncher.launch(intent);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private final int REQUEST_VIDEO_TRIMMER = 0x12;

    private void captureVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 59);
        takeVideoIntent.putExtra("REQUEST_VIDEO_TRIMMER", REQUEST_VIDEO_TRIMMER);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            someActivityResultLauncher.launch(takeVideoIntent);
//            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_TRIMMER);
        }
    }


    @Override
    public void getResult(Uri uri) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                tvCroppingMessage.setVisibility(View.GONE);
            }
        });
        uri.toString();
    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
    }
}