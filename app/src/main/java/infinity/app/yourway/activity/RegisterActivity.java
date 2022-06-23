package infinity.app.yourway.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import infinity.app.yourway.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    ImageView profile_photo;
    String image_id;
    String profile_img;
    String first_n;
    String last_n;
    String Email;
    String pass;
    String NIC;
    String phone;
    CardView cardView;
    private final int PICK_IMAGE_REQUEST = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bindViews();
    }

    EditText first_name, last_name,email,password,phone_number,nic;
    Button sign_up_btn;
    private void bindViews() {
        image_id = db.collection("passenger").document().getId();
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone_number = findViewById(R.id.phone);
        nic = findViewById(R.id.nic);
        profile_photo = findViewById(R.id.profile_photo);
        sign_up_btn = findViewById(R.id.sign_up_button);
        cardView = findViewById(R.id.card);
        cardView.setVisibility(View.GONE);

        profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate_data();
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select profile Image"),PICK_IMAGE_REQUEST);
    }

    private Uri imagePath = null;
    StorageReference ref_main;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                profile_photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void validate_data() { // sign up activity validation part
        first_n = first_name.getText().toString();
        if(first_n.isEmpty()){
            Toast.makeText(this, "First Name cannot be empty", Toast.LENGTH_SHORT).show();
            first_name.requestFocus();
            return;
        }
        if(first_n.length()<2){
            Toast.makeText(this, "First Name too short", Toast.LENGTH_SHORT).show();
            first_name.requestFocus();
            return;
        }

        last_n = last_name.getText().toString();
        if(last_n.isEmpty()){
            Toast.makeText(this, "Last Name cannot be empty", Toast.LENGTH_SHORT).show();
            last_name.requestFocus();
            return;
        }
        if(last_n.length()<2){
            Toast.makeText(this, "Last Name too short", Toast.LENGTH_SHORT).show();
            last_name.requestFocus();
            return;
        }

        Email = email.getText().toString();
        if(Email.isEmpty()){
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }
        if(Email.length()<5){
            Toast.makeText(this, "Email too short", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }

        pass = password.getText().toString();
        if(pass.isEmpty()){
            Toast.makeText(this, "Last Name cannot be empty", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }
        if(pass.length()<8){
            Toast.makeText(this, "Password too weak", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }
        NIC = nic.getText().toString();
        phone = phone_number.getText().toString();
        profile_img = "";
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ref_main = FirebaseStorage.getInstance().getReference();
        ref_main.child("passengers/"+image_id).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegisterActivity.this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();

                ref_main.child("passengers/"+image_id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profile_img  = uri.toString();
                        progressDialog.hide();
                        progressDialog.dismiss();
                        register_passenger();
                        return;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        profile_img  = "https://images.unsplash.com/photo-1578328819058-b69f3a3b0f6b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80";
                        progressDialog.hide();
                        progressDialog.dismiss();
                        //submit_data();
                        return;
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Failed to uplod photo"+e.getMessage(), Toast.LENGTH_SHORT).show();
                profile_img  = "https://images.unsplash.com/photo-1578328819058-b69f3a3b0f6b?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80";
                progressDialog.hide();
                progressDialog.dismiss();
                //submit_data();
                return;
            }
        });


    }

    ProgressDialog progressDialog;

    public static final String USERS_TABLE = "users";
//    private void submit_data() {
//
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Please Wait");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//        db.collection(USERS_TABLE).whereEqualTo("email", userModel.email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                if(!queryDocumentSnapshots.isEmpty()){
//                    Toast.makeText(SignUpActivity.this, "Email already exist", Toast.LENGTH_SHORT).show();
//                    progressDialog.hide();
//                    progressDialog.dismiss();
//                    return;
//                }
//
//                userModel.user_id = db.collection(USERS_TABLE).document().getId();
//                userModel.reg_date = String.valueOf(Calendar.getInstance().getTimeInMillis() +"");
//
//                db.collection(USERS_TABLE).document(userModel.user_id).set(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//
//                        Toast.makeText(SignUpActivity.this, "User Account Created Successfully", Toast.LENGTH_SHORT).show();
//                        progressDialog.hide();
//                        progressDialog.dismiss();
//                        if(login_user()){
//                            Toast.makeText(SignUpActivity.this, "Your login successfully", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            SignUpActivity.this.startActivity(intent);
//                            return;
//                        }else{
//                            Toast.makeText(SignUpActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(SignUpActivity.this, "Failed to create an account", Toast.LENGTH_SHORT).show();
//                        progressDialog.hide();
//                        progressDialog.dismiss();
//                    }
//                });
//
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(SignUpActivity.this, "Failed to create an account", Toast.LENGTH_SHORT).show();
//                progressDialog.hide();
//                progressDialog.dismiss();
//            }
//        });
//
//    }

    private void register_passenger() {
        cardView.setVisibility(View.VISIBLE);
        String url = "https://infinity-bus-app.herokuapp.com/api/savePassenger";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("first_name", first_n);
            postData.put("last_name", last_n);
            postData.put("email", Email);
            postData.put("password", pass);
            postData.put("nic", NIC);
            postData.put("phone", phone);
            postData.put("profile_photo", profile_img);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                cardView.setVisibility(View.GONE);
//                title.setText("");
//                body.setText("");
                try {
                    String msg = response.getString("msg");
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


//                if(response.getJSONObject().getString("msg")=="Record Saved.")

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cardView.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Response: "+error, Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

}