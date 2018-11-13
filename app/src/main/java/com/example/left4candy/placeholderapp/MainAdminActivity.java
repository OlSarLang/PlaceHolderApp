package com.example.left4candy.placeholderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class MainAdminActivity extends AppCompatActivity implements View.OnClickListener {

    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference imageRef;


    String downloadUri;
    private static final int GALLERY_INTENT = 2;
    private ImageView backgroundImage;
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

        mProgressDialog = new ProgressDialog(this);

        imageRef = mStorage.child("images/background.jpg");
        String backgroundUrl = imageRef.toString();

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
        }

        FirebaseUser user = mAuth.getCurrentUser();

        backgroundImage = findViewById(R.id.backgroundImage);

        userTextView = (TextView) findViewById(R.id.textViewUserEmail);
        userTextView.setText(user.getEmail());

        buttonLogout = (Button) findViewById(R.id.logoutPlaceholder);
        buttonLogout.setOnClickListener(this);

        mSelectImage = (Button) findViewById(R.id.selectImage);
        mSelectImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
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

            imageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    downloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                    Toast.makeText(MainAdminActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }
}
