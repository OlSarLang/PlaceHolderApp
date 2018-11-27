package com.example.left4candy.placeholderapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewFragment extends Fragment {
    private static final String TAG = "OverviewFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private int picId;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference backgroundImageRef;
    private StorageReference customImagesRef;

    private ImageView backgroundImage;
    private ImageView positionBackground;
    private RelativeLayout myLayout;

    private List<CustomMarker> customMarkerList; //List over all loaded markers
    private Map<Integer, Integer> pressableCustomMarkerList; //List of the markers shown, this is used to figure out which marker the user pressed and then gets that marker from customMarkerList
    private List<ImageButton> visibleCustomMarkerList; //The visible markers of the above mentioned list
    private CustomMarker customMarker;
    private VisibleMarker visibleMarker;
    private ImageButton imageCircle;
    private String markerColor = "green";

    private ArrayList<String> headerList = new ArrayList<>();
    private ArrayList<String> subHeaderOneList = new ArrayList<>();
    private ArrayList<String> subHeaderTwoList = new ArrayList<>();

    private Bitmap smallRed;
    private Bitmap smallGreen;
    private Bitmap smallBlue;
    private Bitmap smallYellow;
    private Bitmap smallWhite;

    private String newMarkerId;
    private String oldMarkerId;

    //solid color markers
    private int height = 80;
    private int width = 80;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview_fragment, container, false);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);
        backgroundImageRef = mStorage.child("images/background.jpg");
        customImagesRef = mStorage.child("images/custom/" + picId);

        BitmapDrawable bitmapRed = (BitmapDrawable)getResources().getDrawable(R.drawable.colorred);
        BitmapDrawable bitmapGreen = (BitmapDrawable)getResources().getDrawable(R.drawable.colorgreen);
        BitmapDrawable bitmapBlue = (BitmapDrawable)getResources().getDrawable(R.drawable.colorblue);
        BitmapDrawable bitmapYellow = (BitmapDrawable)getResources().getDrawable(R.drawable.coloryellow);
        BitmapDrawable bitmapWhite = (BitmapDrawable)getResources().getDrawable(R.drawable.colorwhite);
        Bitmap red = bitmapRed.getBitmap();
        Bitmap green = bitmapGreen.getBitmap();
        Bitmap blue = bitmapBlue.getBitmap();
        Bitmap yellow = bitmapYellow.getBitmap();
        Bitmap white = bitmapWhite.getBitmap();
        smallRed = Bitmap.createScaledBitmap(red, width, height, false);
        smallGreen = Bitmap.createScaledBitmap(green, width, height, false);
        smallBlue = Bitmap.createScaledBitmap(blue, width, height, false);
        smallYellow = Bitmap.createScaledBitmap(yellow, width, height, false);
        smallWhite = Bitmap.createScaledBitmap(white, width, height, false);

        customMarkerList = new ArrayList<>();
        pressableCustomMarkerList = new HashMap<Integer, Integer>();
        visibleCustomMarkerList = new ArrayList<>();

        myLayout = view.findViewById(R.id.rl_container);
        backgroundImage = view.findViewById(R.id.overviewBackground);
        positionBackground = view.findViewById(R.id.positionBackground);
        loadProfile();
        addTouchListener();


        return view;
    }

    private void addTouchListener(){

        positionBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int x = Math.round(event.getX());
                int y = Math.round(event.getY());

                createMarker(x, y);

                System.out.println("Coordinates: X=" + x +" Y="+ y);

                return false;
            }
        });
    }

    public void createMarker(int x, int y){
        final int placeX = x;
        final int placeY = y;
        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.custom_marker_info, null);
        //TODO Fields to give names, color, image etc should be declared here
        Button saveNewMarker = mView.findViewById(R.id.saveButton);
        Button cancelNewMarker = mView.findViewById(R.id.cancelButton);

        aBuilder.setView(mView);
        final AlertDialog dialog = aBuilder.create();
        Log.d("AlertDialog ", "has been created");

        cancelNewMarker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                dialog.dismiss();
            }
        });

        saveNewMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customMarker = new CustomMarker(placeX, placeY);
                customMarker.setSolidColor(markerColor);
                //TODO set id etc
                customMarker.setSolidColor("green");
                customMarker.setMarkerName("Hope it works");
                customMarkerList.add(customMarker);

                Toast.makeText(getContext(), "Marker added", Toast.LENGTH_SHORT).show();
                loadMarkers(customMarker);
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public void loadMarkers(CustomMarker customMarker){
        visibleMarker = new VisibleMarker(customMarker.getMarkerId(), customMarker.getxPos(), customMarker.getyPos());
        pressableCustomMarkerList.put(customMarker.getMarkerId(), visibleMarker.getMarkerId());

        imageCircle = new ImageButton(getActivity());
        imageCircle.setForegroundGravity(Gravity.LEFT);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.setMargins(visibleMarker.getxPos(), visibleMarker.getyPos(), 0, 0);
        imageCircle.setImageBitmap(smallRed);
        imageCircle.setLayoutParams(layoutParams);
        myLayout.addView(imageCircle);
    }


    //IMAGES//
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
    //END IMAGES//
    /*
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.marker_recycler_view);
        MarkerRecyclerViewAdapter recyclerViewAdapter = new MarkerRecyclerViewAdapter(headerList, subHeaderOneList, subHeaderTwoList, getContext());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }*/

}
