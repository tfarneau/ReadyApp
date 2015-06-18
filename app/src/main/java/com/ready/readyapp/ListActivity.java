package com.ready.readyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.software.shell.fab.ActionButton;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ListActivity extends FragmentActivity implements EditDialogFragment.EditDialogListener, View.OnClickListener {

    ArrayList listPerson;
    ListView lvPerson;

    public void ButtonOnClick(View v) {
        switch (v.getId()) {
            case R.id.edit_button:
                Bundle extras = getIntent().getExtras();
                showEditDialog(extras);
                break;
        }
    };


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
            String newUser = extras.getString("newUser");
            if(newUser.equals("true")) newProfile(profile);
            else updateProfile(profile, null, null, null);
            updateFriendsList(friends);
        }

    }

    public static String getFacebookPictureUrl(String facebookId){
        return "https://graph.facebook.com/" + facebookId + "/picture?type=large";
    }


    public static Drawable getPictureForFacebookId(String url) {

        Drawable picture = null;
        InputStream inputStream = null;

        try {
            inputStream = new URL(url).openStream();
            HttpURLConnection.setFollowRedirects(true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
        picture = Drawable.createFromStream(inputStream, "facebook-pictures");

        return picture;
    }

    public void updateViewUser(ParseUser parseUser){

        Drawable profilePicture;

        profilePicture = getPictureForFacebookId(parseUser.get("profile_pic").toString());

        View includeLayout = findViewById(R.id.userLayout);
        ImageView profileView = (ImageView) includeLayout.findViewById(R.id.picture);
        TextView textStatus = (TextView) includeLayout.findViewById(R.id.status);
        profileView.setBackgroundDrawable(profilePicture);
        textStatus.setText(parseUser.get("status").toString());

        ActionButton actionButton = (ActionButton) findViewById(R.id.action_button);

        if(parseUser.get("available") == true){
            actionButton.setImageDrawable(getResources().getDrawable(R.drawable.off));
            actionButton.setButtonColor(getResources().getColor(R.color.color_red));
        }
        else{
            actionButton.setImageDrawable(getResources().getDrawable(R.drawable.on));
            actionButton.setButtonColor(getResources().getColor(R.color.color_primary));
        }

        actionButton.show();
        actionButton.setOnClickListener(this);

        RelativeTimeTextView timeStatus = (RelativeTimeTextView) includeLayout.findViewById(R.id.time);
        timeStatus.setReferenceTime(parseUser.getUpdatedAt().getTime());
    }

    public void updateProfile(String profile, String status, Boolean available, JSONArray unsubscribedFrom) {

        final ParseUser parseUser = ParseUser.getCurrentUser();

        try {
            final JSONObject dataProfile = new JSONObject(profile);

            parseUser.put("name",dataProfile.getString("name"));
            if(status != null) parseUser.put("status", status);
            if(available != null) parseUser.put("available", available);
            if(unsubscribedFrom != null) parseUser.put("unsubscribed_from", unsubscribedFrom);
            updateViewUser(parseUser);
            parseUser.saveInBackground();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void newProfile(String profile) {

        final ParseUser parseUser = ParseUser.getCurrentUser();

        try {
            final JSONObject dataProfile = new JSONObject(profile);

            parseUser.put("name",dataProfile.getString("name"));
            parseUser.put("fbId", dataProfile.getString("id"));
            parseUser.put("status", this.getString(R.string.default_status));
            parseUser.put("profile_pic", getFacebookPictureUrl(dataProfile.getString("id")));
            parseUser.put("available", true);
            parseUser.put("unsubscribed_from", new JSONArray());
            updateViewUser(parseUser);
            parseUser.saveInBackground();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }


    static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

    public void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }


    public void updateFriendsList(String friends) {

        lvPerson = (ListView) findViewById(R.id.listPerson);
        listPerson = new ArrayList();

        JSONArray dataFriends = null;
        try {
            dataFriends = new JSONObject(friends).getJSONObject("friends").getJSONArray("data");

            ArrayList<String> friendsIds = new ArrayList<String>();

            for (int i = 0; i < dataFriends.length(); i++) {
                friendsIds.add(dataFriends.getJSONObject(i).getString("id"));
            }

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereContainedIn("fbId", friendsIds);
            query.findInBackground(new FindCallback<ParseUser>() {

                @Override
                public void done(List<ParseUser> listUsers, com.parse.ParseException e) {
                    if (e == null) {
                        for (int i = 0; i < listUsers.size(); i++) {
                            Drawable profilePicture;
                            Drawable available = null;

                            if(listUsers.get(i).get("available").toString().equals("true")){
                                available = getResources().getDrawable(R.drawable.btn_green);
                            }

                            if(listUsers.get(i).get("available").toString().equals("false")){
                                Log.d("Ready", listUsers.get(i).get("available").toString());
                                available = getResources().getDrawable(R.drawable.btn_red);
                            }

                            String status;

                            if(listUsers.get(i).get("status").toString() != null) status = listUsers.get(i).get("status").toString();
                            else status = "";

                            profilePicture = getPictureForFacebookId(listUsers.get(i).get("profile_pic").toString());

                            listPerson.add(new Person(i+1, listUsers.get(i).get("name").toString(), status, profilePicture, available, listUsers.get(i).getUpdatedAt().getTime()));

                        }
                        lvPerson.setAdapter(new PersonListAdapter(getApplicationContext(), listPerson));
                    } else {
                        makeToast(getCurrentActivity().getString(R.string.fetch_friends_error));
                    }
                }
            });

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

    public void showEditDialog(Bundle extras) {
        DialogFragment dialog = new EditDialogFragment();
        Log.d("Ready", extras.toString());
        dialog.setArguments(extras);
        dialog.show(getFragmentManager(), "EditDialogFragment");
    }

    public void onResume(){
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String friends = extras.getString("friends");
            updateFriendsList(friends);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.action_button){
            Bundle extras = getIntent().getExtras();
            String profile = null;
            if (extras != null) {
                profile = extras.getString("profile");
            }
            Log.d("Ready", ParseUser.getCurrentUser().get("available").toString());
            if(ParseUser.getCurrentUser().get("available") == true) updateProfile(profile, null, false, null);
            else updateProfile(profile, null, true, null);

        }
    }
}
