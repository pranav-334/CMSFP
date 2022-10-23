package com.example.cmsfp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmsfp.R;
//import com.example.ocms.R;
public class Dashboard extends AppCompatActivity {

    Button btn_user, btn_department;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btn_user = findViewById(R.id.btn_user);
        btn_department = findViewById(R.id.btn_department);

        btn_user.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, RegisterActivity.class)));
        btn_department.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, DepartmentRegister.class)));

    }
}