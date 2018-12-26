package com.example.left4candy.placeholderapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
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
    private DatabaseReference mDatabase;
    private DatabaseReference databaseMarkerRef;

    private FrameLayout myLayout;

    private List<CustomMarker> customMarkerList; //List over all loaded markers
    private CustomMarker customMarker;
    private Map<CustomMarker, ImageButton> pressableCustomMarkerList; //List of the markers shown, this is used to figure out which marker the user pressed and then gets that marker from customMarkerList
    private List<ImageButton> visibleCustomMarkerList; //The visible markers of the above mentioned list
    private VisibleMarker visibleMarker;
    private ImageButton imageCircle;

    private RecyclerView recyclerView;
    private MarkerRecyclerViewAdapter recyclerViewAdapter;
    private DatabaseReference iDatabaseRef;
    private RecyclerView imageRecyclerView;
    private ImagePickerAdapter iAdapter;
    private List<Upload> iUploads;
    private ToggleButton togglePlaceMarker;

    private Bitmap redMarker;
    private Bitmap greenMarker;
    private Bitmap blueMarker;
    private Bitmap yellowMarker;
    private Bitmap mapMap;

    private View thisView;

    long timeWhenDown;
    private boolean placeMarker;


    //solid color markers
    private int height = 100;
    private int width = 100;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //FIREBASE USER INFO
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);

        //FIREBASE IMAGES
        backgroundImageRef = mStorage.child("images/background.jpg");
        customImagesRef = mStorage.child("images/custom/" + picId);

        //FIREBASE MARKERS
        mDatabase = FirebaseDatabase.getInstance().getReference().child(userUid);
        iDatabaseRef = mDatabase.child("images/custom/");
        databaseMarkerRef = mDatabase.child("markers/");

        databaseMarkerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("onDataChange()", "reached");
                getDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //TODO ondata change listener instead of loadimages

        BitmapDrawable bitmapRed = (BitmapDrawable)getResources().getDrawable(R.drawable.colorred);
        BitmapDrawable bitmapGreen = (BitmapDrawable)getResources().getDrawable(R.drawable.colorgreen);
        BitmapDrawable bitmapBlue = (BitmapDrawable)getResources().getDrawable(R.drawable.colorblue);
        BitmapDrawable bitmapYellow = (BitmapDrawable)getResources().getDrawable(R.drawable.coloryellow);
        Bitmap red = bitmapRed.getBitmap();
        Bitmap green = bitmapGreen.getBitmap();
        Bitmap blue = bitmapBlue.getBitmap();
        Bitmap yellow = bitmapYellow.getBitmap();
        redMarker = Bitmap.createScaledBitmap(red, width, height, false);
        greenMarker = Bitmap.createScaledBitmap(green, width, height, false);
        blueMarker = Bitmap.createScaledBitmap(blue, width, height, false);
        yellowMarker = Bitmap.createScaledBitmap(yellow, width, height, false);

        customMarkerList = new ArrayList<>();
        visibleCustomMarkerList = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview_fragment, container, false);

        togglePlaceMarker = view.findViewById(R.id.togglePlaceMarker);

        togglePlaceMarker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    togglePlaceMarker.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.colorred));
                    placeMarker = true;
                }else{
                    togglePlaceMarker.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.colorgreen));
                    placeMarker = false;
                }
            }
        });
        togglePlaceMarker.setBackgroundDrawable(getResources().getDrawable(R.drawable.colorgreen));
        myLayout = view.findViewById(R.id.rl_container);
        loadProfile();
        addTouchListener();

        return view;
    }

    @Override
    public void onResume() {
        databaseMarkerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("onDataChange()", "reached");
                getDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        super.onResume();
    }

    public void getDatabase(DataSnapshot dataSnapshot){
        Log.d("getDatabase()", "reached");
        myLayout.removeAllViewsInLayout();
        customMarkerList.clear();
        visibleCustomMarkerList.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            CustomMarker cmMarker = new CustomMarker();
            cmMarker = ds.getValue(CustomMarker.class);
            CustomMarker.setId(cmMarker.getMarkerId());
            customMarkerList.add(cmMarker);
            loadMarker(cmMarker);
        }
    }

    private void addTouchListener(){

        myLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float x = Math.round(event.getX());
                float y = Math.round(event.getY());

                if(placeMarker == true){
                    createMarker(x-70, y-70);
                    togglePlaceMarker.setChecked(false);
                }

                System.out.println("Coordinates: X=" + x +" Y="+ y);

                return false;
            }
        });

    }

    public void createMarker(float x, float y){
        final float placeX = x;
        final float placeY = y;


        customMarker = new CustomMarker(placeX, placeY);

        customMarker.setMarkerName("Hope it works");

        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.custom_marker_info, null);
        Button saveNewMarker = mView.findViewById(R.id.saveButton);
        Button cancelNewMarker = mView.findViewById(R.id.cancelButton);
        Button addFieldButton = mView.findViewById(R.id.addFieldButton);
        Button removeFieldButton = mView.findViewById(R.id.removeFieldButton);
        Button deleteMarker = mView.findViewById(R.id.deleteMarker);
        final RadioGroup radioGroup = mView.findViewById(R.id.radioGroup);

        final ImageView markerImage = mView.findViewById(R.id.custom_marker_icon);
        markerImage.setImageResource(R.drawable.colorgreen);
        final EditText markerName = mView.findViewById(R.id.markerName);

        aBuilder.setView(mView);
        initRecyclerView(mView, customMarker);
        final AlertDialog dialog = aBuilder.create();
        Log.d("AlertDialog ", "has been created");

        radioGroup.check(R.id.radioGreen);
        customMarker.setGreen(true);

        if(customMarker.isRed()){
            radioGroup.check(R.id.radioRed);
        }else if(customMarker.isGreen()){
            radioGroup.check(R.id.radioGreen);
        }else if(customMarker.isBlue()){
            radioGroup.check(R.id.radioBlue);
        }else if(customMarker.isYellow()){
            radioGroup.check(R.id.radioYellow);
        }else if(!customMarker.isYellow() && !customMarker.isBlue() && !customMarker.isGreen() && !customMarker.isRed()){
            radioGroup.clearCheck();
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioRed){
                    customMarker.setRed(true);
                    customMarker.setGreen(false);
                    customMarker.setBlue(false);
                    customMarker.setYellow(false);
                    customMarker.setSolid(true);
                    markerImage.setImageResource(R.drawable.colorred);
                    Log.d("Radiogroup", "Red");
                }else if(checkedId == R.id.radioGreen) {
                    customMarker.setRed(false);
                    customMarker.setGreen(true);
                    customMarker.setBlue(false);
                    customMarker.setYellow(false);
                    customMarker.setSolid(true);
                    markerImage.setImageResource(R.drawable.colorgreen);
                }else if(checkedId == R.id.radioBlue) {
                    customMarker.setRed(false);
                    customMarker.setGreen(false);
                    customMarker.setBlue(true);
                    customMarker.setYellow(false);
                    customMarker.setSolid(true);
                    markerImage.setImageResource(R.drawable.colorblue);
                }else if(checkedId == R.id.radioYellow){
                    customMarker.setRed(false);
                    customMarker.setGreen(false);
                    customMarker.setBlue(false);
                    customMarker.setYellow(true);
                    customMarker.setSolid(true);
                    markerImage.setImageResource(R.drawable.coloryellow);
                }
            }
        });

        addFieldButton.setVisibility(View.INVISIBLE);
        removeFieldButton.setVisibility(View.INVISIBLE);

        addFieldButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Toast.makeText(getContext(), getResources().getString(R.string.NewMarkerAddField), Toast.LENGTH_LONG).show();
            }
        });

        cancelNewMarker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                CustomMarker.setId(CustomMarker.getId()-1);
                dialog.dismiss();
            }
        });

        deleteMarker.setVisibility(View.INVISIBLE);

        saveNewMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customMarker.setMarkerName(markerName.getText().toString());

                for(int i = 0; i < customMarker.getMarkerItems().size(); i++){
                    System.out.println("Amount of items: " + i);
                }

                Toast.makeText(getContext(), getResources().getString(R.string.MarkerAdded), Toast.LENGTH_SHORT).show();
                databaseMarkerRef = mDatabase.child("markers/" + customMarker.getMarkerId());
                databaseMarkerRef.setValue(customMarker);
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public void deleteMarker(final CustomMarker customMarker){
        databaseMarkerRef = mDatabase.child("markers/" + customMarker.getMarkerId());
        databaseMarkerRef.removeValue();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void loadMarker(final CustomMarker customMarker){
        visibleMarker = new VisibleMarker(customMarker.getMarkerId(), customMarker.getxPos(), customMarker.getyPos());

        imageCircle = new ImageButton(getActivity());
        imageCircle.setForegroundGravity(Gravity.LEFT);
        imageCircle.setBackground(null);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.setMargins((int)visibleMarker.getxPos(), (int)visibleMarker.getyPos(), 0, 0);
        checkImageOrColor(customMarker, imageCircle, "MapMarker");
        imageCircle.setLayoutParams(layoutParams);

        imageCircle.setOnTouchListener(new View.OnTouchListener() {
            int prevX, prevY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageCircle.getLayoutParams());
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    timeWhenDown = System.currentTimeMillis();
                    Log.d("Action Down: " , String.valueOf(timeWhenDown));
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    if(System.currentTimeMillis() > timeWhenDown + 500){
                        v.setVisibility(View.INVISIBLE);
                        Log.d("Action Move: ", String.valueOf(System.currentTimeMillis()));
                        params.topMargin += (int)event.getRawY() - prevY;
                        prevY = (int)event.getRawY();
                        params.leftMargin += (int)event.getRawX() - prevX;
                        prevX = (int)event.getRawX();
                        Log.d("params", String.valueOf(params));
                        v.setLayoutParams(params); //It never moves the original! But it creates a new one where I let go because of the database valuelistener!
                        return true;
                        }
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(System.currentTimeMillis() > timeWhenDown + 500){
                        customMarker.setyPos(event.getY()-70);
                        customMarker.setxPos(event.getX()-70);
                        databaseMarkerRef = mDatabase.child("markers/" + customMarker.getMarkerId());
                        databaseMarkerRef.setValue(customMarker);
                        Log.d("Action Up: " , String.valueOf(System.currentTimeMillis()));

                        return true;
                    }else if (System.currentTimeMillis()-500 < timeWhenDown){
                        ImageButton clickedButton = (ImageButton) v;
                        int index = visibleCustomMarkerList.indexOf(clickedButton);
                        if(index!=-1){
                            System.out.println("Clicked marker " + index);
                            showMarker(customMarkerList.get(index), index);
                        }
                        Log.d("Action Up: " , String.valueOf(System.currentTimeMillis()));
                        return true;
                    }
                }
                return true;
            }
        });
        myLayout.addView(imageCircle);
        visibleMarker.setImageCircle(imageCircle);
        visibleCustomMarkerList.add(imageCircle);
    }

    public void showMarker(final CustomMarker customMarker, int index){
        final AlertDialog.Builder aBuilder = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.custom_marker_info, null);
        Button saveNewMarker = mView.findViewById(R.id.saveButton);
        Button cancelNewMarker = mView.findViewById(R.id.cancelButton);
        Button addFieldButton = mView.findViewById(R.id.addFieldButton);
        Button removeFieldButton = mView.findViewById(R.id.removeFieldButton);
        final Button deleteMarker = mView.findViewById(R.id.deleteMarker);
        final RadioGroup radioGroup = mView.findViewById(R.id.radioGroup);

        //final ImageButton imageButton = visibleCustomMarkerList.get(index).getImageCircle();

        final ImageView markerImage = mView.findViewById(R.id.custom_marker_icon);
        checkImageOrColor(customMarker, markerImage, "IconMarker");
        TextView markerId = mView.findViewById(R.id.markerId);
        markerId.setText(Integer.toString(customMarker.getMarkerId()));
        final EditText markerName = mView.findViewById(R.id.markerName);
        markerName.setText(customMarker.getMarkerName());

        aBuilder.setView(mView);
        initRecyclerView(mView, customMarker);
        final AlertDialog dialog = aBuilder.create();

        markerImage.setClickable(true);

        if(customMarker.isRed()){
            radioGroup.check(R.id.radioRed);
        }else if(customMarker.isGreen()){
            radioGroup.check(R.id.radioGreen);
        }else if(customMarker.isBlue()){
            radioGroup.check(R.id.radioBlue);
        }else if(customMarker.isYellow()){
            radioGroup.check(R.id.radioYellow);
        }else if(!customMarker.isYellow() && !customMarker.isBlue() && !customMarker.isGreen() && !customMarker.isRed()){
            radioGroup.clearCheck();
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId == R.id.radioRed){
                        customMarker.setRed(true);
                        customMarker.setGreen(false);
                        customMarker.setBlue(false);
                        customMarker.setYellow(false);
                        customMarker.setSolid(true);
                        markerImage.setImageResource(R.drawable.colorred);
                        Log.d("Radiogroup", "Red");
                    }else if(checkedId == R.id.radioGreen) {
                        customMarker.setRed(false);
                        customMarker.setGreen(true);
                        customMarker.setBlue(false);
                        customMarker.setYellow(false);
                        customMarker.setSolid(true);
                        markerImage.setImageResource(R.drawable.colorgreen);
                    }else if(checkedId == R.id.radioBlue) {
                        customMarker.setRed(false);
                        customMarker.setGreen(false);
                        customMarker.setBlue(true);
                        customMarker.setYellow(false);
                        customMarker.setSolid(true);
                        markerImage.setImageResource(R.drawable.colorblue);
                    }else if(checkedId == R.id.radioYellow){
                        customMarker.setRed(false);
                        customMarker.setGreen(false);
                        customMarker.setBlue(false);
                        customMarker.setYellow(true);
                        customMarker.setSolid(true);
                        markerImage.setImageResource(R.drawable.coloryellow);
                    }
            }
        });

        markerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "MarkerImage clicked", Toast.LENGTH_LONG).show();
                openImagePicker(markerImage, customMarker);
            }
        });

        addFieldButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                MarkerItem newMarkerItem = new MarkerItem();
                if(customMarker.getMarkerItems().size() > 4){
                    //TODO Crash when 14th has been added
                    Toast.makeText(getContext(), getResources().getString(R.string.TooManyFields), Toast.LENGTH_LONG).show();
                }
                else{
                    customMarker.getMarkerItems().add(newMarkerItem);
                    recyclerViewAdapter.notifyItemInserted(customMarker.getMarkerItems().size()-1);
                }
            }
        });

        removeFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customMarker.getMarkerItems().isEmpty()) {
                    Toast.makeText(getContext(), getResources().getString(R.string.NoFieldRemove), Toast.LENGTH_LONG).show();
                } else {
                    customMarker.getMarkerItems().remove(customMarker.getMarkerItems().size() - 1);
                    recyclerViewAdapter.notifyItemRemoved(customMarker.getMarkerItems().size());
                }
            }
        });

        cancelNewMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                }
        });

        deleteMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface aDialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteMarker(customMarker);
                                //myLayout.removeView(imageButton);
                                dialog.dismiss();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getResources().getString(R.string.DeleteMarker)).setPositiveButton(getResources().getString(R.string.Yes), dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.No), dialogClickListener).show();
            }
        });

        saveNewMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerItem markerItem;

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
                databaseMarkerRef = mDatabase.child("markers/" + customMarker.getMarkerId());
                databaseMarkerRef.setValue(customMarker);
                dialog.dismiss();

            }
        });


        dialog.show();
    }

    //IMAGES//
    public void loadProfile(){
        loadImage(backgroundImageRef, myLayout);
    }

    public void loadImage(StorageReference ref, final FrameLayout imgV){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(OverviewFragment.this).load(uri).apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background)).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imgV.setBackground(resource);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
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

    private void initRecyclerView(View view, CustomMarker customMarker){
        recyclerView = view.findViewById(R.id.marker_recycler_view);
        recyclerViewAdapter = new MarkerRecyclerViewAdapter(customMarker.getMarkerItems(), getContext());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }


    private void checkImageOrColor(CustomMarker customMarker, final ImageView imageView, String type){
        boolean isMapMarker = false;

        if(type == "MapMarker"){
            isMapMarker = true;
        }else if(type == "IconMarker"){
            isMapMarker = false;
        }
        boolean isColor = false, isImage = false;

        if(customMarker.isRed() == true){
            if(isMapMarker){
                imageView.setImageBitmap(redMarker);
            }else {
                imageView.setImageResource(R.drawable.colorred);
            }
            isColor = true;

        }else if(customMarker.isGreen() == true){
            if(isMapMarker){
                imageView.setImageBitmap(greenMarker);
            }else{
                imageView.setImageResource(R.drawable.colorgreen);
            }
            isColor = true;

        }else if(customMarker.isBlue() == true){
            if(isMapMarker){
                imageView.setImageBitmap(blueMarker);
            }else{
                imageView.setImageResource(R.drawable.colorblue);
            }
            isColor = true;

        }else if(customMarker.isYellow() == true){
            if(isMapMarker){
                imageView.setImageBitmap(yellowMarker);
            }else{
                imageView.setImageResource(R.drawable.coloryellow);
            }
            isColor = true;
        }

        if(type == "MapMarker" && customMarker.isRed() == false && customMarker.isGreen() == false && customMarker.isBlue() == false && customMarker.isYellow() == false){
            isImage = true;
            Picasso.get()
                    .load(customMarker.getImageUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mapMap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                            imageView.setImageBitmap(getRoundedShape(mapMap));
                        }
                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }

        if(type == "IconMarker" && customMarker.isRed() == false && customMarker.isGreen() == false && customMarker.isBlue() == false && customMarker.isYellow() == false){
            isImage = true;
            Picasso.get()
                    .load(customMarker.getImageUrl())
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }


    }

    public Bitmap getRoundedShape(Bitmap scaleBitMapImage){
        Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) width- 1) / 2,
                ((float)height - 1) / 2,
                (Math.min(((float) width),
                        ((float) height)) / 2),
                Path.Direction.CCW);
        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitMapImage;
        canvas.drawBitmap(sourceBitmap, new Rect(0,0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);
        return targetBitmap;
    }

    private void openImagePicker(final ImageView imageView, final CustomMarker customMarker){
        final AlertDialog.Builder iBuilder = new AlertDialog.Builder(getContext());
        View iView = getLayoutInflater().inflate(R.layout.marker_image_picker_fragment, null);
        imageRecyclerView = iView.findViewById(R.id.image_recyclerview);
        imageRecyclerView.setHasFixedSize(true);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Button exitImagePicker = (Button) iView.findViewById(R.id.exitImagePicker);

        iUploads = new ArrayList<Upload>();


        iBuilder.setView(iView);
        final AlertDialog imageDialog = iBuilder.create();
        exitImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDialog.dismiss();
            }
        });
        iDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Upload upload = postSnapshot.getValue(Upload.class);
                    iUploads.add(upload);
                    Log.d("upload", upload.getMImageUrl());
                }
                iAdapter = new ImagePickerAdapter(getContext(), iUploads);
                imageRecyclerView.setAdapter(iAdapter);

                iAdapter.setOnImageClickListener(new ImagePickerAdapter.onRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        customMarker.setImageUrl(iUploads.get(position).getMImageUrl());
                        customMarker.setRed(false);
                        customMarker.setGreen(false);
                        customMarker.setBlue(false);
                        customMarker.setYellow(false);
                        customMarker.setSolid(false);
                        Uri imgUri = Uri.parse(customMarker.getImageUrl());
                        Picasso.get()
                                .load(customMarker.getImageUrl())
                                .fit()
                                .centerCrop()
                                .into(imageView);
                        imageDialog.dismiss();
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        imageDialog.show();
    }
    //END IMAGES//
}
