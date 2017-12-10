package com.team3s.lostpropertyse.LoginSign;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.R;

import java.util.HashMap;

import static android.content.ContentValues.TAG;


public class SignIn_Fragment extends Fragment implements OnClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static View view;

    private static EditText emailid, password;
    private static Button loginButton;
    private static TextView forgotPassword, signUp;
    private static CheckBox show_hide_password;
    private static LinearLayout loginLayout;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;

    private FirebaseAuth auth;
    private DatabaseReference mDatabaseUsers,mDatabaseFindUsers,mDatabaseLostUsers;
    private DatabaseReference current_user_db;
    private Query mQueryUserTokenFind,mQueryUserTokenLost;

    public String user_id;
    public String token;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity";
    private String idToken;

    private SignInButton mSignInButton;

    public GoogleApiClient mGoogleApiClient ;
    private final Context mContext = getActivity();
    private LatLng addressLatLng;
    private String name, email,username,addressName,fulladdress;
    private String photo;
    private Uri photoUri;
    private boolean loginsign = false;

    private SharedPreferences sharedpreferences;
    private static final String PREFS = "MyPrefs";


    public SignIn_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mDatabaseFindUsers = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Bulunanlar");
        mDatabaseLostUsers = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Kayiplar");

        mSignInButton = (SignInButton) view.findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_WIDE);

        mSignInButton.setOnClickListener(this);

        sharedpreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);


        configureSignIn();                  //Google giriş ayarları

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), BottomBarActivity.class);
            startActivity(intent);
        }
        initViews();
        setListeners();
        return view;
    }

    // Google giriş ayarları
    public void configureSignIn(){
// Configure sign-in to request the user's basic profile like name and email
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();
        mGoogleApiClient.connect();
    }

    // Kullanıcının google hesabını seçmesi için gelen ekran
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    // Kullanıcının seçtiği hesabı burada çekiyoruz
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, save Token and a state then authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                idToken = account.getIdToken();

                name = account.getDisplayName();
                email = account.getEmail();
                photoUri = account.getPhotoUrl();
                photo = photoUri.toString();
                username=name.replaceAll("\\s+","");
                username.toLowerCase();
                addressName = "Türkiye";


                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuthWithGoogle(credential);
            } else {
                // Google Sign In failed, update UI appropriately
                new CustomToast().Show_Toast(getActivity(), view,
                        "Hata Oluştu!");
            }
        }
    }

    //Google hesabıyla başarılı bir şekilde işlem olduktan sonra Firebase e bu hesabı bağlıyoruz.
    private void firebaseAuthWithGoogle(AuthCredential credential){
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            task.getException().printStackTrace();
                            new CustomToast().Show_Toast(getActivity(), view,
                                    "Hata Oluştu!");
                        }else {
                            checkUser();            //daha önce bu gmail hesabıyma kayıtlı kullanıcı varmı kontrol ediyoruz.
                            loginsign = true;
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("GoogleSign", "googlesign" );
                            editor.commit();
                            Intent intent = new Intent(getActivity(), BottomBarActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });
    }

    //firebase database e kullanıcı bilgilerini kayıt ediyoruz.
    private void createUserInFirebaseHelper(){

        //Add a Listerner to that above location
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
                final String token = sharedPreferences.getString(getString(R.string.FCM_TOKEN), "");
                user_id = auth.getCurrentUser().getUid();

                double lat = 39.500;
                double lng = 30.500;

                current_user_db = mDatabaseUsers.child(user_id);

                current_user_db.child("namesurname").setValue(name);
                current_user_db.child("username").setValue(username);
                current_user_db.child("cityName").setValue(addressName);
                current_user_db.child("fullAddress").setValue(addressName);
                current_user_db.child("latLng").child("latitude").setValue(lat);
                current_user_db.child("latLng").child("longitude").setValue(lng);
                current_user_db.child("backgroundImage").setValue("https://firebasestorage.googleapis.com/v0/b/lostpro-776a5.appspot.com/o/Background_images%2F2142120171?alt=media&token=95caf486-3a0c-4dc8-947d-69385b54ffa1");
                current_user_db.child("token").setValue(token);
                current_user_db.child("profileImage").setValue(photo);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //hideProgressDialog();

            }


        });
    }

    // Initiate Views
    private void initViews() {
        fragmentManager = getActivity().getSupportFragmentManager();

        emailid = (EditText) view.findViewById(R.id.login_emailid);
        password = (EditText) view.findViewById(R.id.login_password);
        loginButton = (Button) view.findViewById(R.id.loginBtn);
        forgotPassword = (TextView) view.findViewById(R.id.forgot_password);
        show_hide_password = (CheckBox) view
                .findViewById(R.id.show_hide_password);
        loginLayout = (LinearLayout) view.findViewById(R.id.login_layout);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.shake);

        // Setting text selector over textviews
        @SuppressLint("ResourceType") XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

            forgotPassword.setTextColor(csl);
            show_hide_password.setTextColor(csl);
            signUp.setTextColor(csl);
        } catch (Exception e) {
        }
    }

    // Set Listeners
    private void setListeners() {
        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);

        // Set check listener over checkbox for showing and hiding password
        show_hide_password
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton button,
                                                 boolean isChecked) {

                        // If it is checkec then show password else hide
                        // password
                        if (isChecked) {

                            show_hide_password.setText(R.string.hide_pwd);// change
                            // checkbox
                            // text

                            password.setInputType(InputType.TYPE_CLASS_TEXT);
                            password.setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());// show password
                        } else {
                            show_hide_password.setText(R.string.show_pwd);// change
                            // checkbox
                            // text

                            password.setInputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            password.setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());// hide password

                        }

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBtn:
                checkLogin();
                break;
            case  R.id.sign_in_button:
                signIn();
                break;
            case R.id.forgot_password:

                break;
        }

    }



    private void checkUser(){

        if(auth.getCurrentUser() != null){

            final String user_id = auth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(user_id)){         //kayıt varsa bottombaractivity e
                        addNewToken();
                        Intent intent = new Intent(getActivity(), BottomBarActivity.class);
                        startActivity(intent);
                    }
                    else{                       //kayıt yoksa
                        if(loginsign == true) { //google hesapla giriş için denediyse
                            createUserInFirebaseHelper();   //database e verileri oluşturuyoruz
                        }else {
                            Intent intent = new Intent(getActivity(), TabsHeaderActivity.class);    //giriş ekranında tutuyoruz kayıt ol diyoruz
                            startActivity(intent);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void addNewToken() {
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
                token = sharedPreferences.getString(getString(R.string.FCM_TOKEN), "");
                user_id = auth.getCurrentUser().getUid();

                current_user_db = mDatabaseUsers.child(user_id);
                current_user_db.child("token").setValue(token);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();                 //postlardaki token güncellemek için
        final DatabaseReference reference = firebaseDatabase.getReference();
        mQueryUserTokenFind =  mDatabaseFindUsers.orderByChild("uid").equalTo(user_id);
        mQueryUserTokenFind.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot tasksSnapshot) {
                for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                    snapshot.getRef().child("token").setValue(token);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        mQueryUserTokenLost =  mDatabaseLostUsers.orderByChild("uid").equalTo(user_id);
        mQueryUserTokenLost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot tasksSnapshot) {
                for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                    snapshot.getRef().child("token").setValue(token);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

    }


    private void checkLogin(){
        String getEmailId = emailid.getText().toString();
        String getPassword = password.getText().toString();

        if(!TextUtils.isEmpty(getEmailId) && !TextUtils.isEmpty(getPassword)){
            auth.signInWithEmailAndPassword(getEmailId, getPassword)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                checkUser();
                            }else{
                                Toast.makeText(getActivity(), "Hata oluştu. Mail veya şifreni kontrol et ya da kayıt ol!", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
