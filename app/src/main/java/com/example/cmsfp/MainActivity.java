package com.example.cmsfp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.cmsfp.R;
//import com.example.ocms.R;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
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
import com.example.cmsfp.adapter.ShowAllComplaintAdapter;
import com.example.cmsfp.model.Complaint;
import com.example.cmsfp.model.UserModel;
import com.example.cmsfp.utils.Constants;
import com.example.cmsfp.utils.Permissions;
import com.example.cmsfp.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] courses = {Constants.GARBAGE, Constants.WATER, Constants.ROADSANDLIGHTS, Constants.ELECTRICITY, Constants.PESTS, Constants.PARKMAINTAINANCE};

    private FloatingActionButton fab_createFood;
    private int selectedDepPosition = 0;
    private Spinner departmentSpin;
    private ArrayAdapter spinnerAdapter;

    private ProgressDialog progressDialog;
    private BottomSheetDialog bottomSheetDialog;
    StorageReference storageReference;
    private Uri imageUri;
    private String imageString;
    private StorageTask uploadTask;
    UserModel model = null;
    String department = "";
    ImageView iv_complaintImage;

    ArrayList<Complaint> mList = new ArrayList<>();
    RecyclerView rv;
    ShowAllComplaintAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.rv_showAllFood);

        FirebaseApp.initializeApp(this);

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        progressDialog = new ProgressDialog(MainActivity.this);
        getUser();

        fab_createFood = findViewById(R.id.fab_createFood);
        fab_createFood.setOnClickListener(v -> createDialog());
    }

    private void initializeSpinner() {
        departmentSpin = findViewById(R.id.sp_departments);
        spinnerAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, courses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpin.setAdapter(spinnerAdapter);
        setLayoutAccToDep();
        departmentSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepPosition = position;
                setLayoutAccToDep();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setLayoutAccToDep() {
        getAllFood(departmentSpin.getSelectedItem().toString());
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alert_add_complaint, null, false);
        builder.setView(view);

        EditText et_description = view.findViewById(R.id.et_description);
        iv_complaintImage = view.findViewById(R.id.iv_complaintImage);
        Spinner spino = view.findViewById(R.id.sp_department);
        Button btn_create = view.findViewById(R.id.btn_addComplaint);
        spino.setOnItemSelectedListener(this);

        ArrayAdapter ad = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, courses);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spino.setAdapter(ad);
        spino.setSelection(selectedDepPosition);

        AlertDialog alertDialog = builder.create();

        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");

        iv_complaintImage.setOnClickListener(v -> showBottomSheetDialog());
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
                createFood(complaintDescription, spino.getSelectedItem().toString());
                alertDialog.dismiss();
            }
        });
    }

    private void showBottomSheetDialog() {

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_image_picker);

        LinearLayoutCompat btnCamera = bottomSheetDialog.findViewById(R.id.btn_camera);
        LinearLayoutCompat btnGallery = bottomSheetDialog.findViewById(R.id.btn_gallery);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Permissions.checkCameraPermission(MainActivity.this)) {
                    reqCamPermLauncher.launch(Manifest.permission.CAMERA);
                } else {
                    imageUri = createImageUri();
                    cameraResultLauncher.launch(imageUri);
                }
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Permissions.checkGalleryPermission(MainActivity.this)) {
                    reqExternalStoragePermLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryResultLauncher.launch(intent);
                }
            }
        });


        bottomSheetDialog.show();
    }

    private final ActivityResultLauncher<String> reqExternalStoragePermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    galleryResultLauncher.launch(intent);
                }
            });

    private final ActivityResultLauncher<Intent> galleryResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    imageUri = result.getData().getData();
                    uploadImage();
                    bottomSheetDialog.dismiss();
                }
            });


    private final ActivityResultLauncher<String> reqCamPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
//                    imageUri = createImageUri();
                }
            });

    private final ActivityResultLauncher<Uri> cameraResultLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), isGranted -> {
                bottomSheetDialog.dismiss();
                uploadImage();
            });

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private Uri createImageUri() {
        try {
            File file = File.createTempFile("IMG_",
                    ".jpg",
                    MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            return FileProvider.getUriForFile(
                    MainActivity.this,
                    "com.example.ocms.fileProvider",
                    file
            );
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);
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
                        Glide.with(MainActivity.this).load(imageUri).into(iv_complaintImage);
                    } catch (Exception e) {
                        Log.d("TAG1", "error Message: " + e.getMessage());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    pd.dismiss();
                } else {
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                    pd.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(MainActivity.this, "No image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void createFood(String complaintDescription, String departmentName) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(departmentName).child(Constants.OCMS_USER).child(userId);

        Complaint complaint = new Complaint();
        complaint.setDepartment(departmentName);
        complaint.setId(model.getId());
        complaint.setComplaintId(Utils.generateUniqueId());
        complaint.setUserName(model.getUserName());
        complaint.setStatus("pending");
        complaint.setReply("pending");
        complaint.setDescription(complaintDescription);
        complaint.setImage(imageString);

        reference.push().setValue(complaint).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                getAllFood(departmentName);
                Toast.makeText(MainActivity.this, "Complaint Added Successfully", Toast.LENGTH_SHORT).show();
                imageString = "";
            } else {
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void getUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid());//.child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    model = snapshot.getValue(UserModel.class);
                    initializeSpinner();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        department = courses[position];
        selectedDepPosition = position;
        Toast.makeText(MainActivity.this, courses[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getAllFood(String depName) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(RegisterActivity.COMPLAINT).child(model.getUserName());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMPLAINT).child(depName).child(Constants.OCMS_USER).child(model.getId());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Complaint model = dataSnapshot.getValue(Complaint.class);
                        mList.add(model);
                    }
                    departmentSpin.setSelection(selectedDepPosition);
                    adapter = new ShowAllComplaintAdapter(MainActivity.this, mList, false);
                    rv.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}