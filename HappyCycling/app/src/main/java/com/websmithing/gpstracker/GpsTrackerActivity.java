package com.websmithing.gpstracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class GpsTrackerActivity extends Activity {
    private static final String TAG = "GpsTrackerActivity";

    private String defaultUploadWebsite;
    public int historyId;
    Chronometer focus;
    private static EditText txtUserName;
    private static EditText txtWebsite;
    private static Button trackingButton;
    private static Button UploadButton;
    private static Button VoucherButton;
    private static Button RefreshBtn;
    private static Button weatherBtn;
    private static ListView myListView;
    private static TextView distance;
    private static TextView carbon;
    private static TextView ScorePoints;
    private static TextView TableSore;
    private static TextView txtWeather;

    private boolean currentlyTracking;
    private RadioGroup intervalRadioGroup;
    private int intervalInMinutes = 1;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;
    JSONObject user = null;

    float ff = 0;
    int points=0;
    ArrayList<String> HistoryArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracker);

        defaultUploadWebsite = getString(R.string.default_upload_website);
        focus = (Chronometer) findViewById(R.id.chronometer);
        txtWebsite = (EditText)findViewById(R.id.txtWebsite);
        txtUserName = (EditText)findViewById(R.id.txtUserName);
        intervalRadioGroup = (RadioGroup)findViewById(R.id.intervalRadioGroup);
        trackingButton = (Button)findViewById(R.id.trackingButton);
        UploadButton = (Button)findViewById(R.id.UploadButton);
        RefreshBtn = (Button)findViewById(R.id.refreshBtn);
        weatherBtn = (Button)findViewById(R.id.weatherbtn);
        VoucherButton = (Button)findViewById(R.id.btnVoucher);
        txtUserName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        myListView = (ListView) findViewById(R.id.listView);
        distance = (TextView) findViewById(R.id.distance);
        carbon = (TextView) findViewById(R.id.carbontxt);
        //distance = (TextView) findViewById(R.id.points);
        TableSore = (TextView) findViewById(R.id.tableDynamic);
        ScorePoints =(TextView) findViewById(R.id.pointss);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost2);
        tabHost.setup();
        TabHost.TabSpec tabs = tabHost.newTabSpec("tracking");
        tabs.setContent(R.id.Track);
        tabs.setIndicator("Tracking");
        tabHost.addTab(tabs);
        tabs = tabHost.newTabSpec("Table");
        tabs.setContent(R.id.Import);
        tabs.setIndicator("Table");
        tabHost.addTab(tabs);
        tabs = tabHost.newTabSpec("history");
        tabs.setContent(R.id.History);
        tabs.setIndicator("History");
        tabHost.addTab(tabs);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        currentlyTracking = sharedPreferences.getBoolean("currentlyTracking", false);

        boolean firstTimeLoadindApp = sharedPreferences.getBoolean("firstTimeLoadindApp", true);
        if (firstTimeLoadindApp) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeLoadindApp", false);
            editor.putString("appID", UUID.randomUUID().toString());
            editor.apply();
        }

        //new HttpAsyncTaskk().execute("http://hmkcode.appspot.com/rest/controller/get.json");
        //AsyncTask test = new RetrieveWeather().execute();
        //Log.d(TAG, "iiiiiiiii"+test);

        intervalRadioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        saveInterval();
                    }
                });

        VoucherButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //LoginBtn(view.);
                Intent intent = new Intent(getBaseContext(), VoucherActivity.class);
                startActivity(intent);
            }
        });

        trackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                trackLocation(view);
            }
        });
        weatherBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new JSONParse().execute();
            }
        });
        RefreshBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String ScoreTableTmp = readFromFile("TableScore");
                String[] separated = ScoreTableTmp.split(",");
                TableSore.setText(Html.fromHtml(separated[0] + "<br />" + separated[1] + "<br />" + separated[2] + "<br />" + "<font color='#EE0000'>" + separated[3] + "</font>"));

            }
        });
        UploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                UploadToServer(view);
            }
        });
        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String test = String.valueOf(parent.getItemAtPosition(position));
                        Toast.makeText(getApplicationContext(), Integer.toString(position), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getBaseContext(), MapLoader.class);
                        intent.putExtra("position", Integer.toString(position));
                        startActivity(intent);
                    }
                }

        );

        //read score table and display
          // writeToFile("1. Dimitar    209, Anar    199, Julien    100, Test     0", "TableScore");
           //String ScoreTableTmp=readFromFile("TableScore");
          // TableSore.setText(ScoreTableTmp);
        //raboti

        //distance tracket
        Thread t = new Thread() {
            String FileNameTmp = "DistanceTracker";
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                String readDistance=readFromFile(FileNameTmp);
                                float f = Float.parseFloat(readDistance);
                                if(f > ff){
                                   points++;
                                   Log.d(TAG, "blabla"+Integer.toString(points));
                                   ScorePoints.setText("Points:  "+Integer.toString(points));
                                    File file1 = new File(Environment.getExternalStorageDirectory(), "TableScore");
                                    File gpxfile1 = new File(file1, "TableScore.txt");
                                    if (!gpxfile1.exists()) {
                                        Log.d(TAG, "NOT EXISTTT");
                                    }
                                    else {
                                        Log.d(TAG, "EXISTTT");
                                        gpxfile1.delete();
                                    }
                                    writeToFile("1. Dimitar    209, 2. Anar    199, 3. Julien    100, 4. Test     "+Integer.toString(points), "TableScore");

                                    //String ScoreTableTmp=readFromFile("TableScore");
                                    //String[] separated = ScoreTableTmp.split(",");
                                    //TableSore.setText(Html.fromHtml(separated[0] + "<br />" + separated[1] + "<br />" + separated[2] + "<br />" + "<font color='#EE0000'>" + separated[3] + "</font>"));

                                }
                                ff=f;
                                String distanceStr=String.format("%.2g",f);
                                distance.setText("Distance Covered (km): " + distanceStr);

                                float fuelCalc = f;
                                double kgCO2 = 12.85 * (fuelCalc / 10);
                                //double kgCO2 = 12.85 * litresUsedPerYear;
                                carbon.setText(Html.fromHtml( "<font color='#00FF00'>" +"CO2 saved: "+ kgCO2+ "</font>"));
                                /*
                                Inputs:
                                double kmPerDay: how many km driven per day, on average
                                double fuelEfficiency: how many km your car can go on 1 litre of gas.
                                Outputs:
                                kg of CO2 emitted by the car per year.

                                public static double carbonPrivateAutoUsage(double kmPerDay, double fuelEfficiency) {
                                    double litresUsedPerYear = 365 * (kmPerDay / fuelEfficiency);
                                    double kgCO2 = 12.85 * litresUsedPerYear;
                                    return kgCO2;
                                }
                                */
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();


        File file = new File(Environment.getExternalStorageDirectory(), "RaritanTracker");
        File gpxfile = new File(file, "RaritanTracker.txt");
             if (!gpxfile.exists()) {
            Log.d(TAG, "NOT EXISTTT");
            }
            else {
                 Log.d(TAG, "EXISTTT");
                 gpxfile.delete();
             }
        File file1 = new File(Environment.getExternalStorageDirectory(), "TableScore");
        File gpxfile1 = new File(file1, "TableScore.txt");
        if (!gpxfile1.exists()) {
            Log.d(TAG, "NOT EXISTTT");
        }
        else {
            Log.d(TAG, "EXISTTT");
            gpxfile1.delete();
        }
    }


    private void saveInterval() {
        if (currentlyTracking) {
            Toast.makeText(getApplicationContext(), R.string.user_needs_to_restart_tracking, Toast.LENGTH_LONG).show();
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (intervalRadioGroup.getCheckedRadioButtonId()) {
            case R.id.i1:
                editor.putInt("intervalInMinutes", 1);
                break;
            /*case R.id.i5:
                editor.putInt("intervalInMinutes", 5);
                break;*/
            /*case R.id.i15:
                editor.putInt("intervalInMinutes", 15);
                break;*/
        }

        editor.apply();
    }

    private void startAlarmManager() {
        Log.d(TAG, "startAlarmManager"); //see how many times this appear

        Context context = getBaseContext();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        intervalInMinutes = sharedPreferences.getInt("intervalInMinutes", 1);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 10000, // 60000 = 1 minute HERE
                pendingIntent);
    }

    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager");

        Context context = getBaseContext();
        Intent gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    //called when UploadButton is tapped
    protected void UploadToServer(View v) {
        String JSONstring;
        String FileNameTmp="RaritanTracker";
        String test = readFromFile(FileNameTmp); //this could be wrong if it doesnt want to upload, new function for read then in order to read the distance covered
        test = test.replaceAll(  "\\}\\{", "},{");
        test = test.replaceAll(  "\\]\\[", "],[");
        test=test.substring(0,test.length()-1);
        test = test.replaceAll(  "\\}\\]\\,\\{", "},{");  //}],{
        JSONstring = "["+test+"]";

        final String uploadWebsite =  defaultUploadWebsite;
        final RequestParams requestParams = new RequestParams();
        requestParams.put("User",JSONstring);

        //post to server
        LoopjHttpClient.post("https://lutcodecamp-niklaskolbe.c9.io/biketracks/create/fromapp", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                LoopjHttpClient.debugLoopJ(TAG, "sendLocationDataToWebsite - success", uploadWebsite, requestParams, responseBody, headers, statusCode, null);
                Log.d(TAG, requestParams.toString());
                //stopSelf();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //LoopjHttpClient.debugLoopJ(TAG, "sendLocationDataToWebsite - failure", uploadWebsite, requestParams, errorResponse, headers, statusCode, e);
                //stopSelf();
            }
        });


    }

    //read the string from the file
    private String readFromFile(String FileNameTmp) {
        BufferedReader reader = null;
        File file = new File(Environment.getExternalStorageDirectory(), FileNameTmp);
        File gpxfile = new File(file, FileNameTmp+".txt");
        try {
            reader = new BufferedReader(new FileReader(gpxfile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total.toString();
    }

    // called when trackingButton is tapped
    protected void trackLocation(View v) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        historyId++;
        if (historyId % 2 == 0) {// even
             String end="]";
            focus.stop();

        } else { //odd On pressed trackingButton for starting the tracking, an item with date will be added to the ListView in History
            focus.setBase(SystemClock.elapsedRealtime());
            focus.start();
            String start="[";
            writeToFile(start, "RaritanTracker");
            //Log.d(TAG, "wwwwwwwww"+historyId);
            HistoryArray.add("Recorded route on: " + date);
            ListAdapter testList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, HistoryArray);
            myListView.setAdapter(testList);

        }

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!saveUserSettings()) {
            return;
        }

        if (!checkIfGooglePlayEnabled()) {
            return;
        }

        if (currentlyTracking) {
            cancelAlarmManager();
            currentlyTracking = false;
            editor.putBoolean("currentlyTracking", false);
            editor.putString("sessionID", "");
        } else {
            startAlarmManager();
            currentlyTracking = true;
            editor.putBoolean("currentlyTracking", true);
            editor.putFloat("totalDistanceInMeters", 0f);
            editor.putBoolean("firstTimeGettingPosition", true);
            editor.putString("sessionID",  UUID.randomUUID().toString());
        }

        editor.apply();
        setTrackingButtonState();
    }
