package ma.ac.emi.findmyfriends;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import customfonts.MyEditText;
import customfonts.MyTextView;
import entities.Personne;

public class LookForFriendActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static MyEditText Nom;
    private static MyEditText Prenom;
    private static NavigationView navigationView;
    private static MyTextView errorView;

    Personne pers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_for_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Find friend");

        pers = (Personne)getIntent().getSerializableExtra("personne");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Profil utilisateur dans le NavHeader
        ImageView navImageProfile = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.navImageProfile);
        TextView navNameProfile = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navNameProfile);
        TextView navEmailProfile = (TextView)navigationView.getHeaderView(0).findViewById(R.id.navEmailProfile);


        navNameProfile.setText(pers.getPrenom() + " "+ pers.getNom());
        navEmailProfile.setText(pers.getEmail());
        if( pers.getPhoto() != null){
            Bitmap bMap = BitmapFactory.decodeByteArray(pers.getPhoto(),0,pers.getPhoto().length);
            navImageProfile.setImageBitmap(bMap);
        }


        Nom = (MyEditText)findViewById(R.id.NomRecherche);
        Prenom = (MyEditText)findViewById(R.id.PrenomRecherche);
        errorView = (MyTextView)findViewById(R.id.errorMsgFriendLookUp);





    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }






    public void finichActivity(View view){
        finish();
    }

    private boolean verifyForm(){
        if(!Utility.isNotNull(Nom.getText().toString()) && !Utility.isNotNull(Prenom.getText().toString()))
            return false;
        return true;
    }

    public void lookUp(View view){
        if(verifyForm() == false){
            errorView.setText("Please fill one of the fields");
            errorView.setVisibility(View.VISIBLE);
        }
        else{

            String nom = Nom.getText().toString();
            String prenom = Prenom.getText().toString();
            Intent it = new Intent(LookForFriendActivity.this, AddFriendsActivity.class);
            it.putExtra("personne",pers);
            it.putExtra("nom", nom);
            it.putExtra("prenom", prenom);
            startActivity(it);


        }
    }


}