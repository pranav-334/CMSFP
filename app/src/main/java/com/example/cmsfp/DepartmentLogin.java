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
import com.example.cmsfp.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth. FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cmsfp.model.UserModel;
import com.example.cmsfp.utils.Constants;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class DepartmentLogin extends AppCompatActivity {

    EditText et_email, et_password;
    Button btn_login;
    TextView tv_registerBtn;

    TextView tv_forgotPassword;
    ArrayList<String> listOfUsers = new ArrayList<>();


    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$";

    Pattern pat = Pattern.compile(emailRegex);

    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    String[] dep = {Constants.GARBAGE, Constants.WATER, Constants.ROADSANDLIGHTS, Constants.ELECTRICITY, Constants.PESTS, Constants.PARKMAINTAINANCE};
    private Spinner departmentSpin;
    private ArrayAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_login);

        departmentSpin = findViewById(R.id.sp_departments);

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(com.example.cmsfp.R.id.et_password);
        btn_login = findViewById(com.example.cmsfp.R.id.btn_login);
        tv_registerBtn = findViewById(com.example.cmsfp.R.id.tv_registerButton);
        tv_forgotPassword = findViewById(R.id.tv_forgotPassword);

        tv_forgotPassword.setOnClickListener(v -> startActivity(new Intent(DepartmentLogin.this, ResetPasswordActivity.class)));

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        btn_login.setOnClickListener(v -> performLogin());

        tv_registerBtn.setOnClickListener(v -> startActivity(new Intent(DepartmentLogin.this, DepartmentRegister.class)));
        initializeSpinner();
    }

    private void initializeSpinner() {
        departmentSpin = findViewById(R.id.sp_departments);
        spinnerAdapter = new ArrayAdapter(DepartmentLogin.this, android.R.layout.simple_spinner_item, dep);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpin.setAdapter(spinnerAdapter);
        departmentSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void performLogin() {
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

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
        } else {
            progressDialog.setMessage("Login in to your Account....");
            progressDialog.setTitle("Loading");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    getAllDepartmentUsers();
                } else {
                    progressDialog.dismiss();
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DepartmentLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    public void getAllDepartmentUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DEPARTMENT).child(departmentSpin.getSelectedItem().toString());//.child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listOfUsers.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        if(firebaseUser.getUid().equals(dataSnapshot.getValue(UserModel.class).getId())){
                            listOfUsers.add("User Exits");
                        }
                    }
                    if(listOfUsers.size() > 0) {
                        sendUserToMainActivity();
                        Toast.makeText(DepartmentLogin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DepartmentLogin.this, "Please Register yourself first", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DepartmentLogin.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(DepartmentLogin.this, DepartmentDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("selectedDep", departmentSpin.getSelectedItem().toString());
        startActivity(intent);

    }

}