//"RaritanTracker"
    private void writeToFile(String s, String fileWrite) {
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), fileWrite);
            if (!root.exists()) {
                root.mkdirs();
            }
            Log.d(TAG, root.getAbsolutePath() + "mmmmmmmmmm");
            File gpxfile = new File(root, fileWrite+".txt");

            BufferedWriter bW;
            bW = new BufferedWriter(new FileWriter(gpxfile, true));

            bW.write(s);
            bW.newLine();
            bW.flush();
            bW.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    private boolean saveUserSettings() {
        if (textFieldsAreEmptyOrHaveSpaces()) {
            return false;
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (intervalRadioGroup.getCheckedRadioButtonId()) {
            case R.id.i1:
                editor.putInt("intervalInMinutes", 1);
                break;
            /*case R.id.i5:
                editor.putInt("intervalInMinutes", 5);
                break;*/
            /*case R.id.i15:
                editor.putInt("intervalInMinutes", 15);
                break;*/
        }

        editor.putString("userName", txtUserName.getText().toString().trim());
        editor.putString("defaultUploadWebsite", txtWebsite.getText().toString().trim());

        editor.apply();

        return true;
    }

    private boolean textFieldsAreEmptyOrHaveSpaces() {
        String tempUserName = txtUserName.getText().toString().trim();
        String tempWebsite = txtWebsite.getText().toString().trim();

        if (tempWebsite.length() == 0 || hasSpaces(tempWebsite) || tempUserName.length() == 0 || hasSpaces(tempUserName)) {
            Toast.makeText(this, R.string.textfields_empty_or_spaces, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    private boolean hasSpaces(String str) {
        return ((str.split(" ").length > 1) ? true : false);
    }

    private void displayUserSettings() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        intervalInMinutes = sharedPreferences.getInt("intervalInMinutes", 1);

        switch (intervalInMinutes) {
            case 1:
                intervalRadioGroup.check(R.id.i1);
                break;
           /* case 5:
                intervalRadioGroup.check(R.id.i5);
                break;*/
            /*case 15:
                intervalRadioGroup.check(R.id.i15);
                break;*/
        }

        txtWebsite.setText(sharedPreferences.getString("ADD SERVER", defaultUploadWebsite));
        txtUserName.setText(sharedPreferences.getString("userName", "Test"));
    }

    private boolean checkIfGooglePlayEnabled() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Log.e(TAG, "unable to connect to google play services.");
            Toast.makeText(getApplicationContext(), R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void setTrackingButtonState() {
        if (currentlyTracking) {
            trackingButton.setBackgroundResource(R.drawable.green_tracking_button);
            trackingButton.setTextColor(Color.BLACK);
            trackingButton.setText(R.string.tracking_is_on);
        } else {
            trackingButton.setBackgroundResource(R.drawable.red_tracking_button);
            trackingButton.setTextColor(Color.WHITE);
            trackingButton.setText(R.string.tracking_is_off);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume(); 

        displayUserSettings();
        setTrackingButtonState();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        //private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtWeather = (TextView)findViewById(R.id.weathertxt);

        }
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            // Getting JSON from URL
            JSONObject json = jParser.getJSONFromUrl("http://api.openweathermap.org/data/2.5/weather?q=Lappeenranta,fi");
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            //pDialog.dismiss();
            Log.d(TAG, "mmmmmm"+json.toString());
            try {
                // Getting JSON Array
                user=json.getJSONObject("main");
                //user = json.getJSONArray("main");
                //JSONObject c = user.getJSONObject();
                //c.length();
                //c.toString();
                // Storing  JSON item in a Variable
                //String temp = c.getString("temp");
                //Set JSON Data in TextView
                Log.d(TAG, "yyyy"+ user.toString());
                float kelvin = Float.parseFloat(user.getString("temp"));
                double celsius = kelvin - 273.15;
                txtWeather.setText("Current temperature in Lappeenrata: "+celsius+" C.");
               // name1.setText(name);
                //email1.setText(email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
