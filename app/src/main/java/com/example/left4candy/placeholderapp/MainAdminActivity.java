package com.example.left4candy.placeholderapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MainAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private boolean mLocationPermissionGranted = false;


    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName = "Not found";

    private OverviewFragment overviewFragment = new OverviewFragment();
    private AccountFragment accountFragment = new AccountFragment();
    private com.example.left4candy.placeholderapp.ListFragment listFragment = new com.example.left4candy.placeholderapp.ListFragment();

    private static final String TAG ="MainAdminActivity";
    private SectionsPageAdapter mSectionsPageAdapter;
    private CustomViewPager mViewPager;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference profileImageRef;
    private DatabaseReference userRef;

    private ImageView profileImage;
    private TextView userTextView;
    private Bitmap profileBitmap;

    public static UserInfo userInfo;

    int width = 200;
    int height = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        userInfo = new UserInfo();

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
        }
        final FirebaseUser user = mAuth.getCurrentUser();


        profileImageRef = mStorage.child("images/profile.jpg");
        profileImage = findViewById(R.id.profileImage);
        userTextView = (TextView) findViewById(R.id.textViewUserEmail);

        String registrationToken = FirebaseInstanceId.getInstance().getToken();

        userRef = FirebaseDatabase.getInstance().getReference().child("users/").child(userUid + "/userinfo");

        loadProfile();

        userTextView.setText(userInfo.getUserName());

        mViewPager = (CustomViewPager) findViewById(R.id.view_pager_container);
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
               //getChatrooms
            }else{
                getLocationPermission();
            }
        }
    }


    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(listFragment, "List", mViewPager);
        adapter.addFragment(overviewFragment, "Overview", mViewPager);
        adapter.addFragment(accountFragment, "Account", mViewPager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
    }

    @Override
    public void onClick(View v) {

    }

    public void loadImage(StorageReference ref, final ImageView imgV){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        profileBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        imgV.setImageBitmap(getRoundedShape(profileBitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                Toast.makeText(MainAdminActivity.this, "Profile picture loaded", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainAdminActivity.this, "Profile picture failed to load", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void loadProfile(){
        loadImage(profileImageRef, profileImage);
        getUser();
    }

    @GlideModule
    public class MyAppGlideModule extends AppGlideModule {

        @Override
        public void registerComponents(Context context, Glide glide, Registry registry) {
            // Register FirebaseImageLoader to handle StorageReference
            registry.append(StorageReference.class, MainAdminActivity.class,
                    (ResourceDecoder<StorageReference, MainAdminActivity>) new FirebaseImageLoader.Factory());
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

    public void logOut(){
        Log.d("MainActivity", "Logout");
        mAuth.signOut();
        Log.d("MainActivity", "after signout");
        //finish();????
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void itemClickedFragmentSwitch(int position){
        mViewPager.setCurrentItem(1);
        overviewFragment.openMarkerAt(position);
    }

    //MAPS PERMISSION
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getChatrooms();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    //getChatrooms();
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }

    public void showUserName(){
        userTextView.setText(userInfo.getUserName());
        Log.d(TAG, userInfo.getUserName() + userInfo.getLatitude() + userInfo.getLongitude());
    }

    public void getUser(){
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userInfo = dataSnapshot.getValue(UserInfo.class);
                    showUserName();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
