package com.example.left4candy.placeholderapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OverviewFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "OverviewFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private int picId;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference customImagesRef;
    private DatabaseReference mDatabase;
    private DatabaseReference databaseMarkerRef;

    private FrameLayout myLayout;

    private List<CustomMarker> customMarkerList; //List over all loaded markers
    private CustomMarker customMarker;
    private Map<CustomMarker, ImageButton> pressableCustomMarkerList; //List of the markers shown, this is used to figure out which marker the user pressed and then gets that marker from customMarkerList
    private List<Marker> markerList; //The visible markers of the above mentioned list

    private RecyclerView recyclerView;
    private MarkerRecyclerViewAdapter recyclerViewAdapter;
    private DatabaseReference iDatabaseRef;
    private DatabaseReference userRef;
    private RecyclerView imageRecyclerView;
    private ImagePickerAdapter iAdapter;
    private List<Upload> iUploads;
    private FloatingActionButton togglePlaceMarker;
    private FloatingActionButton togglePlaceHomePos;

    private Bitmap redMarker;
    private Bitmap greenMarker;
    private Bitmap blueMarker;
    private Bitmap yellowMarker;
    private Bitmap markerMap;

    private View thisView;

    private UserInfo userInfo;

    long timeWhenDown;
    private boolean placeMarker;
    private boolean placeHomePos;

    //Maps
    private GoogleMap map;
    private MapView mMapView;
    public LatLngBounds bounds = new LatLngBounds(new LatLng(55.978793,10.336775), new LatLng(65.833435, 25.713965));
    private GeoPoint gP;


    //solid color markers
    private int height = 100;
    private int width = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FIREBASE USER INFO
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);
        userRef = FirebaseDatabase.getInstance().getReference().child("users/" + userUid + "/userinfo");

        //FIREBASE IMAGES
        customImagesRef = mStorage.child("images/custom/" + picId);

        //FIREBASE MARKERS
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users/").child(userUid);
        iDatabaseRef = mDatabase.child("images/custom/");
        databaseMarkerRef = mDatabase.child("markers/");

        customMarkerList = new ArrayList<>();
        markerList = new ArrayList<>();

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

        databaseMarkerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getDatabase(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    userInfo = dataSnapshot.getValue(UserInfo.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview_fragment, container, false);

        togglePlaceMarker = view.findViewById(R.id.togglePlaceMarker);

        togglePlaceMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(placeMarker == false){
                    togglePlaceHomePos.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondary));
                    togglePlaceMarker.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondaryDark));
                    placeHomePos = false;
                    placeMarker = true;
                }else if(placeMarker == true){
                    togglePlaceMarker.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondary));
                    placeMarker = false;
                }

            }
        });

        togglePlaceHomePos = view.findViewById(R.id.placeHomePos);
        togglePlaceHomePos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(placeHomePos == false){
                    togglePlaceMarker.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondary));
                    togglePlaceHomePos.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondaryDark));
                    placeMarker = false;
                    placeHomePos = true;
                }else if(placeHomePos == true){
                    togglePlaceHomePos.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondary));
                    placeHomePos = false;
                }
            }
        });

        myLayout = view.findViewById(R.id.rl_container);


        mMapView = view.findViewById(R.id.mapView);
        initGoogleMap(savedInstanceState);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    public void getDatabase(DataSnapshot dataSnapshot){
        myLayout.removeAllViewsInLayout();
        customMarkerList.clear();
        markerList.clear();
        map.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            CustomMarker cmMarker = new CustomMarker();
            cmMarker = ds.getValue(CustomMarker.class);
            CustomMarker.setId(cmMarker.getMarkerId());
            customMarkerList.add(cmMarker);
            loadMarker(cmMarker);
        }
    }

    public void createMarker(LatLng latLng){

        customMarker = new CustomMarker(latLng.latitude, latLng.longitude);

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
        markerImage.setImageResource(R.drawable.colorred);
        final EditText markerName = mView.findViewById(R.id.markerName);

        aBuilder.setView(mView);
        initRecyclerView(mView, customMarker);
        final AlertDialog dialog = aBuilder.create();

        radioGroup.check(R.id.radioRed);
        customMarker.setRed(true);

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
        final LatLng markerPos = new LatLng(customMarker.getLatitude(), customMarker.getLongitude());

        Marker mkr;
        if(!customMarker.isSolid()){
            Picasso.get()
                    .load(customMarker.getImageUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            markerMap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                            Marker mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(getRoundedShape(markerMap))));
                            mkr.setDraggable(true);
                            markerList.add(mkr);
                        }
                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }
        if(customMarker.isRed() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(redMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }else if(customMarker.isGreen() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(greenMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }else if(customMarker.isBlue() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(blueMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }else if(customMarker.isYellow() == true){
            mkr = map.addMarker(new MarkerOptions().position(markerPos).icon(BitmapDescriptorFactory.fromBitmap(yellowMarker)));
            mkr.setDraggable(true);
            markerList.add(mkr);
        }
    }

    public void showMarker(final CustomMarker customMarker){
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

    public void openMarkerAt(int position){
        showMarker(customMarkerList.get(position));
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
                            markerMap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                            imageView.setImageBitmap(getRoundedShape(markerMap));
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

    //Start Maps//
    private void initGoogleMap(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if(mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    public void onMapReady(GoogleMap googleMap){
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LatLng start = new LatLng(59.345317, 18.023556);
        map.addMarker(new MarkerOptions().position(start).title("Marker in Stockholm"));
        map.setLatLngBoundsForCameraTarget(bounds);
        map.setMinZoomPreference(5);
        map.moveCamera(CameraUpdateFactory.newLatLng(bounds.getCenter()));
        map.setMyLocationEnabled(true);

        map.getUiSettings().setMyLocationButtonEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(placeHomePos || placeMarker){
                    return false;
                }else {
                    showMarker(customMarkerList.get(markerList.indexOf(marker)));
                    return true;
                }
            }
        });

        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                customMarker = customMarkerList.get(markerList.indexOf(marker));
                databaseMarkerRef = mDatabase.child("markers/" + customMarker.getMarkerId());
                customMarker.setLatitude(marker.getPosition().latitude);
                customMarker.setLongitude(marker.getPosition().longitude);
                databaseMarkerRef.setValue(customMarker);
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                if(placeMarker == true){
                    createMarker(latLng);
                    togglePlaceMarker.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondary));
                    placeMarker = false;
                }
                if(placeHomePos == true){
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                    alert.setTitle(getResources().getString(R.string.NewHomePos));
                    alert.setMessage(getResources().getString(R.string.NewHomePosInfo) + "\n" + "Latitude: " + latLng.latitude + "\n" + "Longitude: " + latLng.longitude);
                    alert.setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            togglePlaceHomePos.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorSecondary));
                            userInfo.setLatitude(latLng.latitude);
                            userInfo.setLongitude(latLng.longitude);
                            userRef.setValue(userInfo);
                            placeHomePos  = false;
                        }
                    });
                    alert.setNegativeButton(getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alert.show();
                }
            }
        });
    }
    //End Maps//

    @Override
    public void onStart(){
        super.onStart();
        mMapView.onStart();
    }
    @Override
    public void onStop(){
        super.onStop();
        mMapView.onStop();
    }
    @Override
    public void onPause(){
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy(){
        mMapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
