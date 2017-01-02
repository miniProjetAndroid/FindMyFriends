package ma.ac.emi.findmyfriends;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import customfonts.MyEditText;
import customfonts.MyTextView;
import entities.Lieu;
import entities.Personne;

public class RegisterActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    TextView signinhere;

    Typeface fonts1;

    TextView btnSignUp;
    MyEditText lastNameET;
    MyEditText firstNameET;
    MyEditText emailET;
    MyEditText passwordET;
    ProgressBar progress;
    MyTextView errorMsg;

    GoogleApiClient mGoogleApiClient;
    Lieu lastLieu = new Lieu();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signinhere = (TextView) findViewById(R.id.signinhere);

        signinhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(it);
            }
        });


        fonts1 = Typeface.createFromAsset(RegisterActivity.this.getAssets(),
                "fonts/Lato-Regular.ttf");


        TextView t1 = (TextView) findViewById(R.id.signinhere);
        t1.setTypeface(fonts1);


        btnSignUp = (TextView) findViewById(R.id.signup1);
        lastNameET = (MyEditText) findViewById(R.id.lastname);
        firstNameET = (MyEditText) findViewById(R.id.firstname);
        emailET = (MyEditText) findViewById(R.id.email);
        passwordET = (MyEditText) findViewById(R.id.password);
        progress = (ProgressBar) findViewById(R.id.progress);
        errorMsg = (MyTextView) findViewById(R.id.errorMsg);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    /**
     * Method gets triggered when Register button is clicked
     *
     */
    public void registerUser() {
        // Get NAme ET control value
        String lastname = lastNameET.getText().toString();
        String firstname = firstNameET.getText().toString();

        // Get Email ET control value
        String email = emailET.getText().toString().trim();

        // Get Password ET control value
        String password = passwordET.getText().toString();

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Name Edit View, Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(lastname) && Utility.isNotNull(firstname) && Utility.isNotNull(email) && Utility.isNotNull(password)) {
            // When Email entered is Valid
            if (Utility.validate(email)) {
                if (Utility.validateName(lastname) && Utility.validateName(firstname)) {
                    Personne personne = new Personne();
                    personne.setEmail(email);
                    personne.setNom(lastname);
                    personne.setPrenom(firstname);
                    personne.setMotDePasse(password);
                    personne.setLieu(lastLieu);

                    ObjectMapper mapper = new ObjectMapper();
                    String personneString = "";
                    try {
                        personneString = mapper.writeValueAsString(personne);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    // Put Http parameter name with value of Name Edit View control
                    params.put("personne", personneString);

                    invokeWS(params);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    errorMsg.setText("The names should contain at least 3 characters");
                }

            }
            // When Email is invalid
            else {
                errorMsg.setVisibility(View.VISIBLE);
                errorMsg.setText("Please enter valid email");
            }
        }
        // When any of the Edit View control left blank
        else {
            errorMsg.setVisibility(View.VISIBLE);
            errorMsg.setText("Please fill the form, don't leave any field blank");
        }
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        progress.setVisibility(View.VISIBLE);
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();


        client.get(getBaseContext().getString(R.string.ipAdress) + "/register/doregister", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                progress.setVisibility(View.GONE);
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        // Set Default Values for Edit View controls
                        setDefaultValues();
                        // Display successfully registered message using Toast
                        Toast.makeText(getApplicationContext(), "You are successfully registered!", Toast.LENGTH_LONG).show();
                        Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(it);
                    }
                    // Else display error message
                    else {
                        errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                progress.setVisibility(View.GONE);
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues() {
        lastNameET.setText("");
        emailET.setText("");
        firstNameET.setText("");
        passwordET.setText("");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lastLieu.setLatitude(mLastLocation.getLatitude());
            lastLieu.setLongitude(mLastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
