package com.botornot.demo.botornot;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.widget.LoginButton;

import java.util.Arrays;

public class LauncherFragment extends Fragment {

    private static final String TAG = "LauncherFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_launcher, container, false);
        LoginButton authButton = (LoginButton) view.findViewById(R.id.facebookButton);
        //authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_friends", "user_likes"));
        return view;
    }
}
