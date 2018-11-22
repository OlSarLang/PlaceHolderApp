package com.example.left4candy.placeholderapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OverviewFragment extends Fragment {
    private static final String TAG = "OverviewFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference backgroundImageRef;
    private StorageReference profileImageRef;

    private ImageView backgroundImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview_fragment, container, false);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);
        backgroundImageRef = mStorage.child("images/background.jpg");

        backgroundImage = view.findViewById(R.id.overviewBackground);
        loadProfile();

        return view;
    }

    public void loadProfile(){
        loadImage(backgroundImageRef, backgroundImage);
    }

    public void loadImage(StorageReference ref, final ImageView imgV){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(OverviewFragment.this).load(uri).into(imgV);
                Toast.makeText(getContext(), "Background Loaded", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Could not find background", Toast.LENGTH_LONG).show();
            }
        });
    }

    public class MyAppGlideModule extends AppGlideModule {

        @Override
        public void registerComponents(Context context, Glide glide, Registry registry) {
            // Register FirebaseImageLoader to handle StorageReference
            registry.append(StorageReference.class, MainAdminActivity.class,
                    (ResourceDecoder<StorageReference, MainAdminActivity>) new FirebaseImageLoader.Factory());
        }
    }

}
