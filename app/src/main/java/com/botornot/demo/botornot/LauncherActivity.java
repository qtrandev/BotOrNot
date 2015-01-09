package com.botornot.demo.botornot;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class LauncherActivity extends ActionBarActivity {

    private Button mRegularButton;
    private LoginButton mFacebookButton;
    public static HashMap<Long, String> nameLookupMap = new HashMap<Long, String>();
    public static ArrayList<Long> friendList = new ArrayList<Long>();

    private static final String LOG_TAG = "QUYEN";
    private static final int LAUNCH = 0;
    private static final int SETTINGS = 1;
    private static final int FRAGMENT_COUNT = SETTINGS + 1;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean isResumed = false;
    private int mode = 10;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initButtons();

        FragmentManager fm = getSupportFragmentManager();
        fragments[LAUNCH] = fm.findFragmentById(R.id.launchFragment);
        fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        Log.d(LOG_TAG, "Facebook session state changed: " + state.toString());
        // Only make changes if the activity is visible
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            // Get the number of entries in the back stack
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (state.isOpened()) {
                // If the session state is open:
                // Show the authenticated fragment
                showFragment(LAUNCH, false);
                makeMeRequest(session);
            } else if (state.isClosed()) {
                friendList = new ArrayList<Long>();
                nameLookupMap = new HashMap<Long, String>();
                showFragment(LAUNCH, false);
            }
        }
    }

    private void makeMeRequest(Session session) {
        Log.d(LOG_TAG, "Requesting friends...");
        new Request(session, "v1.0/me/friends", null, HttpMethod.GET, new Request.Callback() {
                    public void onCompleted(Response response) {
                        //Log.d(LOG_TAG, "Facebook request response: "+response.toString());
                        //parseUserFromFQLResponse(response);
                        friendList = requestFriendIds(response);
                        Intent i = new Intent(LauncherActivity.this, BotOrNotActivity.class);
                        i.putExtra("MODE", 20);
                        //startActivity(i);
                    }
                }
        ).executeAsync();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open,
            // try to show the selection fragment
            //showFragment(ACTIVITY, false);
        } else {
            // otherwise present the splash screen
            // and ask the person to login.
            showFragment(LAUNCH, false);
        }
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void initButtons() {
        mRegularButton = (Button) findViewById(R.id.regularButton);
        getRegularButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LauncherActivity.this, BotOrNotActivity.class);
                i.putExtra("MODE", 10);
                startActivity(i);
            }
        });

        mFacebookButton = (LoginButton) findViewById(R.id.facebookButton);
//        getFacebookButton().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(LauncherActivity.this, BotOrNotActivity.class);
//                i.putExtra("MODE", 20);
//                startActivity(i);
//            }
//        });
    }

    private Button getRegularButton() {
        return mRegularButton;
    }

    private LoginButton getFacebookButton() {
        return mFacebookButton;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
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
            showFragment(SETTINGS, false);
            return true;
        } else if (id == R.id.action_intro) {
            showFragment(LAUNCH, false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Long> requestFriendIds(Response response) {
        ArrayList<Long> ids = new ArrayList<Long>();
        nameLookupMap.clear();
        try {
            GraphObject go  = response.getGraphObject();
            JSONObject jso = go.getInnerJSONObject();
            JSONArray dataArray = jso.getJSONArray( "data" );
            JSONObject item;
            String name;
            Long id;
            for(int i = 0; i < dataArray.length(); ++i) {
                item = dataArray.getJSONObject(i);
                id = item.getLong("id");
                name = item.getString("name");
                ids.add(id);
                nameLookupMap.put(id, name.split(" ")[0]);
                Log.i(LOG_TAG, "id="+id+"name="+name);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,e.toString());
        }
        return ids;
    }
}
