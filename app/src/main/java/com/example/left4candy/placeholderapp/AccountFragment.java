package com.example.left4candy.placeholderapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.net.URL;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName = "Not Found";

    private int picId;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference backgroundImageRef;
    private StorageReference profileImageRef;
    private StorageReference customImagesRef;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference userNameRef;
    private DatabaseReference imageDatabaseRef;

    private UploadTask uploadTask;

    private StorageReference thisReference;
    private ImageView thisImage;
    private String thisImageType;
    private Uri thisUri;

    private ImageView changeProfileImage;
    private TextView userTextView;
    private Button buttonLogout;
    private Button mSelectBackgroundImage;
    private Button mSelectCustomImages;

    private ProgressBar progressBar;

    private static final int GALLERY_INTENT = 2;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);
        FirebaseUser user = mAuth.getCurrentUser();

        backgroundImageRef = mStorage.child("images/background.jpg");
        profileImageRef = mStorage.child("images/profile.jpg");
        customImagesRef = mStorage.child("images/custom/");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child(userUid);
        userNameRef = FirebaseDatabase.getInstance().getReference().child(userUid + "/UserName");
        imageDatabaseRef = mDatabaseRef.child("/images");

        userNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    userName = dataSnapshot.getValue(String.class);
                    userTextView.setText(userName);
                    Toast.makeText(getContext(), "Username " + userName, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        changeProfileImage = view.findViewById(R.id.changeProfileImage);

        userTextView = view.findViewById(R.id.accountName);
        userTextView.setText(userName);

        userTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                alert.setTitle("Change accountname");
                alert.setMessage("Change name here yo");
                final EditText input = new EditText(getContext());
                alert.setView(input);

                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userNameRef.setValue(input.getText().toString());
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert.show();
            }
        });

        progressBar = view.findViewById(R.id.progressBar);

        buttonLogout = view.findViewById(R.id.logoutPlaceholder);

        loadProfile();


        changeProfileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = profileImageRef;
                thisImage = changeProfileImage;
                thisImageType = "profile";
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        mSelectBackgroundImage = view.findViewById(R.id.mSelectBackgroundImage);
        mSelectBackgroundImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = backgroundImageRef;
                thisImageType = "background";
                //TODO Samma som nedanför
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        mSelectCustomImages = view.findViewById(R.id.mSelectCustomImages);
        mSelectCustomImages.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = customImagesRef;
                thisImageType = "custom";
                //TODO Kolla om thisImage behövs, ev. felhantering
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == getActivity().RESULT_OK){
            thisUri = data.getData();
            uploadFile();
        }
    }


    private String getFileExtension(Uri uri){
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
            final StorageReference fileReference;
            if(thisReference == profileImageRef) {
                fileReference = thisReference;
            }else if(thisReference == backgroundImageRef){
                fileReference = thisReference;
            }else{
                fileReference = thisReference.child(System.currentTimeMillis() + "." + getFileExtension(thisUri));
            }

            uploadTask = fileReference.putFile(thisUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri downloadUri = task.getResult();
                            String thisUri = downloadUri.toString();
                            Upload upload = new Upload(thisUri.toString());
                            String uploadId = imageDatabaseRef.push().getKey();
                            if(thisImageType == "profile" || thisImageType == "background"){
                                imageDatabaseRef.child(thisImageType +"/").setValue(upload);
                            }else{
                                imageDatabaseRef.child(thisImageType +"/" +  uploadId).setValue(upload);
                            }
                            loadProfile();
                            ((MainAdminActivity)getActivity()).loadProfile();
                            ((MainAdminActivity)getActivity()).changeOverviewBackground();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
    }

    public void onClick(View v) {
        if(v == buttonLogout){
            mAuth.signOut();
            //finish();????
            startActivity(new Intent(getContext(), LoginActivity.class));
        }
    }

    public void loadProfile(){
        loadImage(profileImageRef, changeProfileImage);
    }

    public void loadImage(StorageReference ref, final ImageView imgV){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(AccountFragment.this).load(uri).into(imgV);
                Toast.makeText(getContext(), "Background Loaded", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Background failed", Toast.LENGTH_LONG).show();
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
