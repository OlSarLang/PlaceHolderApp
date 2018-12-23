package com.example.left4candy.placeholderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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

    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName = "Not found";

    private OverviewFragment overviewFragment = new OverviewFragment();
    private AccountFragment accountFragment = new AccountFragment();
    private ListFragment listFragment = new ListFragment();

    private static final String TAG ="MainAdminActivity";
    private SectionsPageAdapter mSectionsPageAdapter;
    private CustomViewPager mViewPager;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference profileImageRef;
    private DatabaseReference userNameRef;

    private ImageView profileImage;
    private TextView userTextView;
    private Bitmap profileBitmap;

    private ProgressDialog mProgressDialog;

    int width = 200;
    int height = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
        }
        FirebaseUser user = mAuth.getCurrentUser();

        mProgressDialog = new ProgressDialog(this);

        profileImageRef = mStorage.child("images/profile.jpg");
        profileImage = findViewById(R.id.profileImage);
        userTextView = (TextView) findViewById(R.id.textViewUserEmail);
        userTextView.setText(userName);

        String registrationToken = FirebaseInstanceId.getInstance().getToken();

        userNameRef = FirebaseDatabase.getInstance().getReference().child(userUid + "/UserName");

        userNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    userName = dataSnapshot.getValue(String.class);
                    userTextView.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        loadProfile();

        mViewPager = (CustomViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(listFragment, "List");
        adapter.addFragment(overviewFragment, "Overview");
        adapter.addFragment(accountFragment, "Account");
        viewPager.setAdapter(adapter);
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
                        profileImage.setImageBitmap(getRoundedShape(profileBitmap));
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
    }

    public void changeOverviewBackground() {
        overviewFragment.loadProfile();
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
        mAuth.signOut();
        Log.d("LOGOUT!!!", "should log out");
        //finish();????
        startActivity(new Intent(this, LoginActivity.class));
    }

}
