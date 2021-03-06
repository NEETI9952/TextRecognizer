package com.example.invoicetextdetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "Main Activity";
    public static String API_KEY = "af30c484b8c6520a2c60a0ba41702fa7";
    public static String GSTIN = "";
    TextInputLayout gstinTextLayout;
    LinearLayout linearButtonLayout;
    JSONObject responseFromApi;
//     07AANFG0307M1ZC


    private Bitmap myBitmap;
    private ImageView myImageView;
    private TextView myTextViewInfo;
    private TextInputEditText myEditTextGst;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    int rotation=0;
    String GstPattern= "GST\\s*.*\\s*([0-9A-Z]{15})";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (Build.VERSION.SDK_INT >= 24) {
//            try {
//                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
//                m.invoke(null);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

        myTextViewInfo = findViewById(R.id.textViewBillInfo);
        myEditTextGst = findViewById(R.id.textViewGST);
        myImageView = findViewById(R.id.imageView);
        progressBar= findViewById(R.id.progressBar);
        gstinTextLayout=findViewById(R.id.textInputLayoutGST);
        linearButtonLayout=findViewById(R.id.linearButtonLayout);
        findViewById(R.id.checkText).setOnClickListener(this);
        findViewById(R.id.select_image).setOnClickListener(this);

        myEditTextGst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                gstinTextLayout.setEndIconDrawable(null);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecognition();
                }else {
                    Toast.makeText(this, "Please insert a bill", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.select_image:
                myTextViewInfo.setText("");
                myEditTextGst.setText("");
                GSTIN = "";
                myImageView.setImageResource(R.drawable.bill);
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
                        try {
                            cameraPermission();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
                    try {
                        dispatchTakePictureIntent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    public void cameraPermission() throws IOException {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
            }
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent () throws IOException {
//        ContentValues values= new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE,"New Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION,"from camera");
//        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

//        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DCIM), "Camera");
//        File file = File.createTempFile(
//                timeStamp,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );

        // Save a file: path for use with ACTION_VIEW intents

        File file = new File(Environment.getExternalStorageDirectory(),  timeStamp + ".png");
//        imageUri = Uri.fromFile(file);
//        imageUri = FileProvider.getUriForFile(MainActivity.this,"com.example.invoicetextdetector.provider",file); //(use your app signature + ".provider" )
//        imageUri  = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",file);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(MainActivity.this, "Please Wait!", Toast.LENGTH_SHORT).show();

        if (requestCode == CAMERA_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                try {
//                    Log.i("", "onActivityResult: ");
//                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                    myImageView.setImageBitmap(myBitmap);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
                myBitmap=(Bitmap) extras.get("data");
                myImageView.setImageBitmap(myBitmap);
                myImageView.setTag("photo added");
//                showProgressBar();
//                handleUpload(bitmap);
            }
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

        if(linearButtonLayout.getVisibility()==View.VISIBLE) {
            linearButtonLayout.setVisibility(View.GONE);
        }
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

                myTextViewInfo.append(lineText+"\n");

                if(lineText.contains("GST")){
                    Pattern r = Pattern.compile(GstPattern);
                    Matcher m = r.matcher(lineText);

                    if (m.find( )) {
                        myEditTextGst.setText(m.group(1));

                    }
                }



//                for (Text.Element element : line.getElements()) {
//                    String elementText = element.getText();
//
////                    Pattern p = Pattern.compile(GstPattern);
////                    Matcher m = p.matcher(elementText);
////
////                    if (m.find( )) {
////                        myTextViewInfo.setText("GSTIN"+m.group(1));
////
////                    }
////                    myTextViewInfo.append(elementText+"\n");
////                    Log.i("ExtractedText-line:", lineText);
////                    Log.i("ExtractedText-block:", blockText);
////                    Log.i("ExtractedText-element:", elementText);
//                }
            }

            if(myEditTextGst.getText().toString().trim().isEmpty()){
                Toast.makeText(MainActivity.this,"Couldn't find GSTIN",Toast.LENGTH_LONG).show();
                //alertbox
            }
        }
    }

    public void validateGST(View v){
        showProgressBar();
        String gst="([0-9A-Z]{15})";
        GSTIN= myEditTextGst.getText().toString().trim();
        Pattern p = Pattern.compile(gst);

        Matcher matcher = p.matcher(GSTIN);

        if (matcher.find()) {

            // Instantiate the RequestQueue.
//            queue = Volley.newRequestQueue(this);
            String url ="https://sheet.gstincheck.ml/check/"+ API_KEY +"/"+ GSTIN +"";

            JsonRequest objectRequest=new JsonObjectRequest(Request.Method.GET, url, null,new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    responseFromApi=response;
//                        TempDialog.dismiss();
                    hideProgressBar();
                    try {
                        Boolean responseflag=response.getBoolean("flag");
                        Log.i("statusresponse","status:" +responseflag);

                        if(responseflag){

                            Toast.makeText(MainActivity.this, "GST Valid",Toast.LENGTH_SHORT).show();
                            gstinTextLayout.setEndIconDrawable(R.drawable.ic_baseline_check_circle_24);
                            linearButtonLayout.setVisibility(View.VISIBLE);
                            
//                            fetchDataFromApi(response);

                        }else{
                            gstinTextLayout.setEndIconDrawable(R.drawable.ic_baseline_close_24);
//                            gstinTextLayout.setError("GST Invalid");
                            Toast.makeText(MainActivity.this, "GST Invalid",Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressBar();
                    Log.e("statuscode",error.toString());
                }
            });

            MySingleton.getInstance(this).addToRequestQueue(objectRequest);
//            queue.add(objectRequest);

        } else {
            hideProgressBar();
            gstinTextLayout.setError("GST must contain 15 characters");
//            Toast.makeText(this,"GST must contain 15 characters",Toast.LENGTH_SHORT).show();
        }
    }

    public void fetchDetailButton(View v) throws JSONException {
        if(responseFromApi!=null){
            fetchDataFromApi(responseFromApi);
        }else {
            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchDataFromApi(JSONObject response) throws JSONException {

        Toast.makeText(MainActivity.this, "GST Valid",Toast.LENGTH_SHORT).show();

        JSONObject data_from_api  = response.getJSONObject("data");
        Log.i(TAG, "onResponse data:"+data_from_api.toString());
        DataClass data= new DataClass();

        data.setTradeNam(data_from_api.getString("tradeNam"));
        data.setSts(data_from_api.getString("sts"));
        data.setLstupdt(data_from_api.getString("lstupdt"));
        data.setRgdt(data_from_api.getString("rgdt"));
        data.setDty(data_from_api.getString("dty"));

        JSONObject pradr_from_api= data_from_api.getJSONObject("pradr");


        JSONObject address_from_api= pradr_from_api.getJSONObject("addr");
        addressClass address = new addressClass();

        address.setBno(address_from_api.getString("bno"));
        address.setFlno(address_from_api.getString("flno"));
        address.setSt(address_from_api.getString("st")) ;
        address.setLoc(address_from_api.getString("loc"));
        address.setCity(address_from_api.getString("city"));
        address.setDst(address_from_api.getString("dst"));
        address.setStcd(address_from_api.getString("stcd"));
        address.setLoc(address_from_api.getString("pncd"));


        goToTaxPayerInfoActivity(data,address);
    }

    private void goToTaxPayerInfoActivity(DataClass data, addressClass address) {

        Intent intent = new Intent(MainActivity.this, TaxPayerInfoActivity.class);

        intent.putExtra("GSTIN", GSTIN);
        intent.putExtra("tradeName", data.getTradeNam());
        intent.putExtra("status", data.getSts());
        intent.putExtra("lstupdate", data.getLstupdt());
        intent.putExtra("address", address.getBno()+" "+address.getFlno()+" "+address.getSt()+" "+address.getLoc()+" "+ address.getCity()+" "+address.getDst()+" "+address.getStcd()+" "+address.getPncd());
        intent.putExtra("registrationType", data.getDty());
        intent.putExtra("registrationDate", data.getRgdt());
        startActivity(intent);
    }

    public  void CreateExcelSheet(View v){
        ////
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    private void hideProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}

