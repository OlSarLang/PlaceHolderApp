package com.example.left4candy.placeholderapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.net.URL;

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName = "Not Found";

    private int picId;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private StorageReference profileImageRef;
    private StorageReference customImagesRef;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference userRef;
    private DatabaseReference imageDatabaseRef;

    private UploadTask uploadTask;

    private StorageReference thisReference;
    private String thisImageType;
    private Uri thisUri;

    private ImageView changeProfileImage;
    private Bitmap profileBitmap;
    private TextView userTextView;
    private Button buttonLogout;
    private Button mSelectProfileImage;
    private Button mSelectCustomImages;
    private CheckBox checkPrivate;

    private TextView changeEmail;
    private TextView changeNumber;
    private TextView emailView;
    private TextView numberView;

    private Boolean privacy = true;

    public UserInfo userInfo = new UserInfo();

    private ProgressBar progressBar;

    private static final int GALLERY_INTENT = 2;

    int width = 200;
    int height = 200;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child(userUid);
        final FirebaseUser user = mAuth.getCurrentUser();

        profileImageRef = mStorage.child("images/profile.jpg");
        customImagesRef = mStorage.child("images/custom/");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users/" + userUid);
        userRef = FirebaseDatabase.getInstance().getReference().child("users/" + userUid + "/userinfo");
        imageDatabaseRef = mDatabaseRef.child("/images");

        checkPrivate = view.findViewById(R.id.checkPrivate);
        userTextView = view.findViewById(R.id.accountName);

        changeEmail = view.findViewById(R.id.changeEmail);
        changeNumber = view.findViewById(R.id.changeNr);
        emailView = view.findViewById(R.id.emailAccountTextView);
        numberView = view.findViewById(R.id.phoneTextView);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    userInfo = dataSnapshot.getValue(UserInfo.class);
                    userTextView.setText(userInfo.getUserName());
                    checkPrivate.setChecked(userInfo.isPrivacy());
                    numberView.setText(userInfo.getPhoneNumber());
                    emailView.setText(userInfo.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        changeProfileImage = view.findViewById(R.id.changeProfileImage);

        userTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                alert.setTitle(getResources().getString(R.string.ChangeName));
                alert.setMessage(getResources().getString(R.string.ChangeNameBelow));
                final EditText input = new EditText(getContext());
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
                alert.setView(input);

                alert.setPositiveButton(getResources().getString(R.string.Save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userInfo.setUserName(input.getText().toString());
                        userRef.setValue(userInfo);
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert.show();
            }
        });

        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                alert.setTitle(getResources().getString(R.string.ChangeNumber));
                alert.setMessage(getResources().getString(R.string.ChangeNumberBelow));
                final EditText input = new EditText(getContext());
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
                alert.setView(input);

                alert.setPositiveButton(getResources().getString(R.string.Save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userInfo.setPhoneNumber(input.getText().toString());
                        userRef.setValue(userInfo);
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert.show();
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                alert.setTitle(getResources().getString(R.string.ChangeEmail));
                alert.setMessage(getResources().getString(R.string.ChangeEmailBelow));
                final EditText input = new EditText(getContext());
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
                alert.setView(input);

                alert.setPositiveButton(getResources().getString(R.string.Save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userInfo.setEmail(input.getText().toString());
                        userRef.setValue(userInfo);
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert.show();
            }
        });

        checkPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    userInfo.setPrivacy(isChecked);
                    userRef.setValue(userInfo);
                }else if(!isChecked){
                    userInfo.setPrivacy(isChecked);
                    userRef.setValue(userInfo);
                }
            }
        });

        progressBar = view.findViewById(R.id.progressBar);

        buttonLogout = view.findViewById(R.id.logoutPlaceholder);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AccountFragment", "Logout");
                ((MainAdminActivity)getActivity()).logOut();
            }
        });

        loadProfile();

        mSelectProfileImage = view.findViewById(R.id.mSelectProfileImage);
        mSelectProfileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                thisReference = profileImageRef;
                thisImageType = "profile";
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
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
    }

    public void loadProfile(){
        loadImage(profileImageRef, changeProfileImage);
    }

    public void loadImage(StorageReference ref, final ImageView imgV){
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        profileBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        changeProfileImage.setImageBitmap(getRoundedShape(profileBitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
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

    public class MyAppGlideModule extends AppGlideModule {

        @Override
        public void registerComponents(Context context, Glide glide, Registry registry) {
            // Register FirebaseImageLoader to handle StorageReference
            registry.append(StorageReference.class, MainAdminActivity.class,
                    (ResourceDecoder<StorageReference, MainAdminActivity>) new FirebaseImageLoader.Factory());
        }
    }
}
