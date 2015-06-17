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

import org.json.JSONException;
import org.json.JSONObject;

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
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d("Ready", "onSuccess");
            AccessToken accessToken = loginResult.getAccessToken();
            checkFriends(accessToken);

        }


        @Override
        public void onCancel() {
           Activity activity = getActivity();
           makeToast(activity.getString(R.string.toast_connection_cancel));
        }

        @Override
        public void onError(FacebookException e) {
            Activity activity = getActivity();
            makeToast(activity.getString(R.string.toast_connection_error));
        }
    };

    public MainFragment(){

    }

    private String checkProfile(){
        Profile profile = Profile.getCurrentProfile();
        JSONObject dataProfile = new JSONObject();
        try {
            dataProfile.put("id", profile.getId());
            dataProfile.put("name", profile.getName());
            dataProfile.put("picture", profile.getProfilePictureUri(250, 250));

            return dataProfile.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataProfile.toString();

    }

    private void checkFriends(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {

                        if (object != null) startListActivity(object.toString());

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

    public void startListActivity(String friends){

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken != null){
            Intent listActivity = new Intent(getActivity(), ListActivity.class);
            String dataProfile = checkProfile();
            listActivity.putExtra("friends",friends);
            listActivity.putExtra("profile",dataProfile);
            startActivity(listActivity);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager=CallbackManager.Factory.create();
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                Profile profile = Profile.getCurrentProfile();
                checkFriends(newToken);
            }
        };
        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                //updateProfile(newProfile);
            }
        };
        accessTokenTracker.startTracking();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_login_button, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);
        profile=Profile.getCurrentProfile();


    }

    public void onResume(){
        super.onResume();
        //Profile profile=Profile.getCurrentProfile();
    }

    public void onStop(){
        super.onStop();
        //accessTokenTracker.stopTracking();
        //profileTracker.stopTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
