package com.example.thesis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HistoricData extends AppCompatActivity {
    private Button goBackButton;
    private Button getHistoricDataButton;
    private EditText editText;
    private int numberOfHistoricData=0;
    private Context context=this;
    private TextView insolationText;
    private TextView timeText;

    private int insolationMeasure=-1;
    private long measureTime=-1;
    private int[] groundMoistureMeasure;
    private int[] airMoistureMeasure;
    private int[] temperatureMeasure;
    private TableLayout tableLayout;
    private boolean canBeClicked=true;

    private static boolean lastMeasureUpdated=false;
    private static long timeOfLastUpdate=-1;
    private static int lastGroundMoistureMeasure=-1;
    private static int lastAirMoistureMeasure=-1;
    private static int lastTemperatureMeasure=-1;
    private static int lastInsolationMeasure=-1;

    public static boolean wasLastMeasureUpdated(){return lastMeasureUpdated;}
    public static long getTimeOfLastUpdate(){return timeOfLastUpdate;}
    public static int getLastGroundMoistureMeasure(){return lastGroundMoistureMeasure;}
    public static int getLastAirMoistureMeasure(){return lastAirMoistureMeasure;}
    public static int getLastTemperatureMeasure(){return lastTemperatureMeasure;}
    public static int getLastInsolationMeasure(){return lastInsolationMeasure;}


    private boolean numberCorrect()
    {
        String temp= editText.getText().toString();
        if(temp.isEmpty()) return false;
        int tempInt=Integer.parseInt(temp);
        if((tempInt<1)||(tempInt>144)) return false;

        numberOfHistoricData=tempInt;
        return true;
    }

    private void getHistoricData()
    {
        Communication.getHistoricData(numberOfHistoricData);

        getHistoricDataButton.setBackgroundColor(getResources().getColor(R.color.green));
        getHistoricDataButton.setText("Pobierz");
        canBeClicked=true;
        getHistoricDataButton.setEnabled(true);
        goBackButton.setEnabled(true);
        getHistoricDataButton.setClickable(true);
        goBackButton.setClickable(true);


        if(!Communication.haveDataCame())
        {
            Toast toast = Toast.makeText(context, "Błąd pobierania danych", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        else
        {
            lastMeasureUpdated=true;

            groundMoistureMeasure= Communication.getHistoricGroundMoistureMeasure();
            airMoistureMeasure= Communication.getHistoricAirMoistureMeasureMeasure();
            temperatureMeasure=Communication.getHistoricTemperatureMeasure();
            insolationMeasure= Communication.getHistoricInsolationMeasure();
            measureTime= Communication.getHistoricTime();

            if(insolationMeasure!=-1) insolationText.setText(String.valueOf(insolationMeasure));
            else insolationText.setText("-");

            Instant instant = Instant.ofEpochSecond(measureTime);
            ZoneId zoneId = ZoneId.of("Europe/Warsaw");
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String formattedDateTime = zonedDateTime.format(dateTimeFormatter);
            timeText.setText(formattedDateTime);

            int incomingHistoricDataNumber=Communication.getNumberOfHistoricMeasure();
            groundMoistureMeasure = new int[incomingHistoricDataNumber];
            airMoistureMeasure = new int[incomingHistoricDataNumber];
            temperatureMeasure = new int[incomingHistoricDataNumber];

            groundMoistureMeasure=Communication.getHistoricGroundMoistureMeasure();
            airMoistureMeasure=Communication.getHistoricAirMoistureMeasureMeasure();
            temperatureMeasure=Communication.getHistoricTemperatureMeasure();

            tableLayout.removeAllViews();


            timeOfLastUpdate=measureTime;
            lastGroundMoistureMeasure=groundMoistureMeasure[0];
            lastAirMoistureMeasure=airMoistureMeasure[0];
            lastTemperatureMeasure=temperatureMeasure[0];
            lastInsolationMeasure=insolationMeasure;

            for(int i=0;i<incomingHistoricDataNumber;i++)
            {
                TableRow tableRow = new TableRow(this);

                TextView idxText = new TextView(this);
                idxText.setText(String.valueOf(i+1));
                tableRow.addView(idxText);

                TextView groundText = new TextView(this);
                groundText.setText(String.valueOf(groundMoistureMeasure[i]));
                groundText.setTypeface(null, Typeface.BOLD);
                tableRow.addView(groundText);

                TextView airText = new TextView(this);
                airText.setText(String.valueOf(airMoistureMeasure[i]));
                airText.setTypeface(null, Typeface.BOLD);
                tableRow.addView(airText);

                TextView temperatureText = new TextView(this);
                temperatureText.setText(String.valueOf(temperatureMeasure[i]));
                temperatureText.setTypeface(null, Typeface.BOLD);
                tableRow.addView(temperatureText);
                tableLayout.addView(tableRow);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lastMeasureUpdated=false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic);
        goBackButton= findViewById(R.id.go_back_button);
        getHistoricDataButton = findViewById(R.id.get_historic_data_button);

        insolationText= findViewById(R.id.sun_historic_text);
        timeText= findViewById(R.id.time_historic_text);

        tableLayout= findViewById(R.id.table_layout);
        editText=findViewById(R.id.text_number);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canBeClicked==true) finish();
            }
        });

        getHistoricDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canBeClicked == true) {
                    if (numberCorrect() == false) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle("Nieprawidłowe dane")
                                .setMessage("Wprowadź liczbę od 1 do 144.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    } else {
                        canBeClicked = false;
                        getHistoricDataButton.setBackgroundColor(getResources().getColor(R.color.yellow));
                        getHistoricDataButton.setText("Pobieram...");

                        getHistoricDataButton.setEnabled(false);
                        goBackButton.setEnabled(false);
                        getHistoricDataButton.setClickable(false);
                        goBackButton.setClickable(false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(context, "Pobieram dane...", Toast.LENGTH_LONG);
                                        toast.show();
                                        getHistoricData();

                                    }
                                });
                            }
                        }).start();
                    }

                }
            }
        });



    }
}
