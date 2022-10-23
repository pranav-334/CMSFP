package com.example.cmsfp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmsfp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cmsfp.model.Complaint;
import com.example.cmsfp.utils.Constants;

public class OpenComplaintActivity extends AppCompatActivity {

    Button btnMarkAsInProgress, btnSave, btnMarkAsCompleted;
    EditText reply;
    TextView complaintDesc, complaintStatus;
    String userId, complaintParentKey = "", department, complaintId;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_complaint);
        init();
        progressDialog.show();
        btnMarkAsInProgress.setOnClickListener(v -> {
            complaintStatus.setText("Status: In Progress");
        });

        btnMarkAsCompleted.setOnClickListener(v -> {
            complaintStatus.setText("Status: Completed");
        });

        btnSave.setOnClickListener(v -> {
            updateComplaintStatus();
        });
    }

    private void updateComplaintStatus() {
        progressDialog.show();
        String rep;
        if (reply.getText().toString().equals("Pending")) rep = "Pending"; else rep = reply.getText().toString();
        FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(department)
                .child(Constants.OCMS_USER).child(userId).child(complaintParentKey)
                .child("status").setValue(complaintStatus.getText().toString().split(": ")[1]).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            updateReplyStatus(rep);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OpenComplaintActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateReplyStatus(String rep) {
        FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(department)
                .child(Constants.OCMS_USER).child(userId).child(complaintParentKey)
                .child("reply").setValue(rep).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(OpenComplaintActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OpenComplaintActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        btnMarkAsInProgress = findViewById(R.id.btn_markasInProgress);
        btnMarkAsCompleted = findViewById(R.id.btn_mark_as_completed);
        btnSave = findViewById(R.id.btn_save);
        complaintDesc = findViewById(R.id.tv_complaintDesc);
        complaintStatus = findViewById(R.id.tv_complaintStatus);
        reply = findViewById(R.id.btn_reply);

        userId = getIntent().getStringExtra("userId");
        department = getIntent().getStringExtra("dept");
        complaintId = getIntent().getStringExtra("complaintId");

        getAllFood(department, userId, complaintId);
    }

    private void getAllFood(String depName, String userId, String complaintId) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(depName).child(Constants.OCMS_USER).child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isItemFound = false;
                    Complaint model = null;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(complaintParentKey.equals(""))
                            complaintParentKey = dataSnapshot.getKey();
                        model = dataSnapshot.getValue(Complaint.class);
                        String storedId = model.getComplaintId();

                        if (complaintId.equals(storedId)) {
                            isItemFound = true;
                        }
                        if (isItemFound) break;
                    }
                    if (model != null) updateLayout(model);
                    progressDialog.dismiss();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void updateLayout(Complaint complaint) {
        complaintDesc.setText(complaint.getDescription());
        reply.setText(complaint.getReply());
        complaintStatus.setText("Status: " + complaint.getStatus());    }

}