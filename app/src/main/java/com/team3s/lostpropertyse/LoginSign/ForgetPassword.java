package com.team3s.lostpropertyse.LoginSign;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.team3s.lostpropertyse.R;

public class ForgetPassword extends AppCompatActivity {
    private FirebaseAuth auth;

    private Button newPass;
    private EditText email;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forget_password);
        //FirebaseAuth sınıfının referans olduğu nesneleri kullanabilmek için getInstance methodunu kullanıyoruz.
        auth = FirebaseAuth.getInstance();
        newPass = (Button) findViewById(R.id.newPass);
        email = (EditText) findViewById(R.id.uyeEmail);

        newPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newPass.setText("Lütfen bekleyiniz!");
                newPass.setEnabled(false);

                String mail = email.getText().toString().trim();

                if (TextUtils.isEmpty(mail)) {
                    Toast.makeText(getApplication(), "Lütfen email adresinizi giriniz", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(mail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(ForgetPassword.this, TabsHeaderActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(ForgetPassword.this, "Yeni parola için gerekli bağlantı adresinize gönderildi!", Toast.LENGTH_SHORT).show();
                                } else {
                                    newPass.setText("Yeni Parola Bağlantısını Gönder!");
                                    newPass.setEnabled(true);
                                    Toast.makeText(ForgetPassword.this, "Mail gönderme hatası!", Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
            }
        });
    }
}
