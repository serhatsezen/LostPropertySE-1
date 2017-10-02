package com.team3s.lostpropertyse.LoginSign;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
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
import android.widget.Spinner;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class SignUp_Fragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static View view;
    private static EditText fullName, emailId, username, location,
            password, confirmPassword;
    private static ImageView profile;
    private static Button signUpButton;
    private static CheckBox terms_conditions;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private DatabaseReference current_user_db;
    private String cityname;

    private Uri mUserProf = null;

    private StorageReference mStorageImage;

    String user_id;

    private static final int GALLERY_REQUEST = 3;
    //String app_server_url="http://aydinserhatsezen.com/fcm/fcm_haberler_insert.php";

    public SignUp_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Spinner spinner = (Spinner) view.findViewById(R.id.location);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("Adana");        categories.add("Adıyaman");        categories.add("Afyonkarahisar");        categories.add("Ağrı");        categories.add("Amasya");        categories.add("Ankara");
        categories.add("Antalya");        categories.add("Artvin");        categories.add("Aydın");        categories.add("Balıkesir");        categories.add("Bilecik");        categories.add("Bingöl");
        categories.add("Bitlis");        categories.add("Bolu");        categories.add("Burdur");        categories.add("Bursa");        categories.add("Çanakkale");        categories.add("Çankırı");        categories.add("Çorum");
        categories.add("Denizli");        categories.add("Diyarbakır");        categories.add("Edirne");        categories.add("Elazığ");        categories.add("Erzincan");        categories.add("Erzurum");
        categories.add("Eskişehir");        categories.add("Gaziantep");        categories.add("Giresun");        categories.add("Gümüşhane");        categories.add("Hakkâri");        categories.add("Hatay");
        categories.add("Isparta");        categories.add("Mersin");        categories.add("İstanbul");        categories.add("İzmir");        categories.add("Kars");        categories.add("Kastamonu");
        categories.add("Kayseri");        categories.add("Kırklareli");        categories.add("Kırşehir");        categories.add("Genel");        categories.add("Kocaeli");        categories.add("Konya");
        categories.add("Kütahya");        categories.add("Malatya");        categories.add("Manisa");        categories.add("Kahramanmaraş");        categories.add("Mardin");        categories.add("Muğla");
        categories.add("Muş");        categories.add("Nevşehir");        categories.add("Niğde");        categories.add("Ordu");        categories.add("Rize");        categories.add("Sakarya");        categories.add("Samsun");
        categories.add("Siirt");        categories.add("Sinop");        categories.add("Sivas");        categories.add("Tekirdağ");        categories.add("Tokat");        categories.add("Trabzon");
        categories.add("Tunceli");        categories.add("Şanlıurfa");        categories.add("Uşak");        categories.add("Van");        categories.add("Yozgat");        categories.add("Zonguldak");
        categories.add("Aksaray");        categories.add("Bayburt");        categories.add("Karaman");        categories.add("Kırıkkale");        categories.add("Batman");        categories.add("Şırnak");
        categories.add("Bartın");        categories.add("Ardahan");        categories.add("Iğdır");        categories.add("Yalova");        categories.add("Karabük");        categories.add("Kilis");
        categories.add("Osmaniye");        categories.add("Düzce");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        initViews();

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
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
            mUserProf = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            android.database.Cursor cursor =getActivity().getContentResolver().query(mUserProf,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) view.findViewById(R.id.signupProf);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }


    private void checkValidation() {
        // Get all edittext texts
        final String getFullName = fullName.getText().toString();
        String getEmailId = emailId.getText().toString();
        final String getUserName = username.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();
        final String city_val = cityname.toString().trim();


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
        else if (mUserProf==null)
            new CustomToast().Show_Toast(getActivity(), view,
                    "Profil Resmi Seçmediniz!");

        else
            auth.createUserWithEmailAndPassword(getEmailId, getConfirmPassword)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Hoşgeldiniz", Toast.LENGTH_SHORT).show();
                                user_id = auth.getCurrentUser().getUid();
                                mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
                                current_user_db = mDatabase.child(user_id);

                                current_user_db.child("namesurname").setValue(getFullName);
                                current_user_db.child("username").setValue(getUserName);
                                current_user_db.child("city").setValue(city_val);
                                // current_user_db.child("token").setValue(token);

                                StorageReference filepath = mStorageImage.child(mUserProf.getLastPathSegment());
                                if (mUserProf != null) {
                                    filepath.putFile(mUserProf).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                            current_user_db.child("profileImage").setValue(downloadUri);
                                        }
                                    });
                                }
                                Intent mainIntent = new Intent(getActivity(), BottomBarActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainIntent);
                            } else {
                                Toast.makeText(getActivity(), "Bu mail başkası tarafından kullanılmakta.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

}
