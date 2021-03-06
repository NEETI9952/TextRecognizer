package com.example.invoicetextdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.invoicetextdetector.MainActivity.API_KEY;

public class TaxPayerInfoActivity extends AppCompatActivity {

    private String tradeName, status, lastUpdate,address,registrationType,registrationDate,gstin;
    Chip statusChip;
    TextInputLayout textInputLayoutGSTIN,textInputLayoutTradename,textInputLayoutLastUpdate, textInputLayoutAddress, textInputLayoutRegistrationType, textInputLayoutRegistrationDate;
    ProgressBar progressBar;
    public static final String TAG = "Tax Payer Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax_payer_info);

        statusChip = findViewById(R.id.statusChip);
        textInputLayoutGSTIN= findViewById(R.id.textInputLayoutGST);
        textInputLayoutTradename=findViewById(R.id.textInputLayoutTradeName);
        textInputLayoutAddress=findViewById(R.id.textInputLayoutAddress);
        textInputLayoutRegistrationType= findViewById(R.id.textInputLayoutRegistrationType);
        textInputLayoutRegistrationDate = findViewById(R.id.textInputLayoutRegistrationDate);
        progressBar= findViewById(R.id.progressBar);

        Intent intent=getIntent();
        gstin=intent.getStringExtra("GSTIN");
        tradeName=intent.getStringExtra("tradeName");
        status=intent.getStringExtra("status");
        lastUpdate=intent.getStringExtra("lstupdate");
        address=intent.getStringExtra("address");
        registrationType=intent.getStringExtra("registrationType");
        registrationDate=intent.getStringExtra("registrationDate");

        if(status.equals("Active")){
            statusChip.setChipBackgroundColorResource(R.color.green);
        }else{
            statusChip.setChipBackgroundColorResource(R.color.red);
        }
        statusChip.append(status);
        textInputLayoutGSTIN.getEditText().setText(gstin);
        textInputLayoutTradename.getEditText().setText(tradeName);
        textInputLayoutAddress.getEditText().setText(address);
        textInputLayoutRegistrationType.getEditText().setText(registrationType);
        textInputLayoutRegistrationDate.getEditText().setText(registrationDate);

    }

    public void getFillingDetails(View view){
        String url ="https://sheet.gstincheck.ml/check-return/"+ API_KEY +"/"+ gstin +"";

        JsonRequest objectRequest=new JsonObjectRequest(Request.Method.GET, url, null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                responseFromApi=response;
//                        TempDialog.dismiss();
                hideProgressBar();
                try {
                    Boolean responseflag=response.getBoolean("flag");
                    Log.i("statusresponse","status:" +responseflag);

                    if(responseflag){

                        Toast.makeText(TaxPayerInfoActivity.this, response.getString("message"),Toast.LENGTH_SHORT).show();
                            fetchFillingDetailsFromApi(response);
                    }else{

                        Toast.makeText(TaxPayerInfoActivity.this, "response.getString(\"message\")d",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressBar();
                Toast.makeText(TaxPayerInfoActivity.this, error.toString(),Toast.LENGTH_SHORT).show();
                Log.e("statuscode",error.toString());

            }
        });

        MySingleton.getInstance(this).addToRequestQueue(objectRequest);



    }

    private void fetchFillingDetailsFromApi(JSONObject response) throws JSONException {

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