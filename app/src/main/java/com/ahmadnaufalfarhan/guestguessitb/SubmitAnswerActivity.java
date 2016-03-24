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
import java.io.PrintWriter;
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

    private class SubmitAnswerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... answers) {
            StringBuilder sb = new StringBuilder();
            String result = "";
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
                requestJson.put(Identification.PRM_LONGITUDE, MapsActivity.currentPosition.longitude);   // TODO: set current longitude question
                requestJson.put(Identification.PRM_LATITUDE, MapsActivity.currentPosition.latitude);    // TODO: set current latitude question
                requestJson.put(Identification.PRM_TOKEN, MainActivity.token);    // TODO: set current question token

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(requestJson.toString());      // write the request to outputstream, sending them to server
                out.flush();

                // if the response is OK (200), get the response string
                // and parse them as JSON
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line + "\n");

                br.close();

                // return the json string as the result
                result = sb.toString();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
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
        protected void onPostExecute(String outputResult) {
            if (outputResult.length() > 0) {
                Toast.makeText(SubmitAnswerActivity.this, outputResult, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject result = new JSONObject(outputResult);   // parse result to json

                    // check if the answer request is a right answer
                    String status = result.getString(Identification.PRM_STATUS);
                    if (status.equalsIgnoreCase(Identification.STATUS_OK)) {
                        // if the answer is right, autoload the map
                        // and set the marker to point to the new coordinate
                        MapsActivity.longitude = result.getDouble("latitude");
                        MapsActivity.latitude = result.getDouble("longitude");
                        MainActivity.token = result.getString("token");    // TODO: set the new token

                        // resume maps activity intent
                        Intent intent = new Intent(SubmitAnswerActivity.this, MapsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    } else if (status.equalsIgnoreCase(Identification.STATUS_WRONGANSWER)) {
                        Toast.makeText(SubmitAnswerActivity.this, "Oops! Wrong answer! Please try another answer.", Toast.LENGTH_LONG).show();
                    } else if (status.equalsIgnoreCase(Identification.STATUS_FINISH)) {
                        // TODO: Start finished activity or go back to splash screen
                        Toast.makeText(SubmitAnswerActivity.this, "You have completed the game.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SubmitAnswerActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
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
