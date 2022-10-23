package com.example.cmsfp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmsfp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.example.cmsfp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.cmsfp.utils.Constants;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {


    EditText et_email, et_password, et_confirmPassword, et_username, et_address, et_phoneNumber;
    Button btn_Register;
    TextView tv_loginBtn;

    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$";

    Pattern pat = Pattern.compile(emailRegex);

    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_confirmPassword = findViewById(R.id.et_confirmPassword);
        et_address = findViewById(R.id.et_address);
        et_phoneNumber = findViewById(R.id.et_phoneNumber);
        btn_Register = findViewById(R.id.btn_register);
        tv_loginBtn = findViewById(R.id.tv_loginButton);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        tv_loginBtn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        btn_Register.setOnClickListener(v -> PerformAuth());

    }


    private void PerformAuth() {
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();
        String confirmPassword = et_confirmPassword.getText().toString();
        String username = et_username.getText().toString();
        String address = et_address.getText().toString();
        String phoneNumber = et_phoneNumber.getText().toString();

        if (email.isEmpty()) {
            et_email.setError("Please Enter Email");
            return;
        } else if (!pat.matcher(email).matches()) {
            et_email.setError("Please Enter a valid Email");
            return;
        } else if (password.isEmpty()) {
            et_password.setError("Please input Password");
            return;
        } else if (password.length() < 6) {
            et_password.setError("Password too short");
            return;
        } else if (!confirmPassword.equals(password)) {
            et_confirmPassword.setError("Password doesn't matches");
            return;
        } else {
            progressDialog.setMessage("Creating your Account....");
            progressDialog.setTitle("Creating");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task ->{
                if (task.isSuccessful()) {
//                    Toast.makeText(RegisterActivity.this,"Hi3",Toast.LENGTH_SHORT).show();
//                    Toast.makeText(RegisterActivity.this,"Hi",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String userId = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    hashMap.put("username", username);
                    hashMap.put("email", email);
                    hashMap.put("password", password);
                    hashMap.put("address", address);
                    hashMap.put("phoneNumber", phoneNumber);

                    UserModel model = new UserModel();
                    model.setAddress(address);
                    model.setPhoneNumber(phoneNumber);
                    model.setEmail(email);
                    model.setPassword(password);
                    model.setId(userId);
                    model.setUserName(username);


                    reference.setValue(model).addOnCompleteListener(task1 -> {
//                        Toast.makeText(RegisterActivity.this,"Hi2",Toast.LENGTH_SHORT).show();
                        if (task1.isSuccessful()) {
//                            Toast.makeText(RegisterActivity.this,"Hi1",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                    });


                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
//                    sendUserToMainActivity();
                } else {
                    progressDialog.dismiss();
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

}