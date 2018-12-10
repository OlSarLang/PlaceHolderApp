package com.example.left4candy.placeholderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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

    private ProgressDialog mProgressDialog;

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
                Glide.with(MainAdminActivity.this).load(uri).into(imgV);
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

}
