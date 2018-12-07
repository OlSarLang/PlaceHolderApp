package com.example.left4candy.placeholderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName;

    private int picId;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference backgroundImageRef;
    private StorageReference profileImageRef;
    private StorageReference customImagesRef;

    private StorageReference thisReference;
    private ImageView thisImage;

    private ImageView changeProfileImage;
    private TextView userTextView;
    private Button buttonLogout;
    private Button mSelectBackgroundImage;
    private Button mSelectCustomImages;

    private ProgressDialog mProgressDialog;

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

        changeProfileImage = view.findViewById(R.id.changeProfileImage);

        userTextView = view.findViewById(R.id.changeableAccountName);
        userTextView.setText(user.getDisplayName());

        buttonLogout = view.findViewById(R.id.logoutPlaceholder);

        loadProfile();

        mProgressDialog = new ProgressDialog(getActivity());

        changeProfileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = profileImageRef;
                thisImage = changeProfileImage;
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
            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.show();
            Uri uri = data.getData();

            thisReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Upload Done", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                    if(thisImage == changeProfileImage) {
                        loadImage(thisReference, thisImage);
                        ((MainAdminActivity)getActivity()).loadProfile();
                    }
                    ((MainAdminActivity)getActivity()).changeOverviewBackground();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
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
