package com.team3s.lostpropertyse.Chat;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.AdapterClass;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.CircleTransform;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.internal.zzagr.runOnUiThread;

public class UsersDMList extends Fragment {
    private EditText cevap;
    private ImageButton cevaponay;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private DatabaseReference database;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseAnswer;
    private DatabaseReference mDatabasePost;
    private DatabaseReference mDatabasePToken;

    private ListView cevapList;

    private String post_key = null;
    private String post_type = null;

    private String tokenUser = null;
    private String nameFuser = null;
    private String mUID = null;
    private String currentUID = null;

    public String user_key = null;
    public String cevap_val;

    TextView noUsersText;
    ArrayList<String> al = new ArrayList<>();
    int totalUsers = 0;
    ProgressDialog pd;
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences mPrefs;
    DatabaseReference reference1,reference2,userDB;
    Query dmRefQuery;

    private String receiver_name;
    private String senderName;
    private String currentUid;
    private String receiveruid;
    private String senderuid;
    private String usernameSender;
    private String usernameReceiver;
    private String useruidkey;
    private String currentUserId;
    private String profileImage;

    UsersAdapter usersAdapter;
    final ArrayList<Users> usersInformation = new ArrayList<Users>();
    final ArrayList<String> usersnames = new ArrayList<String>();
    private Runnable run;

    public UsersDMList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_users, container, false);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        currentUserId = auth.getCurrentUser().getUid();


        cevapList = (ListView) v.findViewById(R.id.usersList);

        mPrefs = getActivity().getSharedPreferences(MyPREFERENCES,0);
        senderName = mPrefs.getString("username", "");
        receiver_name = mPrefs.getString("receiver_name","");
        currentUid = mPrefs.getString("USERKEY_SHARED","");
        userDB = FirebaseDatabase.getInstance().getReference().child("Users");

        return v;

    }

    @Override
    public void onResume() {
        super.onResume();

        listUsers();

    }

    public void listUsers() {                                                       //ListView e kullanıcının mesajlaştığı kişileri ekliyoruz
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference();
        DatabaseReference node = mDatabaseReference.child("messages");
        node.orderByChild("senderUid").addValueEventListener(new ValueEventListener() {                //Burada kullanıcının uid sine göre messages node unda sıralama yapıyoruz
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    try {                                                                              //kullanıcının uid sinin bulundugu childların içindeki mesajı alan ve gönderini uid sini çekiyoruz
                        receiveruid = data.child("receiver_uid").getValue().toString();
                        senderuid = data.child("senderUid").getValue().toString();
                        if (senderuid.equals(currentUid)) {                                             // eğer child daki senderUid ile şuan ki kullanıcının uid si aynı ise
                            userDB.child(receiveruid).addValueEventListener(new ValueEventListener() {  // mesajlaşılan kişinin bilgilerini user node unun içinden çekeceğiz
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    usernameReceiver = String.valueOf(dataSnapshot.child("username").getValue());
                                    usernameReceiver = usernameReceiver.toLowerCase();
                                    usernameReceiver = usernameReceiver.replaceAll("\\s+","");     //username i alıyoruz, boşlukları silip büyük harfleri küçük yapıyoruz
                                    useruidkey = dataSnapshot.getKey();                                              //userUID sini alıyoruz
                                    profileImage = String.valueOf(dataSnapshot.child("profileImage").getValue());    //profil resmini de alıyoruz

                                    if (usersnames.contains(usernameReceiver)) {
                                    } else {
                                        usersInformation.add(new Users(usernameReceiver, useruidkey, profileImage));     //custom listview e bu bilgileri koyuyoruz,
                                        usersnames.add(usernameReceiver);
                                        usersAdapter = new UsersAdapter(getActivity(), usersInformation);
                                        cevapList.setAdapter(usersAdapter);
                                    }

                                    cevapList.setOnItemClickListener(new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view,
                                                                int position, long id) {
                                            TextView textViewUid = (TextView) view.findViewById(R.id.list_row_textview_sure);
                                            String textuid = textViewUid.getText().toString();
                                            TextView textViewName = (TextView) view.findViewById(R.id.list_row_textview_isim);
                                            String textuidname = textViewName.getText().toString();

                                            SharedPreferences.Editor editor = mPrefs.edit();
                                            editor.putString("receiver_name", textuidname);
                                            editor.commit();
                                            Intent senddmUser = new Intent(getActivity(), Chat.class);
                                            senddmUser.putExtra("sender_uid",currentUserId);
                                            senddmUser.putExtra("receiver_uid",textuid);
                                            startActivity(senddmUser);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}