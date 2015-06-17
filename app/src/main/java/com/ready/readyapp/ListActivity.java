package com.ready.readyapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.apache.http.HttpConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class ListActivity extends ActionBarActivity {

    String[] listNames = {"Simon Corompt", "Clara Rua", "Clément Delaunay"};

    String[] listStatus = {"Dispo pour un jap", "Dispo pour un italien", "Dispo pour un jap"};

    ArrayList listPerson;
    ListView lvPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        overridePendingTransition(R.anim.pushdownin, R.anim.pushdownout);
        setContentView(R.layout.activity_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String friends = extras.getString("friends");
            String profile = extras.getString("profile");
            updateProfile(profile);
            updateFriendsList(friends);
        }

    }

    public static Drawable getPictureForFacebookId(String facebookId) {

        Drawable picture = null;
        InputStream inputStream = null;

        try {
            inputStream = new URL("https://graph.facebook.com/" + facebookId + "/picture?type=large").openStream();
            HttpURLConnection.setFollowRedirects(true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
        picture = Drawable.createFromStream(inputStream, "facebook-pictures");

        return picture;
    }

    public void updateProfile(String profile) {

        JSONObject dataProfile = null;
        try {
            dataProfile = new JSONObject(profile);

            View includeLayout = findViewById( R.id.userLayout );
            ImageView profileView = (ImageView) includeLayout.findViewById(R.id.picture);

            Drawable profilePicture;
            profilePicture = getPictureForFacebookId(dataProfile.getString("id"));

            profileView.setBackgroundDrawable(profilePicture);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public void updateFriendsList(String friends) {

        JSONArray dataFriends = null;
        try {
            dataFriends = new JSONObject(friends).getJSONObject("friends").getJSONArray("data");


            lvPerson = (ListView) findViewById(R.id.listPerson);
            listPerson = new ArrayList();

            Drawable[] listPictures = {
                    getResources().getDrawable(R.drawable.profile),
                    getResources().getDrawable(R.drawable.profile),
                    getResources().getDrawable(R.drawable.profile)};

            Drawable[] listOnline = {
                    getResources().getDrawable(R.drawable.btn_red),
                    getResources().getDrawable(R.drawable.btn_green),
                    getResources().getDrawable(R.drawable.btn_red)};


            for (int i = 0; i < dataFriends.length(); i++) {
                Drawable profilePicture;
                profilePicture = getPictureForFacebookId(dataFriends.getJSONObject(i).getString("id"));
                listPerson.add(new Person(i+1, dataFriends.getJSONObject(i).getString("name"), listStatus[0], profilePicture, listOnline[0]));

            }

            lvPerson.setAdapter(new PersonListAdapter(getApplicationContext(), listPerson));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
