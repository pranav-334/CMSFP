package com.example.cmsfp.fragment;

import static android.app.Activity.RESULT_OK;

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
import android.view.ViewGroup;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmsfp.R;
import com.example.cmsfp.RegisterActivity;
import com.example.cmsfp.adapter.ShowAllComplaintAdapter;
import com.example.cmsfp.model.UserModel;
import com.example.cmsfp.model.Complaint;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.example.cmsfp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NewComplaintFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public NewComplaintFragment() {}

    public static NewComplaintFragment newInstance(String param1, String param2) {
        NewComplaintFragment fragment = new NewComplaintFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    String[] courses = {"Garbage", "Water", "Road"};

    private FloatingActionButton fab_createFood;

    private ProgressDialog progressDialog;

    private static final int STORAGE_PERMISSION_CODE = 100;
    StorageReference storageReference;
    public static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String imageString;
    private StorageTask uploadTask;


    UserModel model = null;

    String department = "";
    ImageView iv_complaintImage;

    List<Complaint> mList = new ArrayList<>();
    RecyclerView rv;
    ShowAllComplaintAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_complaint, container, false);

        rv = view.findViewById(R.id.rv_showAllFood);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        fab_createFood = view.findViewById(R.id.fab_createFood);
        fab_createFood.setOnClickListener(v -> createDialog());

        return view;
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alert_add_complaint, null, false);
        builder.setView(view);

        EditText et_description = view.findViewById(R.id.et_description);
        iv_complaintImage = view.findViewById(R.id.iv_complaintImage);
        Spinner spino = view.findViewById(R.id.sp_department);
        Button btn_create = view.findViewById(R.id.fab_createFood);
        spino.setOnItemSelectedListener(this);

        ArrayAdapter ad = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, courses);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spino.setAdapter(ad);

        AlertDialog alertDialog = builder.create();

        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");

        iv_complaintImage.setOnClickListener(v -> checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE));
        alertDialog.show();
        btn_create.setOnClickListener(v -> {
            String complaintDescription = et_description.getText().toString();
            String foodImage = imageString;
            if (complaintDescription.isEmpty()) {
                et_description.setError("Empty description");
            } else {
                progressDialog.setMessage("Adding Your Complaint");
                progressDialog.setTitle("Adding...");
                progressDialog.setCanceledOnTouchOutside(false);
                createFood(complaintDescription, department, foodImage);
                alertDialog.dismiss();
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading...");
        pd.show();
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    try {
                        Uri downloadingUri = task.getResult();
                        Log.d("TAG", "onComplete: uri completed");
                        String mUri = downloadingUri.toString();
                        imageString = mUri;
                        Glide.with(getContext()).load(imageUri).into(iv_complaintImage);
                    } catch (Exception e) {
                        Log.d("TAG1", "error Message: " + e.getMessage());
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    pd.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed here", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(getContext(), "No image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    public void checkPermission(String permission, int requestCode) {

        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        } else {
            openImage();
            Toast.makeText(getContext(), "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImage();
                Toast.makeText(getContext(), "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createFood(String complaintDescription, String department, String foodImage) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(model.getUserName());

        Complaint complaint = new Complaint();
        complaint.setDepartment(department);
        complaint.setId(model.getId());
        complaint.setUserName(model.getUserName());
        complaint.setStatus("pending");
        complaint.setReply("pending");
        complaint.setDescription(complaintDescription);
        complaint.setImage(imageString);

        reference.push().setValue(complaint).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                getAllFood();
                Toast.makeText(getContext(), "Complaint Added Successfully", Toast.LENGTH_SHORT).show();
                imageString = "";
            } else {
                Toast.makeText(getContext(), "Complaint Created Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    model = snapshot.getValue(UserModel.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        department = courses[position];
        Toast.makeText(getContext(), courses[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getAllFood() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(model.getUserName());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Complaint model = dataSnapshot.getValue(Complaint.class);
                        mList.add(model);
                    }
                    adapter = new ShowAllComplaintAdapter(getContext(), (ArrayList<Complaint>) mList, false);
                    rv.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}