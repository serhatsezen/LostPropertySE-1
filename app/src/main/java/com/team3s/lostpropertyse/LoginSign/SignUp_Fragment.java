package com.team3s.lostpropertyse.LoginSign;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class SignUp_Fragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static View view;
    private static EditText fullName, emailId, username, location,
            password, confirmPassword;
    private static TextView selectLoc;
    private static ImageView profile;
    private static Button signUpButton;
    private static CheckBox terms_conditions;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private DatabaseReference current_user_db;
    private String cityname;
    private ProgressBar selectLocProgress;


    private StorageReference mStorageImage;

    String user_id;

    int PLACE_PICKER_REQUEST = 3;

    private String fullAddress;
    private String addressName;
    private LatLng addressLatLng;
    private Uri imageUri = null;


    private static final int GALLERY_REQUEST = 1;

    public SignUp_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        initViews();

        // attaching data adapter to spinner
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkValidation();
            }
        });


        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY_REQUEST);
            }
        });

        selectLoc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                selectLoc.setVisibility(View.GONE);
                selectLocProgress.setVisibility(View.VISIBLE);
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intentroadpic;

                try {
                    intentroadpic = builder.build(getActivity());
                    startActivityForResult(intentroadpic,PLACE_PICKER_REQUEST );
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });



        return view;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        cityname = item;
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // Initialize all views
    private void initViews() {
        fullName = (EditText) view.findViewById(R.id.fullName);
        emailId = (EditText) view.findViewById(R.id.userEmailId);
        username = (EditText) view.findViewById(R.id.userName);
        password = (EditText) view.findViewById(R.id.password);
        confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        signUpButton = (Button) view.findViewById(R.id.signUpBtn);
        profile = (ImageView) view.findViewById(R.id.signupProf);
        terms_conditions = (CheckBox) view.findViewById(R.id.terms_conditions);
        selectLoc = (TextView) view.findViewById(R.id.selectLoc);
        selectLocProgress = (ProgressBar) view.findViewById(R.id.selectLocProgress);
        // Setting text selector over textviews
        XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

            terms_conditions.setTextColor(csl);
        } catch (Exception e) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(requestCode==PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data,getActivity());
                fullAddress = (String) place.getAddress();
                addressName = (String) place.getName();
                addressLatLng = (LatLng) place.getLatLng();
                selectLoc.setVisibility(View.VISIBLE);
                selectLocProgress.setVisibility(View.GONE);
                selectLoc.setText(addressName);

            }
        }
    }


    private void checkValidation() {
        // Get all edittext texts
        final String getFullName = fullName.getText().toString();
        String getEmailId = emailId.getText().toString();
        final String getUserName = username.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();


        // Pattern match for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getUserName.equals("") || getUserName.length() == 0
                || getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0)

            new CustomToast().Show_Toast(getActivity(), view,
                    "Tüm Alanlar doldurulmalıdır!");

            // Check if email id valid or not
        else if (!m.find())
            new CustomToast().Show_Toast(getActivity(), view,
                    "Geçersiz Email!");

            // Check if both password should be equal
        else if (!getConfirmPassword.equals(getPassword))
            new CustomToast().Show_Toast(getActivity(), view,
                    "Şifreler aynı değil!");

            // Make sure user should check Terms and Conditions checkbox
        else if (!terms_conditions.isChecked())
            new CustomToast().Show_Toast(getActivity(), view,
                    "Şartları kabul etmelisiniz!");
        else if (imageUri==null)
            new CustomToast().Show_Toast(getActivity(), view,
                    "Profil Resmi Seçmediniz!");

        else if(fullAddress==null){
            new CustomToast().Show_Toast(getActivity(), view,
                    "Konum Seçmediniz!");
        }

        else
            auth.createUserWithEmailAndPassword(getEmailId, getConfirmPassword)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Hoşgeldiniz", Toast.LENGTH_SHORT).show();
                                user_id = auth.getCurrentUser().getUid();
                                SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
                                final String token = sharedPreferences.getString(getString(R.string.FCM_TOKEN), "");

                                mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
                                current_user_db = mDatabase.child(user_id);

                                current_user_db.child("namesurname").setValue(getFullName);
                                current_user_db.child("username").setValue(getUserName);
                                current_user_db.child("cityName").setValue(addressName);
                                current_user_db.child("fullAddress").setValue(fullAddress);
                                current_user_db.child("latLng").setValue(addressLatLng);
                                current_user_db.child("backgroundImage").setValue("https://firebasestorage.googleapis.com/v0/b/lostpro-776a5.appspot.com/o/Background_images%2F2142120171?alt=media&token=95caf486-3a0c-4dc8-947d-69385b54ffa1");
                                current_user_db.child("token").setValue(token);

                                StorageReference filepath = mStorageImage.child(imageUri.getLastPathSegment());
                                if (imageUri != null) {
                                    filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                            current_user_db.child("profileImage").setValue(downloadUri);
                                        }
                                    });
                                }
                                Intent mainIntent = new Intent(getActivity(), BottomBarActivity.class);
                                startActivity(mainIntent);
                            } else {
                                Toast.makeText(getActivity(), "Bu mail başkası tarafından kullanılmakta.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

}
