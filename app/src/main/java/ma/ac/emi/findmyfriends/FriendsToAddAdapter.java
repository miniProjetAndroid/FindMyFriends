package ma.ac.emi.findmyfriends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import customfonts.MyTextView;
import entities.Personne;

public class FriendsToAddAdapter extends RecyclerView.Adapter<FriendsToAddAdapter.MyViewHolder> {

    private Personne me;
    private Context context;

    private List<Personne> amis = new ArrayList<Personne>();

    public FriendsToAddAdapter(Personne pers, Context context) {
        me= pers;
        this.context=context;
    }

    public List<Personne> getAmis() {
        return amis;
    }

    public void setAmis(List<Personne> amis) {
        this.amis = amis;
    }

    @Override
    public int getItemCount() {
        return amis.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_friends_to_add, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Personne a = amis.get(position);
        holder.display(a);
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {

        MyTextView NomComplet;
        MyTextView description;
        ImageView image;
        Button ajouter;




        private Personne currentA;


        public MyViewHolder(final View itemView) {
            super(itemView);

            NomComplet = (MyTextView)itemView.findViewById(R.id.nomCompletPersonneToAdd);
            description = (MyTextView)itemView.findViewById(R.id.infosPersonneToAdd);
            image = (ImageView)itemView.findViewById(R.id.imagePersonneToAdd);
            ajouter= (Button) itemView.findViewById(R.id.button_ajouter_a_liste_amis);






            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }



        public void display(final Personne a) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");

            currentA = a;
            Log.i("voilà", a.getNom());
            NomComplet.setText(a.getPrenom()+" "+a.getNom());

            String lieunaissance="";
            String habite="";
            String né="";
            String tel="";
            if(a.getLieuDeNaissance()!=null){lieunaissance=a.getLieuDeNaissance();}
            if(a.getHabite()!=null){habite=a.getHabite();}
            if(a.getDateDeNaissance()!=null){né=sdf.format(a.getDateDeNaissance());}
            if(a.getTelephone()!=null){tel=a.getTelephone();}
            description.setText("De : " +lieunaissance+".\n"+ "Habite à : "+habite+".\n"+
            "Né le : "+né +".\n"+ "Numéro de telephone : "+tel+".");
            Log.i("voilà", description.getText().toString());

                if (a.getPhoto() != null) {
                    Bitmap bMap = BitmapFactory.decodeByteArray(a.getPhoto(),0, a.getPhoto().length);
                    image.setImageBitmap(bMap);
                }

            ajouter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("hi","how you doing");
                    // appel WS suppression;
                    invokeAdd(me,a);
                }
            });




        }
    }


    public void invokeAdd(Personne pers, final Personne AAjouter){

        //Request parameters
        final RequestParams params = new RequestParams();
        Gson gson = new Gson();
        params.put("personne", gson.toJson(pers));
        params.put("personneaajouter", gson.toJson(AAjouter));

        // Show Progress Dialog
        // Make RESTful webservice call using AsyncHttpClient object
        final AsyncHttpClient client = new AsyncHttpClient();
        final String ip = context.getString(R.string.ipAdress);
        AsyncHttpResponseHandler RH=new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // JSON Object
                Gson gson = new Gson();
                // When the JSON response has status boolean value assigned with true

                if(response.equals("added")){
                    //

                    boolean b=amis.remove(AAjouter);
                    Log.i("l", Boolean.toString(b));
                    notifyDataSetChanged();
                }
                // Else display error message
                else{
                    Toast.makeText(context, "Nothing to show here!!", Toast.LENGTH_LONG).show();
                }
            }
            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {


                // Hide Progress Dialog
                // When Http response code is '404'
                if(statusCode == 404){

                     Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){

                     Toast.makeText(context, "Something went wrong at server end 2 ", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{

                     Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        };
        client.get( ip +"/amis/add",params ,RH);

    }



    public void invokeWS(Personne p , final int nbre, String nom, String prenom , final boolean premierappel, final Context context){

        //Request parameters
        final RequestParams params = new RequestParams();
        Gson gson = new Gson();
        if(amis.size()>0){
            params.put("offset", gson.toJson(amis.get(amis.size()-1).getId()));
        Log.i("id -----: ","----------- :" +amis.get(amis.size()-1).getId());
        }
        else{   params.put("offset",0);}
        params.put("nom", gson.toJson(nom));
        params.put("prenom", gson.toJson(prenom));
        params.put("nbre", gson.toJson(nbre));
        params.put("personne", gson.toJson(p));
        params.put("premierappel", gson.toJson(premierappel));


        // Show Progress Dialog
        // Make RESTful webservice call using AsyncHttpClient object
        final AsyncHttpClient client = new AsyncHttpClient();
        final String ip = context.getString(R.string.ipAdress);
        AsyncHttpResponseHandler RH=new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // JSON Object
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .setDateFormat("MMM d, yyyy HH:mm:ss")
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();
                // When the JSON response has status boolean value assigned with true
                if(!response.equals("")&& !response.equals(null) && !response.equals("[]")){
                    //
                    Type type = new TypeToken<List<Personne>>(){}.getType();

                     if(premierappel){amis=gson.fromJson(response, type);}
                    else{
                         List <Personne> listA=gson.fromJson(response, type);
                         amis.addAll(listA);}

                    notifyDataSetChanged();
                }
                // Else display error message
                else{
                    Toast.makeText(context, "Nothing to show here!!", Toast.LENGTH_LONG).show();
                }
            }
            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(context, "Something went wrong at server end 2 ", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        };
        client.get( ip +"/amis/affichertoadd",params ,RH);

    }

}