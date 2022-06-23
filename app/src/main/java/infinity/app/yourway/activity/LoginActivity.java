package infinity.app.yourway.activity;

import androidx.appcompat.app.AppCompatActivity;

import infinity.app.yourway.MapsActivity;
import infinity.app.yourway.Model.BusModel;
import infinity.app.yourway.Model.PassengerModel;
import infinity.app.yourway.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText nic;
    EditText password;
    Button login;
    TextView nic_msg;
    TextView password_msg;
    TextView register;

    private DatabaseReference db;

    private String url;
    private RequestQueue requestQueue;
    private JsonArrayRequest jsonArrayRequest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setStatusBarColor(getResources().getColor(R.color.nav_bar_col));

        initComponent();
    }

    private void initComponent() {
        nic = findViewById(R.id.nic_number);
        password = findViewById(R.id.password);
        login = findViewById(R.id.sign_in_button);
        nic_msg = findViewById(R.id.nic_message);
        password_msg = findViewById(R.id.password_message);
        register = findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });
    }

    String nic_value;
    String password_value;
    @SuppressLint("SetTextI18n")
    private void validation() {

        nic_value = nic.getText().toString().trim();
        password_value = password.getText().toString().trim();




        if (nic_value.length()==10 && password_value.length()>8){
            nic_msg.setVisibility(View.GONE);
            password_msg.setVisibility(View.GONE);
            login_process();
            Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
        }
        if (nic_value.isEmpty() && password_value.isEmpty()){
            nic_msg.setVisibility(View.GONE);
            password_msg.setVisibility(View.GONE);
            nic_msg.setText("enter your NIC number");
            nic_msg.setVisibility(View.VISIBLE);
            password_msg.setText("enter your password");
            password_msg.setVisibility(View.VISIBLE);


        }

        else if (nic_value.length()==10 && password_value.length()<8){
            nic_msg.setVisibility(View.GONE);
            password_msg.setVisibility(View.GONE);
            nic_msg.setVisibility(View.GONE);
            password_msg.setText("password must be grater than 8 character");
            password_msg.setVisibility(View.VISIBLE);
        }

        else if (nic_value.length()<10 && password_value.length()>8){
            nic_msg.setVisibility(View.GONE);
            password_msg.setVisibility(View.GONE);
            nic_msg.setText("Invalid NIC");
            nic_msg.setVisibility(View.VISIBLE);
            password_msg.setVisibility(View.GONE);
        }

        else if (nic.length()<10 && password_value.length()<8){
            nic_msg.setVisibility(View.GONE);
            password_msg.setVisibility(View.GONE);
            nic_msg.setText("NIC number must be 10 character");
            nic_msg.setVisibility(View.VISIBLE);
            password_msg.setText("password must be grater than 8 character");
            password_msg.setVisibility(View.VISIBLE);
        }



    }



    private void login_process() {

        url = "https://infinity-bus-app.herokuapp.com/api/login?nic_no="+nic_value+"&password="+password_value;
        PassengerModel passengerModel = new PassengerModel();
        requestQueue = Volley.newRequestQueue(this);
        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
            int i;
            for (i=0; i<response.length(); i++){
                try{
                    JSONObject jsn = response.getJSONObject(i);
                    String msg = jsn.getString("msg");

                    Log.d("NJN", jsn.toString());

                    if (msg.equals("login successful")){
                        passengerModel.setUser_id(jsn.getString("user_id"));
                        passengerModel.setFirst_name(jsn.getString("first_name"));
                        passengerModel.setLast_name(jsn.getString("last_name"));
                        passengerModel.setEmail(jsn.getString("email"));
                        passengerModel.setPassword(jsn.getString("password"));
                        passengerModel.setNic(jsn.getString("nic"));
                        passengerModel.setPhone(jsn.getString("phone"));
                        passengerModel.setProfile_photo(jsn.getString("profile_photo"));
                        passengerModel.setMsg(jsn.getString("msg"));


                        try {
                            PassengerModel.save(passengerModel);
                            Toast.makeText(LoginActivity.this, "Local database created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            LoginActivity.this.startActivity(intent);
                        }catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "failed to create local database" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        String ds = jsn.getString("msg");
                        Toast.makeText(LoginActivity.this, "Login success" + passengerModel.getFirst_name(), Toast.LENGTH_SHORT).show();
                    }else if (msg.equals("Wrong Password")){
                        password_msg.setText("wrong password");
                        password_msg.setVisibility(View.VISIBLE);
                        nic_msg.setVisibility(View.GONE);
                    }
                    else if (msg.equals("nic not found")){
                        nic_msg.setText("nic number not registered");
                        nic_msg.setVisibility(View.VISIBLE);
                        password_msg.setVisibility(View.GONE);
                    }
                }catch (Exception e){

                }
            }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                
            }
        });
        requestQueue.add(jsonArrayRequest);
    }
}