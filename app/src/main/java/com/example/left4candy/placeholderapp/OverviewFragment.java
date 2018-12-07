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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private Map<CustomMarker, ImageButton> pressableCustomMarkerList; //List of the markers shown, this is used to figure out which marker the user pressed and then gets that marker from customMarkerList
    private List<ImageButton> visibleCustomMarkerList; //The visible markers of the above mentioned list
    private CustomMarker customMarker;
    private VisibleMarker visibleMarker;
    private ImageButton imageCircle;

    private RecyclerView recyclerView;

    private Bitmap redMarker;
    private Bitmap greenMarker;
    private Bitmap blueMarker;
    private Bitmap yellowMarker;
    private Bitmap whiteMarker;

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

        //TODO ondata change listener instead of loadimages

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
        redMarker = Bitmap.createScaledBitmap(red, width, height, false);
        greenMarker = Bitmap.createScaledBitmap(green, width, height, false);
        blueMarker = Bitmap.createScaledBitmap(blue, width, height, false);
        yellowMarker = Bitmap.createScaledBitmap(yellow, width, height, false);
        whiteMarker = Bitmap.createScaledBitmap(white, width, height, false);

        customMarkerList = new ArrayList<>();
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

        //TODO OnTouch for the visibleMarkerList()


    }

    public void createMarker(int x, int y){
        final int placeX = x;
        final int placeY = y;

        customMarker = new CustomMarker(placeX, placeY);
        customMarker.setSolidColor(greenMarker);
        //TODO set id etc

        customMarker.setMarkerName("Hope it works");

        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.custom_marker_info, null);
        //TODO Fields to give names, color, image etc should be declared here
        Button saveNewMarker = mView.findViewById(R.id.saveButton);
        Button cancelNewMarker = mView.findViewById(R.id.cancelButton);
        Button addFieldButton = mView.findViewById(R.id.addFieldButton);
        //TODO Fix so that you can choose colors/images
        ImageView markerImage = mView.findViewById(R.id.custom_marker_icon);
        markerImage.setImageResource(R.drawable.colorgreen);
        final EditText markerName = mView.findViewById(R.id.markerName);

        aBuilder.setView(mView);
        initRecyclerView(mView, customMarker);
        final AlertDialog dialog = aBuilder.create();
        Log.d("AlertDialog ", "has been created");

        addFieldButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Toast.makeText(getContext(), "You need to create the marker before adding fields.", Toast.LENGTH_LONG).show();
            }
        });

        cancelNewMarker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                dialog.dismiss();
            }
        });

        saveNewMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customMarker.setMarkerName(markerName.getText().toString());
                customMarkerList.add(customMarker);

                for(int i = 0; i < customMarker.getMarkerItems().size(); i++){
                    System.out.println("Amount of items: " + i);
                }

                Toast.makeText(getContext(), "Marker added", Toast.LENGTH_SHORT).show();
                loadMarkers(customMarker);
                //TODO save to database
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public void loadMarkers(CustomMarker customMarker){
        visibleMarker = new VisibleMarker(customMarker.getMarkerId(), customMarker.getxPos(), customMarker.getyPos());

        imageCircle = new ImageButton(getActivity());
        imageCircle.setForegroundGravity(Gravity.LEFT);
        imageCircle.setBackground(null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.setMargins(visibleMarker.getxPos(), visibleMarker.getyPos(), 0, 0);
        imageCircle.setImageBitmap(customMarker.getSolidColor());
        imageCircle.setLayoutParams(layoutParams);
        myLayout.addView(imageCircle);
        visibleCustomMarkerList.add(imageCircle);

        imageCircle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageButton clickedButton = (ImageButton) v;
                int index = visibleCustomMarkerList.indexOf(clickedButton);
                if(index!=-1){
                    System.out.println("Clicked marker " + index);
                    showMarker(customMarkerList.get(index));
                }
            }
        });
    }

    public void showMarker(final CustomMarker customMarker){
        customMarker.getSolidColor();

        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.custom_marker_info, null);
        //TODO Fields to give names, color, image etc should be declared here
        Button saveNewMarker = mView.findViewById(R.id.saveButton);
        Button cancelNewMarker = mView.findViewById(R.id.cancelButton);
        Button addFieldButton = mView.findViewById(R.id.addFieldButton);
        //TODO Fix so that you can choose colors/images
        final ImageView markerImage = mView.findViewById(R.id.custom_marker_icon);
        markerImage.setImageResource(R.drawable.colorgreen);
        TextView markerId = mView.findViewById(R.id.markerId);
        final EditText markerName = mView.findViewById(R.id.markerName);
        markerName.setText(customMarker.getMarkerName());

        aBuilder.setView(mView);
        initRecyclerView(mView, customMarker);
        final AlertDialog dialog = aBuilder.create();
        Log.d("AlertDialog ", "has been created");

        addFieldButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                MarkerItem newMarkerItem = new MarkerItem();
                customMarker.getMarkerItems().add(newMarkerItem);
                dialog.dismiss();
                showMarker(customMarker);
            }
        });

        cancelNewMarker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                dialog.dismiss();
            }
        });

        saveNewMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerItem markerItem = new MarkerItem();

                //TODO save to database
                customMarker.setMarkerName(markerName.getText().toString());
                for(int i = 0; i < customMarker.getMarkerItems().size(); i++){
                    markerItem = customMarker.getMarkerItems().get(i);
                    View childView = recyclerView.getChildAt(i);
                    EditText headerText = childView.findViewById(R.id.headertext);
                    EditText subHeaderOneText = childView.findViewById(R.id.subheadertext);
                    markerItem.setHeader(headerText.getText().toString());
                    markerItem.setSubHeader(subHeaderOneText.getText().toString());
                    System.out.println("Amount of items: " + i);
                }
                dialog.dismiss();

            }
        });


        dialog.show();
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

    private void initRecyclerView(View view, CustomMarker customMarker){
        recyclerView = view.findViewById(R.id.marker_recycler_view);
        MarkerRecyclerViewAdapter recyclerViewAdapter = new MarkerRecyclerViewAdapter(customMarker.getMarkerItems(), getContext());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

}
