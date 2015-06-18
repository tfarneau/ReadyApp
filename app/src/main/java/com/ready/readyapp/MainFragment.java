package com.ready.readyapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by clement on 15/06/15.
 */
public class MainFragment extends Fragment{

    private boolean isListActivityStarted = false;
    private TextView mTextDetails;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private Profile profile;

    public MainFragment(){

    }

    private String checkProfile(){
        Profile profile = Profile.getCurrentProfile();
        JSONObject dataProfile = new JSONObject();
        try {
            dataProfile.put("id", profile.getId());
            dataProfile.put("name", profile.getName());

            return dataProfile.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataProfile.toString();

    }

    private void checkFriends(AccessToken accessToken, final Boolean newUser){
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {

                        if (object != null) startListActivity(object.toString(), newUser);

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void makeToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void startListActivity(String friends, Boolean newUser){

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken != null){
            Intent listActivity = new Intent(getActivity(), ListActivity.class);
            String dataProfile = checkProfile();
            listActivity.putExtra("friends",friends);
            listActivity.putExtra("profile",dataProfile);
            listActivity.putExtra("newUser",newUser.toString());
            startActivity(listActivity);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ParseUser.getCurrentUser() != null) checkFriends(AccessToken.getCurrentAccessToken(), false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_login_button, container, false);
    }

    public void parseLogin(){
        List<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {

            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (parseUser.isNew()) {
                    checkFriends(AccessToken.getCurrentAccessToken(), true);
                } else {
                    parseUser.saveInBackground();
                    checkFriends(AccessToken.getCurrentAccessToken(), false);
                }
            }
        });
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        ImageView facebookButton = (ImageView) view.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                parseLogin();
            }
        });

    }

    public void onStop(){
        super.onStop();
        //accessTokenTracker.stopTracking();
        //profileTracker.stopTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
