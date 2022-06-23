package infinity.app.yourway.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import infinity.app.yourway.R;

public class ComplainActivity extends AppCompatActivity {
    String bus_id = null;
    String user_id = null;
    Button save_complain;
    EditText title;
    EditText body;
    ProgressBar progressBar;
    CardView cardView;
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);
        initComponent();
        Intent i = getIntent();
        bus_id = i.getStringExtra("bus_id");
        user_id = i.getStringExtra("user_id");


        save_complain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveComplain();
            }
        });
        Log.d("complain", bus_id);
        Log.d("complain", user_id);
    }

    private void initComponent() {
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        save_complain = findViewById(R.id.save_complain);
        progressBar = findViewById(R.id.progressBar);
        cardView = findViewById(R.id.cardView);
        cardView.setVisibility(View.GONE);
    }

    private void saveComplain() {
        cardView.setVisibility(View.VISIBLE);
        String url = "https://infinity-bus-app.herokuapp.com/api/saveComplain";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", user_id);
            postData.put("bus_no", bus_id);
            postData.put("complain_title", title.getText().toString());
            postData.put("complain_body", body.getText().toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                cardView.setVisibility(View.GONE);
                title.setText("");
                body.setText("");
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