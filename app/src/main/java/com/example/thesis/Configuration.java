package com.example.thesis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Configuration extends AppCompatActivity {
    private Button acceptButton;
    private Button declineButton;
    private Context context=this;

    private EditText groundHumMinText;
    private EditText groundHumMaxText;
    private EditText airHumMinText;
    private EditText airHumMaxText;
    private EditText temperatureMinText;
    private EditText temperatureMaxText;
    private EditText insolationMinText;

    private int groundHumMin;
    private int groundHumMax;
    private int airHumMin;
    private int airHumMax;
    private int temperatureMin;
    private int temperatureMax;
    private int insolationMin;

    private boolean canBeClicked=true;


    private boolean dataCorrect()
    {
        String groundHumMinString=groundHumMinText.getText().toString();
        String groundHumMaxString=groundHumMaxText.getText().toString();
        String airHumMinString=airHumMinText.getText().toString();
        String airHumMaxString=airHumMaxText.getText().toString();
        String temperatureMinString=temperatureMinText.getText().toString();
        String temperatureMaxString= temperatureMaxText.getText().toString();
        String insolationMinString=insolationMinText.getText().toString();
        if((groundHumMinString.isEmpty())||(groundHumMaxString.isEmpty())||(airHumMinString.isEmpty())||(airHumMaxString.isEmpty())||(temperatureMinString.isEmpty())||(temperatureMaxString.isEmpty())||(insolationMinString.isEmpty())) return false;

        int groundHumMinTemp=Integer.parseInt(groundHumMinString);
        int groundHumMaxTemp=Integer.parseInt(groundHumMaxString);
        int airHumMinTemp=Integer.parseInt(airHumMinString);
        int airHumMaxTemp=Integer.parseInt(airHumMaxString);
        int temperatureMinTemp=Integer.parseInt(temperatureMinString);
        int temperatureMaxTemp= Integer.parseInt(temperatureMaxString);
        int insolationMinTemp=Integer.parseInt(insolationMinString);

        if((groundHumMinTemp<0)||(groundHumMinTemp>100)||(groundHumMaxTemp>100)||(groundHumMinTemp>groundHumMaxTemp)) return false;
        if((airHumMinTemp<20)||(airHumMinTemp>90)||(airHumMaxTemp>90)||(airHumMinTemp>airHumMaxTemp)) return false;
        if((temperatureMinTemp<0||(temperatureMinTemp>50)||temperatureMaxTemp>50)||(temperatureMinTemp>temperatureMaxTemp)) return false;
        if((insolationMinTemp<0)||(insolationMinTemp>100)) return false;

        groundHumMin=groundHumMinTemp;
        groundHumMax=groundHumMaxTemp;
        airHumMin=airHumMinTemp;
        airHumMax=airHumMaxTemp;
        temperatureMin=temperatureMinTemp;
        temperatureMax=temperatureMaxTemp;
        insolationMin=insolationMinTemp;

        return true;
    }

    private void updateNorms()
    {
        Norms.setGroundHumidityMin(groundHumMin);
        Norms.setGroundHumidityMax(groundHumMax);
        Norms.setAirHumidityMin(airHumMin);
        Norms.setAirHumidityMax(airHumMax);
        Norms.setTemperatureMin(temperatureMin);
        Norms.setTemperatureMax(temperatureMax);
        Norms.setInsolation(insolationMin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        acceptButton= findViewById(R.id.accept_button);
        declineButton = findViewById(R.id.decline_button);

        groundHumMinText = findViewById(R.id.text_min_ground);
        groundHumMaxText= findViewById(R.id.text_ground_max);
        airHumMinText= findViewById(R.id.text_air_min);
        airHumMaxText= findViewById(R.id.text_air_max);
        temperatureMinText= findViewById(R.id.text_temp_min);
        temperatureMaxText= findViewById(R.id.text_temp_max);
        insolationMinText= findViewById(R.id.text_sun);

        acceptButton.setBackgroundColor(getResources().getColor(R.color.darkgreen));
        acceptButton.setText("Prześlij");

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canBeClicked == true) {
                    if (dataCorrect() == false) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Nieprawidłowe dane")
                                .setMessage("Upewnij się że wprowadziłeś wszystkie dane poprawnie")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    } else {
                        acceptButton.setBackgroundColor(getResources().getColor(R.color.yellow));
                        acceptButton.setText("Przesyłam...");
                        canBeClicked=false;
                        acceptButton.setEnabled(false);
                        acceptButton.setClickable(false);
                        declineButton.setEnabled(false);
                        declineButton.setClickable(false);

                        updateNorms();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if(Communication.sendNorms()==false)
                                        {
                                            AlertDialog alertDialog = new AlertDialog.Builder(context)
                                                    .setTitle("Błąd")
                                                    .setMessage("Nie udało się przesłać konfiguracji.")
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .create();
                                            alertDialog.show();
                                        }
                                        else
                                        {
                                            Toast toast = Toast.makeText(context, "Zaktualizowano", Toast.LENGTH_LONG);
                                            toast.show();
                                            if (Communication.haveDataCame()) finish();
                                        }
                                        canBeClicked=true;
                                        acceptButton.setEnabled(true);
                                        acceptButton.setClickable(true);
                                        declineButton.setEnabled(true);
                                        declineButton.setClickable(true);


                                    }
                                });
                            }
                        }).start();

                    }
                }
            }
        });


        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canBeClicked==true) finish();
            }
        });





    }
}
