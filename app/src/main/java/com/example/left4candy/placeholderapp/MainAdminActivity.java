package com.example.left4candy.placeholderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainAdminActivity extends AppCompatActivity implements View.OnClickListener {

    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference backgroundImageRef;
    private StorageReference profileImageRef;

    private StorageReference thisReference;
    private ImageView thisImage;

    private static final int GALLERY_INTENT = 2;
    private ImageView backgroundImage;
    private ImageView profileImage;
    private TextView userTextView;
    private Button buttonLogout;
    private Button mSelectImage;

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

        backgroundImageRef = mStorage.child("images/background.jpg");
        profileImageRef = mStorage.child("images/profile.jpg");
        backgroundImage = findViewById(R.id.backgroundImage);
        profileImage = findViewById(R.id.profileImage);

        userTextView = (TextView) findViewById(R.id.textViewUserEmail);
        userTextView.setText(user.getEmail());

        buttonLogout = (Button) findViewById(R.id.logoutPlaceholder);
        buttonLogout.setOnClickListener(this);

        loadProfile();

        profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = profileImageRef;
                thisImage = profileImage;
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        mSelectImage = (Button) findViewById(R.id.selectImage);
        mSelectImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = backgroundImageRef;
                thisImage = backgroundImage;
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == buttonLogout){
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){
            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.show();
            Uri uri = data.getData();

            thisReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainAdminActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                    loadImage(thisReference, thisImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public void loadImage(StorageReference ref, final ImageView imgV){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MainAdminActivity.this).load(uri).into(imgV);
                Toast.makeText(MainAdminActivity.this, "Background Loaded", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainAdminActivity.this, "Background failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void loadProfile(){
        loadImage(backgroundImageRef, backgroundImage);
        loadImage(profileImageRef, profileImage);
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
