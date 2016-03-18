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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public String token;
    public String nimId;

    private EditText nimEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nimEditText = (EditText) findViewById(R.id.nimEditText);

        // initiate the token and identification NIM
        token = "";
        nimId = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start the Google Maps Activity by getting the intent
     * */
    public void requestChallengeHandler(View view) {
        String nimCurrent = nimEditText.getText().toString();
        if (nimCurrent.equalsIgnoreCase(""))
            Toast.makeText(MainActivity.this, "You have to enter your Student ID to play!", Toast.LENGTH_SHORT).show();
        else {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = connMgr.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isConnected())
                //new RequestChallengeTask().execute();
                startMapsActivity(104, 99);
            else
                Toast.makeText(MainActivity.this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *   This is the main class for our request forwarding method.
     *   We will be using AsyncTask to run our request on the background
     */
    private class RequestChallengeTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            StringBuilder sb = new StringBuilder();
            JSONObject result = new JSONObject();

            HttpURLConnection conn = null;
            try {
                URL url = new URL(Identification.URL_PRODUCTION);
                conn = (HttpURLConnection) url.openConnection();

                /* connection properties */
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                /* create the request JSON object */
                JSONObject requestJson = new JSONObject();
                requestJson.put(Identification.PRM_COMMUNICATION, Identification.COM_REQLOCATION);
                requestJson.put(Identification.PRM_NIM, Identification.NIM_VALUE);

                conn.connect();     // connect to the url
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(requestJson.toString());      // write the request to outputstream, sending them to server
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // if the response is OK (200), get the response string
                    // and parse them as JSON
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line + "\n");

                    br.close();

                    // return the json string as the result
                    result = new JSONObject(sb.toString());
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (conn != null)
                    conn.disconnect();
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
                        double latitude = result.getDouble("latitude");
                        double longitude = result.getDouble("longitude");
                        String token = result.getString("token");    // TODO: set the new token

                        // start the maps activity
                        startMapsActivity(latitude, longitude);

                    } else if (status.equalsIgnoreCase(Identification.STATUS_WRONGANSWER)) {
                        Toast.makeText(MainActivity.this, "Oops! Wrong answer! Please try another answer.", Toast.LENGTH_LONG).show();
                    } else if (status.equalsIgnoreCase(Identification.STATUS_FINISH)) {
                        // TODO: Start finished activity or go back to splash screen
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                    Log.d("Guest Guess ITB Debug", e.toString());
                }

            } else {
                Toast.makeText(MainActivity.this, "Connection error! Please retry after a while.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Method to send intent to Maps Activity
     * Therefore, starting the game
     */
    public void startMapsActivity(double latitude, double longitude) {
        // resume maps activity intent
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra(Identification.PRM_LATITUDE, latitude);
        intent.putExtra(Identification.PRM_LONGITUDE, longitude);
        startActivity(intent);
    }


}
