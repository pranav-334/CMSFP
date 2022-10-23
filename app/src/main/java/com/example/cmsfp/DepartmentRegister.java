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
//import com.example.ocms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.example.cmsfp.model.DepartmentUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.cmsfp.utils.Constants;

import java.util.HashMap;
import java.util.regex.Pattern;

public class DepartmentRegister extends AppCompatActivity {

    EditText et_email, et_password, et_confirmPassword, et_username, et_phoneNumber;
    Button btn_Register;
    TextView tv_loginBtn;
    String[] dep = {Constants.GARBAGE, Constants.WATER, Constants.ROADSANDLIGHTS, Constants.ELECTRICITY, Constants.PESTS, Constants.PARKMAINTAINANCE};
    private Spinner departmentSpin;
    private ArrayAdapter spinnerAdapter;


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
        setContentView(R.layout.activity_department_register);

        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        departmentSpin = findViewById(R.id.sp_departments);

        et_password = findViewById(R.id.et_password);
        et_confirmPassword = findViewById(R.id.et_confirmPassword);
        et_phoneNumber = findViewById(R.id.et_phoneNumber);
        btn_Register = findViewById(R.id.btn_register);
        tv_loginBtn = findViewById(R.id.tv_loginButton);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        tv_loginBtn.setOnClickListener(v -> startActivity(new Intent(DepartmentRegister.this, DepartmentLogin.class)));

        btn_Register.setOnClickListener(v -> PerformAuth());
        initializeSpinner();
    }

    private void initializeSpinner() {
        departmentSpin = findViewById(R.id.sp_departments);
        spinnerAdapter = new ArrayAdapter(DepartmentRegister.this, android.R.layout.simple_spinner_item, dep);
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

    private void PerformAuth() {
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();
        String confirmPassword = et_confirmPassword.getText().toString();
        String username = et_username.getText().toString();
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

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String userId = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference().child(Constants.DEPARTMENT).child(departmentSpin.getSelectedItem().toString()).child(userId);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userId);
                        hashMap.put("username", username);
                        hashMap.put("email", email);
                        hashMap.put("password", password);
                        hashMap.put("department", departmentSpin.getSelectedItem().toString());
                        hashMap.put("phoneNumber", phoneNumber);

                    DepartmentUser model = new DepartmentUser();
                    model.setPhoneNumber(phoneNumber);
                    model.setEmail(email);
                    model.setPassword(password);
                    model.setId(userId);
                    model.setUserName(username);
                    model.setDepartment(departmentSpin.getSelectedItem().toString());

                    reference.setValue(model).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            sendUserToMainActivity();
                        }
                    });
                    Toast.makeText(DepartmentRegister.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(DepartmentRegister.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(DepartmentRegister.this, DepartmentDashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("selectedDep", departmentSpin.getSelectedItem().toString());
        startActivity(intent);

    }

}