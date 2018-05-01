package com.dragons.aurora.fragment.intro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dragons.aurora.R;

import java.util.Objects;

public class Page2Fragment extends Fragment {

    View v;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.intro_page2, container, false);
        Button ask_perm = v.findViewById(R.id.enable_storage_perms);
        ask_perm.setOnClickListener((v1 -> ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1)));
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ask_perm.setText("Done");
            ask_perm.setClickable(false);
        }
        return v;
    }
}
