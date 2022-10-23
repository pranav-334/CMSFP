package com.example.cmsfp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmsfp.adapter.ShowAllComplaintAdapter;
import com.example.cmsfp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.example.cmsfp.adapter.ShowAllComplaintAdapter;
import com.example.cmsfp.model.DepartmentUser;
import com.example.cmsfp.model.Complaint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cmsfp.model.UserModel;
import com.example.cmsfp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DepartmentDashboard extends AppCompatActivity {


    DepartmentUser model = null;
    private String selectedDep;
    private ProgressDialog progressDialog;

    List<Complaint> mList = new ArrayList<>();
    RecyclerView rv;
    ShowAllComplaintAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_dashboard);

        rv = findViewById(R.id.rv_showAllFood);
        selectedDep = getIntent().getStringExtra("selectedDep");
        rv.setHasFixedSize(true);
        FirebaseApp.initializeApp(this);

        rv.setLayoutManager(new LinearLayoutManager(DepartmentDashboard.this));
        progressDialog = new ProgressDialog(DepartmentDashboard.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        model = null;
        getUser();
    }

    public void getUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DEPARTMENT).child(selectedDep).child(firebaseUser.getUid());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    model = snapshot.getValue(DepartmentUser.class);
                    getAllFood(model.getDepartment());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("error", error.getMessage());
                }
            });
        }
    }

    private void getAllFood(String depName) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(depName).child(Constants.OCMS_USER);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        for(DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                            Complaint model = childSnapshot.getValue(Complaint.class);
                            mList.add(model);
                        }
                    }
                    adapter = new ShowAllComplaintAdapter(DepartmentDashboard.this, (ArrayList<Complaint>) mList, true);
                    rv.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}