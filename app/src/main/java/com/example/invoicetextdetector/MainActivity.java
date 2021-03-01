package com.example.invoicetextdetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Bitmap myBitmap;
    private ImageView myImageView;
    private TextView myTextViewInfo;
    private EditText myEditTextGst;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    int rotation=0;
    Uri imageUri;
    String GstPattern= "((?=.*[0-9A-Z]).{15})";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        myTextViewInfo = findViewById(R.id.textViewBillInfo);
        myEditTextGst = findViewById(R.id.textViewGST);
        myImageView = findViewById(R.id.imageView);
        findViewById(R.id.checkText).setOnClickListener(this);
        findViewById(R.id.select_image).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecognition();
                }else {
                    Toast.makeText(this, "Check text clicked", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.select_image:
                myTextViewInfo.setText("");
                selectPhoto();
                break;
        }
    }

    public void selectPhoto() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        builder.setIcon(R.drawable.addimage);
        builder.setMessage("Choose image source")
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cameraPermission();
                    }
                }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                galleryPermissions();
            }
        });
        builder.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERM_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {

                }
                break;
            case GALLERY_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchToGalleryIntent();
                } else {
                }
                break;

        }

    }

    private void galleryPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
            }

        } else {
            dispatchToGalleryIntent();
        }
    }

    private void dispatchToGalleryIntent() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, GALLERY_REQUEST_CODE);
    }

    public void cameraPermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
            }

        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent () {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(Environment.getExternalStorageDirectory(),  timeStamp + ".png");
        imageUri = Uri.fromFile(file);


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(MainActivity.this, "Please Wait!", Toast.LENGTH_SHORT).show();
        if(requestCode == CAMERA_REQUEST_CODE){
            try {
                myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                myImageView.setImageBitmap(myBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (resultCode == Activity.RESULT_OK) {
//                Bundle extras = data.getExtras();
//                myBitmap=(Bitmap) extras.get("data");
//                myImageView.setImageBitmap(myBitmap);
//                myImageView.setTag("photo added");
//                showProgressBar();
//                handleUpload(bitmap);
//            }
        }
        if (requestCode == GALLERY_REQUEST_CODE&&data!=null&&data.getData()!=null) {
            if (resultCode == Activity.RESULT_OK) {

                Uri uri=data.getData();
                try {
                    myBitmap=MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),uri);
                    myImageView.setImageBitmap(myBitmap);
//                    showProgressBar();

                } catch (IOException e) {
                    Toast.makeText(this, "Photo not uploaded", Toast.LENGTH_SHORT).show();
//                    hideProgressBar();
                    e.printStackTrace();
                }

            }
        }
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(myBitmap,rotation);

        TextRecognizer recognizer = TextRecognition.getClient();

        Task<Text> result =recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(MainActivity.this,
                        exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processExtractedText(Text firebaseVisionText) {
        myTextViewInfo.setText(null);
        if (firebaseVisionText.getText().length() == 0) {
            myTextViewInfo.setText("No text found. Please try again");
            return;
        }

//        for(FirebaseVisionText.Block block:firebaseVisionText.getBlocks()){
//            String blockText = block.getText();
//            myTextView.append(blockText);
//        }
        for (Text.TextBlock block : firebaseVisionText.getTextBlocks()) {
            String blockText = block.getText();

            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();

                if(lineText.contains("GSTIN")){
                    Pattern r = Pattern.compile(GstPattern);
                    Matcher m = r.matcher(lineText);

                    if (m.find( )) {
                        myEditTextGst.setText(m.group(0));
                        return;
                    }
                }

                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();


                    myTextViewInfo.append(elementText+"\n");
//                    Log.i("ExtractedText-line:", lineText);
//                    Log.i("ExtractedText-block:", blockText);
//                    Log.i("ExtractedText-element:", elementText);
                }
            }
        }
    }

    public boolean validateGST(String gst){
        return GstPattern.matches(gst);
    }

}