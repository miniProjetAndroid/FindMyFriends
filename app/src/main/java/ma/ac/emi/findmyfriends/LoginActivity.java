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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

import customfonts.MyEditText;
import customfonts.MyTextView;
import entities.Lieu;
import entities.Personne;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    TextView create;

    Typeface fonts1;

    MyEditText emailET;
    MyEditText passwordET;
    MyTextView errorMsgET;
    MyTextView btnSignIn;

    ProgressBar progressBar;
    Personne P = null;

    GoogleApiClient mGoogleApiClient;
    Lieu lastLieu = new Lieu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        create = (TextView) findViewById(R.id.create);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(it);
            }
        });


        fonts1 = Typeface.createFromAsset(LoginActivity.this.getAssets(),
                "fonts/Lato-Regular.ttf");


        TextView t4 = (TextView) findViewById(R.id.create);
        t4.setTypeface(fonts1);

        emailET = (MyEditText) findViewById(R.id.email);
        passwordET = (MyEditText) findViewById(R.id.password);
        errorMsgET = (MyTextView) findViewById(R.id.errorMsg);
        btnSignIn = (MyTextView) findViewById(R.id.signin1);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeLogin(emailET.getText().toString(), passwordET.getText().toString(), lastLieu);

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

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
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
            lastLieu.setLongitude(mLastLocation.getLongitude());
            lastLieu.setLatitude(mLastLocation.getLatitude());

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void invokeLogin(String email, String password, Lieu lieu) {
        //Request parameters
        final RequestParams params = new RequestParams();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("MMM d, yyyy HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        params.put("lieu", gson.toJson(lieu));
        params.put("email", gson.toJson(email));
        params.put("motDePasse", gson.toJson(password));

        // Show Progress Dialog
        // Make RESTful webservice call using AsyncHttpClient object
        final AsyncHttpClient client = new AsyncHttpClient();
        final String ip = getBaseContext().getString(R.string.ipAdress);
        AsyncHttpResponseHandler RH = new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                progressBar.setVisibility(View.GONE);

                // JSON Object
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .setDateFormat("MMM d, yyyy HH:mm:ss")
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();


                // When the JSON response has status boolean value assigned with true
                if ((!response.equals("")) && !response.equals(null)) {

                    Type type = new TypeToken<Personne>(){}.getType();

                    P = gson.fromJson(response, type);

                    Toast.makeText(getApplicationContext(), P.getNom() + " " + P.getPrenom() + " connecté", Toast.LENGTH_LONG).show();

                    Intent it = new Intent(LoginActivity.this, FriendsActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    it.putExtra("personne", P);
                    P.setPhoto(null);
                    startActivity(it);
                    finish();
                }
                // Else display error message
                else {
                    errorMsgET.setText("E-Mail ou mot de passe incorrect");
                    Toast.makeText(getApplicationContext(), "E-Mail ou mot de passe incorrect", Toast.LENGTH_LONG).show();
                }

            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Log.i("l", "lil");

                // Hide Progress Dialog
                // When Http response code is '404'
                if (statusCode == 404) {
                    Log.i("l", "Requested resource not found");
                    //Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Log.i("l", "Something went wrong at server end ");
                    //Toast.makeText(context, "Something went wrong at server end 2 ", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Log.i("l", "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]");
                    //Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        };
        client.get(ip + "/login/dologin", params, RH);
    }
}
