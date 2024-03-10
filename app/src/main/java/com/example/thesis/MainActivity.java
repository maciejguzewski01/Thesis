package com.example.thesis;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {
    private com.example.thesis.checkPermissions checkPermissions;
    private com.example.thesis.BluetoothConnection bluetoothConnection;

    private int lastGroundMoistureMeasure=-1;
    private int lastAirMoistureMeasure=-1;
    private int lastTemperatureMeasure=-1;
    private int lastInsolationMeasure=-1;
    private long lastMeasureTime=-1;

    private TextView lastGroundMoistureMeasureText;
    private TextView lastAirMoistureMeasureText;
    private TextView lastTemperatureMeasureText;
    private TextView lastInsolationMeasureText;
    private TextView lastMeasureTimeText;

    private Button connectButton;
    private Context context;

    private void setLastMeasurements()
    {
        lastGroundMoistureMeasureText = findViewById(R.id.ground_text);
        lastAirMoistureMeasureText = findViewById(R.id.air_text);
        lastTemperatureMeasureText = findViewById(R.id.temperature_text);
        lastInsolationMeasureText = findViewById(R.id.sun_text);
        lastMeasureTimeText = findViewById(R.id.time_text);

        SharedPreferences sharedPreferences = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE);
        lastGroundMoistureMeasure = sharedPreferences.getInt("ground", -1);
        lastAirMoistureMeasure = sharedPreferences.getInt("air", -1);
        lastTemperatureMeasure = sharedPreferences.getInt("temperature", -1);
        lastInsolationMeasure = sharedPreferences.getInt("sun", -1);
        lastMeasureTime = sharedPreferences.getLong("time", -1);

        if(lastGroundMoistureMeasure==-1) lastGroundMoistureMeasureText.setText("-");
        else lastGroundMoistureMeasureText.setText(String.valueOf(lastGroundMoistureMeasure));

        if(lastAirMoistureMeasure==-1) lastAirMoistureMeasureText.setText("-");
        else lastAirMoistureMeasureText.setText(String.valueOf(lastAirMoistureMeasure));

        if(lastTemperatureMeasure==-1) lastTemperatureMeasureText.setText("-");
        else lastTemperatureMeasureText.setText(String.valueOf(lastTemperatureMeasure));

        if(lastInsolationMeasure==-1) lastInsolationMeasureText.setText("-");
        else lastInsolationMeasureText.setText(String.valueOf(lastInsolationMeasure));

        if(lastMeasureTime==-1) lastMeasureTimeText.setText("-");
        else
        {
            Instant instant = Instant.ofEpochSecond(lastMeasureTime);
            ZoneId zoneId = ZoneId.of("Europe/Warsaw");
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String formattedDateTime = zonedDateTime.format(dateTimeFormatter);
            lastMeasureTimeText.setText(formattedDateTime);
        }
        setColors();
    }

    private void setColors()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("StorageSharedPreferencesNorms", MODE_PRIVATE);
        Norms.setGroundHumidityMin(sharedPreferences.getInt("groundMin", 40));
        Norms.setGroundHumidityMax(sharedPreferences.getInt("groundMax", 80));
        Norms.setAirHumidityMin(sharedPreferences.getInt("airMin", 50));
        Norms.setAirHumidityMax(sharedPreferences.getInt("airMax", 75));
        Norms.setTemperatureMin(sharedPreferences.getInt("temperatureMin", 17));
        Norms.setTemperatureMax(sharedPreferences.getInt("temperatureMax", 30));
        Norms.setInsolation(sharedPreferences.getInt("sun", 50));




        if (Norms.groundHumidityOk(lastGroundMoistureMeasure))
            lastGroundMoistureMeasureText.setTextColor(getResources().getColor(R.color.black));
        else {
            lastGroundMoistureMeasureText.setTextColor(getResources().getColor(R.color.red));
        }

        if (Norms.airHumidityOk(lastAirMoistureMeasure))
            lastAirMoistureMeasureText.setTextColor(getResources().getColor(R.color.black));
        else {
            lastAirMoistureMeasureText.setTextColor(getResources().getColor(R.color.red));
        }

        if (Norms.temperatureOk(lastTemperatureMeasure))
            lastTemperatureMeasureText.setTextColor(getResources().getColor(R.color.black));
        else {
            lastTemperatureMeasureText.setTextColor(getResources().getColor(R.color.red));
        }

        if (lastInsolationMeasure != -1) {
            if (Norms.insolationOk(lastInsolationMeasure))
                lastInsolationMeasureText.setTextColor(getResources().getColor(R.color.black));
            else {
                lastInsolationMeasureText.setTextColor(getResources().getColor(R.color.red));
            }
        }
    }

    private void somePermissionDeclined()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Nie udzielono uprawnień")
                .setMessage("Bez przydzielenia uprawnień aplikacja nie może poprawnie działać.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void cannotConnect()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Nie można połączyć")
                .setMessage("Aby sparować z urządzeniem wejdź w ustawienia bluetooth i wybierz urządzenie \"Flower\" podając jako kod \"1234\". Jeśli urządzenie zostało uprzednio sparowane upewnij się że jest włączone.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        context=this;
        checkPermissions = new com.example.thesis.checkPermissions(this);


        if (checkPermissions.allPermissionsGranted() == false) {somePermissionDeclined(); }
        else {
            setLastMeasurements();
            connectButton= findViewById(R.id.connect_button);
            connectButton.setText("Połącz");
            connectButton.setBackgroundColor(getResources().getColor(R.color.green));

            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        bluetoothConnection = new com.example.thesis.BluetoothConnection(context);

                        if (bluetoothConnection.enableBluetooth()) {
                            connectButton.setEnabled(false);

                            connectButton.setText("Łączę...");
                            connectButton.setBackgroundColor(getResources().getColor(R.color.yellow));
                            Toast toast = Toast.makeText(context, "Łączenie...", Toast.LENGTH_LONG);
                            toast.show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final boolean isConnected = bluetoothConnection.connect();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectButton.setText("Połącz");
                                            connectButton.setBackgroundColor(getResources().getColor(R.color.green));
                                            connectButton.setEnabled(true);

                                            if (isConnected) {
                                                Intent intent = new Intent(MainActivity.this, CurrentData.class);
                                                startActivity(intent);
                                                getLastMeas();
                                                setLastMeasurements();
                                            } else {
                                                cannotConnect();
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
            });

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setLastMeasurements();
    }



    @Override
    protected void onPause() {
        super.onPause();

        getLastMeas();

        SharedPreferences.Editor editor = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE).edit();
        editor.putInt("ground", lastGroundMoistureMeasure);
        editor.putInt("air", lastAirMoistureMeasure);
        editor.putInt("temperature", lastTemperatureMeasure);
        editor.putInt("sun", lastInsolationMeasure);
        editor.putLong("time",lastMeasureTime);
        editor.apply();

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

        getLastMeas();

        SharedPreferences.Editor editor = getSharedPreferences("StorageSharedPreferences", MODE_PRIVATE).edit();
        editor.putInt("ground", lastGroundMoistureMeasure);
        editor.putInt("air", lastAirMoistureMeasure);
        editor.putInt("temperature", lastTemperatureMeasure);
        editor.putInt("sun", lastInsolationMeasure);
        editor.putLong("time",lastMeasureTime);
        editor.apply();

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


    private void getLastMeas()
    {
        lastGroundMoistureMeasure=CurrentData.getGroundMoistureMeasure();
        lastAirMoistureMeasure=CurrentData.getAirMoistureMeasure();
        lastTemperatureMeasure=CurrentData.getTemperatureMeasure();
        lastInsolationMeasure=CurrentData.getInsolationMeasure();
        lastMeasureTime=CurrentData.getMeasureTime();
    }



}
