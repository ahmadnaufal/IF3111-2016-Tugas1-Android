package com.ahmadnaufalfarhan.guestguessitb;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class SubmitAnswerActivity extends AppCompatActivity {

    private Spinner spinnerAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_answer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerAnswers = (Spinner) findViewById(R.id.spinnerAnswers);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.location_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnswers.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void submitAnswer(View view) {
        // get the selected answer string
        String answer = spinnerAnswers.getSelectedItem().toString().toLowerCase();
        answer = answer.replace(" ", "_");

        /* Establish a connection to the server and prepare the JSON */
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = connMgr.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isConnected())
            new SubmitAnswerTask().execute(answer);
        else
            Toast.makeText(SubmitAnswerActivity.this, "You are not connected to internet", Toast.LENGTH_SHORT).show();
    }

    private class SubmitAnswerTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... answers) {
            StringBuilder sb = new StringBuilder();
            JSONObject result = new JSONObject();
            String answer = answers[0];

            Socket socket = null;

            try {
                InetAddress serverAddress = InetAddress.getByName(Identification.SERVER_IP);
                socket = new Socket(serverAddress, Identification.SERVER_PORT);

                /* create the request JSON object */
                JSONObject requestJson = new JSONObject();
                requestJson.put(Identification.PRM_COMMUNICATION, Identification.COM_ANSWER);
                requestJson.put(Identification.PRM_NIM, Identification.NIM_VALUE);
                requestJson.put(Identification.PRM_ANSWER, answer);
                requestJson.put(Identification.PRM_LONGITUDE, null);   // TODO: set current longitude question
                requestJson.put(Identification.PRM_LATITUDE, null);    // TODO: set current latitude question
                requestJson.put(Identification.PRM_TOKEN, null);    // TODO: set current question token

                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
                out.write(requestJson.toString());      // write the request to outputstream, sending them to server
                out.close();

                // if the response is OK (200), get the response string
                // and parse them as JSON
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line + "\n");

                br.close();

                // return the json string as the result
                result = new JSONObject(sb.toString());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Toast.makeText(SubmitAnswerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result.length() > 0) {
                try {
                    // check if the answer request is a right answer
                    String status = result.getString(Identification.PRM_STATUS);
                    if (status.equalsIgnoreCase(Identification.STATUS_OK)) {
                        // if the answer is right, autoload the map
                        // and set the marker to point to the new coordinate
                        double newLatitude = result.getDouble("latitude");
                        double newLongitude = result.getDouble("longitude");
                        String newToken = result.getString("token");    // TODO: set the new token

                        // resume maps activity intent
                        Intent intent = new Intent(SubmitAnswerActivity.this, MapsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Identification.PRM_LATITUDE, newLatitude);
                        intent.putExtra(Identification.PRM_LONGITUDE, newLongitude);
                        startActivity(intent);

                    } else if (status.equalsIgnoreCase(Identification.STATUS_WRONGANSWER)) {
                        Toast.makeText(SubmitAnswerActivity.this, "Oops! Wrong answer! Please try another answer.", Toast.LENGTH_LONG).show();
                    } else if (status.equalsIgnoreCase(Identification.STATUS_FINISH)) {
                        // TODO: Start finished activity or go back to splash screen

                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                    Log.d("Guest Guess ITB Debug", e.toString());
                }

            } else {
                Toast.makeText(SubmitAnswerActivity.this, "Connection error! Please retry after a while.", Toast.LENGTH_LONG).show();
            }

        }
    }
}
