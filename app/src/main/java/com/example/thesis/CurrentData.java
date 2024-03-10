package com.example.thesis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentData extends AppCompatActivity {

    static private int groundMoistureMeasure = -1;
    static private int airMoistureMeasure = -1;
    static private int temperatureMeasure = -1;
    static private int insolationMeasure = -1;
    static private long measureTime = -1;

    private TextView groundMoistureMeasureText;
    private TextView airMoistureMeasureText;
    private TextView temperatureMeasureText;
    private TextView insolationMeasureText;
    private TextView measureTimeText;

    private Button refreshButton;
    private Button configurationButton;
    private Button historicDataButton;
    private BluetoothConnection bluetoothConnection;
    private Context context = this;

    private boolean canBeClicked=true;
    private boolean historicDataActivated=false;

    private void setColors()
    {
        if (Norms.groundHumidityOk(groundMoistureMeasure))
            groundMoistureMeasureText.setTextColor(getResources().getColor(R.color.black));
        else {
            groundMoistureMeasureText.setTextColor(getResources().getColor(R.color.red));
        }

        if (Norms.airHumidityOk(airMoistureMeasure))
            airMoistureMeasureText.setTextColor(getResources().getColor(R.color.black));
        else {
            airMoistureMeasureText.setTextColor(getResources().getColor(R.color.red));
        }

        if (Norms.temperatureOk(temperatureMeasure))
            temperatureMeasureText.setTextColor(getResources().getColor(R.color.black));
        else {
            temperatureMeasureText.setTextColor(getResources().getColor(R.color.red));
        }

        if (insolationMeasure != -1) {
            if (Norms.insolationOk(insolationMeasure))
                insolationMeasureText.setTextColor(getResources().getColor(R.color.black));
            else {
                insolationMeasureText.setTextColor(getResources().getColor(R.color.red));
            }
        }
    }
    private void checkNorms() {
        boolean groundOk = true;
        boolean airOk = true;
        boolean temperatureOk = true;
        boolean sunOk = true;
        setColors();

        if(Norms.groundHumidityOk(groundMoistureMeasure)==false) groundOk=false;
        if(Norms.airHumidityOk(airMoistureMeasure)==false) airOk=false;
        if(Norms.temperatureOk(temperatureMeasure)==false) temperatureOk=false;
        if(insolationMeasure != -1)
        {
            if(Norms.insolationOk(insolationMeasure)==false) sunOk=false;
        }




        if ((groundOk) && (airOk) && (temperatureOk) && (sunOk)) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("not_chan_1", "channel", importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this,CurrentData.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        StringBuilder stringToShow = new StringBuilder("Nieprawidłowa: ");

        if((groundOk == false)||(airOk == false)||(temperatureOk == false)||(sunOk == false))
        {
            if (groundOk == false) stringToShow.append("wilgotność gleby, ");
            if (airOk == false) stringToShow.append("wilgotność powietrza, ");
            if (temperatureOk == false) stringToShow.append("temperatura, ");
            if (sunOk == false) stringToShow.append("ilość słońca");

            if(stringToShow.charAt(stringToShow.length()-2)==',') stringToShow.deleteCharAt(stringToShow.length()-2);
        }



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "not_chan_1")
                .setSmallIcon(R.drawable.img)
                .setContentTitle("Przekroczenie parametrów!")
                .setContentText(stringToShow)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return;
        }
        notificationManager.notify(1, builder.build());


    }
    private void displayData()
    {
        groundMoistureMeasureText.setText(String.valueOf(groundMoistureMeasure));
        airMoistureMeasureText.setText(String.valueOf(airMoistureMeasure));
        temperatureMeasureText.setText(String.valueOf(temperatureMeasure));
        if(insolationMeasure!=-1) insolationMeasureText.setText(String.valueOf(insolationMeasure));
        else insolationMeasureText.setText("-");

        checkNorms();

        Instant instant = Instant.ofEpochSecond(measureTime);
        ZoneId zoneId = ZoneId.of("Europe/Warsaw");
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDateTime = zonedDateTime.format(dateTimeFormatter);
        measureTimeText.setText(formattedDateTime);

    }

    private void getData()
    {

        Communication.getLatestData();

        if(!Communication.haveDataCame())
        {

            Toast toast = Toast.makeText(context, "Błąd pobierania danych", Toast.LENGTH_LONG);
            toast.show();

        }
        else
        {
            groundMoistureMeasure= Communication.getGroundMoistureMeasure();
            airMoistureMeasure= Communication.getAirMoistureMeasureMeasure();
            temperatureMeasure=Communication.getTemperatureMeasure();
            insolationMeasure= Communication.getInsolationMeasure();
            measureTime= Communication.getTime();

            displayData();


        }

        refreshButton.setBackgroundColor(getResources().getColor(R.color.green));
        refreshButton.setText("Odśwież");

        canBeClicked=true;
        refreshButton.setEnabled(true);
        configurationButton.setEnabled(true);
        historicDataButton.setEnabled(true);
        refreshButton.setClickable(true);
        configurationButton.setClickable(true);
        historicDataButton.setClickable(true);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshButton=findViewById(R.id.refresh_button);
        configurationButton=findViewById(R.id.configuration_button);
        historicDataButton=findViewById(R.id.historic_button);
        setLastMeasurements();


        refreshButton.setBackgroundColor(getResources().getColor(R.color.green));
        refreshButton.setText("Odśwież");

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Can be clicked: "+canBeClicked);
                if (canBeClicked == true) {
                    canBeClicked=false;
                    refreshButton.setEnabled(false);
                    configurationButton.setEnabled(false);
                    historicDataButton.setEnabled(false);
                    refreshButton.setClickable(false);
                    configurationButton.setClickable(false);
                    historicDataButton.setClickable(false);


                    refreshButton.setBackgroundColor(getResources().getColor(R.color.yellow));
                    refreshButton.setText("Pobieram dane...");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast toast = Toast.makeText(context, "Pobieram dane...", Toast.LENGTH_LONG);
                                    toast.show();
                                    getData();
                                }
                            });
                        }
                    }).start();
                }
            }


        });


        configurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canBeClicked==true)
                {
                    Intent intent = new Intent(CurrentData.this, Configuration.class);
                    startActivity(intent);
                }


            }
        });

        historicDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canBeClicked==true)
                {
                    Intent intent = new Intent(CurrentData.this, HistoricData.class);
                    startActivity(intent);
                    historicDataActivated=true;
                    if(HistoricData.wasLastMeasureUpdated()==true) updateFromHistoricData();
                }

            }
        });

    }

    private void getBackgroundData()
    {
        Communication.getLatestData();
        if(Communication.haveDataCame())
        {
            groundMoistureMeasure= Communication.getGroundMoistureMeasure();
            airMoistureMeasure= Communication.getAirMoistureMeasureMeasure();
            temperatureMeasure=Communication.getTemperatureMeasure();
            insolationMeasure= Communication.getInsolationMeasure();
            measureTime= Communication.getTime();

            displayData();
        }
    }

    private void updateFromHistoricData()
    {
        groundMoistureMeasure= HistoricData.getLastGroundMoistureMeasure();
        airMoistureMeasure= HistoricData.getLastAirMoistureMeasure();
        temperatureMeasure=HistoricData.getLastTemperatureMeasure();
        insolationMeasure= HistoricData.getLastInsolationMeasure();
        measureTime= HistoricData.getTimeOfLastUpdate();
        displayData();
    }


    static public int getGroundMoistureMeasure()
    {
        return groundMoistureMeasure;
    }
    static public int getAirMoistureMeasure()
    {
        return airMoistureMeasure;
    }
    static public int getTemperatureMeasure()
    {
        return temperatureMeasure;
    }
    static public int getInsolationMeasure()
    {
        return insolationMeasure;
    }
    static public long getMeasureTime()
    {
        return measureTime;
    }


    private void setLastMeasurements()
    {

        groundMoistureMeasureText=findViewById(R.id.groundText);
        airMoistureMeasureText =findViewById(R.id.airText);
        temperatureMeasureText=findViewById(R.id.temperatureText);
        insolationMeasureText=findViewById(R.id.sunText);
        measureTimeText=findViewById(R.id.timeText);

        SharedPreferences sharedPreferences = getSharedPreferences("StorageSharedPreferencesCurrent", MODE_PRIVATE);
        groundMoistureMeasure = sharedPreferences.getInt("ground", -1);
        airMoistureMeasure = sharedPreferences.getInt("air", -1);
        temperatureMeasure = sharedPreferences.getInt("temperature", -1);
        insolationMeasure = sharedPreferences.getInt("sun", -1);
        measureTime = sharedPreferences.getLong("time", -1);

        SharedPreferences sharedPreferencesNorms = getSharedPreferences("StorageSharedPreferencesNorms", MODE_PRIVATE);
        Norms.setGroundHumidityMin(sharedPreferencesNorms.getInt("groundMin", 40));
        Norms.setGroundHumidityMax(sharedPreferencesNorms.getInt("groundMax", 80));
        Norms.setAirHumidityMin(sharedPreferencesNorms.getInt("airMin", 50));
        Norms.setAirHumidityMax(sharedPreferencesNorms.getInt("airMax", 75));
        Norms.setTemperatureMin(sharedPreferencesNorms.getInt("temperatureMin", 17));
        Norms.setTemperatureMax(sharedPreferencesNorms.getInt("temperatureMax", 30));
        Norms.setInsolation(sharedPreferencesNorms.getInt("sun", 50));

        if(groundMoistureMeasure==-1) groundMoistureMeasureText.setText("-");
        else groundMoistureMeasureText.setText(String.valueOf(groundMoistureMeasure));

        if(airMoistureMeasure==-1) airMoistureMeasureText.setText("-");
        else airMoistureMeasureText.setText(String.valueOf(airMoistureMeasure));

        if(temperatureMeasure==-1) temperatureMeasureText.setText("-");
        else temperatureMeasureText.setText(String.valueOf(temperatureMeasure));

        if(insolationMeasure==-1) insolationMeasureText.setText("-");
        else insolationMeasureText.setText(String.valueOf(insolationMeasure));

        if(measureTime==-1) measureTimeText.setText("-");
        else
        {
            Instant instant = Instant.ofEpochSecond(measureTime);
            ZoneId zoneId = ZoneId.of("Europe/Warsaw");
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String formattedDateTime = zonedDateTime.format(dateTimeFormatter);
            measureTimeText.setText(formattedDateTime);
        }
        setColors();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setLastMeasurements();
        if((historicDataActivated==true)&&(HistoricData.wasLastMeasureUpdated()==true))
        {
            updateFromHistoricData();
            historicDataActivated=false;
        }

    }



    @Override
    protected void onPause() {
        super.onPause();


        SharedPreferences.Editor editor = getSharedPreferences("StorageSharedPreferencesCurrent", MODE_PRIVATE).edit();
        editor.putInt("ground", groundMoistureMeasure);
        editor.putInt("air", airMoistureMeasure);
        editor.putInt("temperature", temperatureMeasure);
        editor.putInt("sun", insolationMeasure);
        editor.putLong("time",measureTime);
        editor.apply();

        SharedPreferences.Editor editorMain = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE).edit();
        editorMain.putInt("ground", groundMoistureMeasure);
        editorMain.putInt("air", airMoistureMeasure);
        editorMain.putInt("temperature", temperatureMeasure);
        editorMain.putInt("sun", insolationMeasure);
        editorMain.putLong("time",measureTime);
        editorMain.apply();

        SharedPreferences.Editor editorNorms = getSharedPreferences("StorageSharedPreferencesNorms", MODE_PRIVATE).edit();
        editorNorms.putInt("groundMin", Norms.getGroundHumidityMin());
        editorNorms.putInt("groundMax", Norms.getGroundHumidityMax());
        editorNorms.putInt("airMin", Norms.getAirHumidityMin());
        editorNorms.putInt("airMax", Norms.getAirHumidityMax());
        editorNorms.putInt("temperatureMin", Norms.getTemperatureMin());
        editorNorms.putInt("temperatureMax", Norms.getTemperatureMax());
        editorNorms.putInt("sun", Norms.getInsolation());
        editorNorms.apply();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = getSharedPreferences("StorageSharedPreferencesCurrent", MODE_PRIVATE).edit();
        editor.putInt("ground", groundMoistureMeasure);
        editor.putInt("air", airMoistureMeasure);
        editor.putInt("temperature", temperatureMeasure);
        editor.putInt("sun", insolationMeasure);
        editor.putLong("time",measureTime);
        editor.apply();

        SharedPreferences.Editor editorMain = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE).edit();
        editorMain.putInt("ground", groundMoistureMeasure);
        editorMain.putInt("air", airMoistureMeasure);
        editorMain.putInt("temperature", temperatureMeasure);
        editorMain.putInt("sun", insolationMeasure);
        editorMain.putLong("time",measureTime);
        editorMain.apply();

        SharedPreferences.Editor editorNorms = getSharedPreferences("StorageSharedPreferencesNorms", MODE_PRIVATE).edit();
        editorNorms.putInt("groundMin", Norms.getGroundHumidityMin());
        editorNorms.putInt("groundMax", Norms.getGroundHumidityMax());
        editorNorms.putInt("airMin", Norms.getAirHumidityMin());
        editorNorms.putInt("airMax", Norms.getAirHumidityMax());
        editorNorms.putInt("temperatureMin", Norms.getTemperatureMin());
        editorNorms.putInt("temperatureMax", Norms.getTemperatureMax());
        editorNorms.putInt("sun", Norms.getInsolation());
        editorNorms.apply();
    }

}
