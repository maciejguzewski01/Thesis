package com.example.thesis;

import static java.lang.Thread.sleep;
import java.util.zip.CRC32;

public class Communication {
    static private class CurrentDataMeas{
        int groundMoistureMeasure=-1;
        int airMoistureMeasureMeasure=-1;
        int temperatureMeasure=-1;
        int insolationMeasure=-1;
        long timeMeasure=-1;
        int info;
    }
    static private CurrentDataMeas currentDataMeas=new CurrentDataMeas();
    static private long tempTime=-1;

    static public int getGroundMoistureMeasure()
    {
        return currentDataMeas.groundMoistureMeasure;
    }

    static public int getAirMoistureMeasureMeasure()
    {
        return currentDataMeas.airMoistureMeasureMeasure;
    }

    static public int getTemperatureMeasure()
    {
        return currentDataMeas.temperatureMeasure;
    }

    static public int getInsolationMeasure()
    {
        return currentDataMeas.insolationMeasure;
    }

    static public int getInfo()
    {
        return currentDataMeas.info;
    }

    static public long getTime()
    {
        return currentDataMeas.timeMeasure;
    }



    static private byte[] outgoingMessage;

    static private void generateMessageDataRequest(boolean isCurrentMessage,int historicDataSize)
    {
        outgoingMessage = new byte[12];
        outgoingMessage[0]=12; //size
        outgoingMessage[1]=0;  //size

        long currentTime= System.currentTimeMillis()/1000;
        tempTime=currentTime;

        outgoingMessage[2]= (byte)((currentTime >> 24) & 0xFF); //time
        outgoingMessage[3]= (byte)((currentTime >> 16) & 0xFF); //time
        outgoingMessage[4]= (byte)((currentTime >> 8) & 0xFF); //time
        outgoingMessage[5]= (byte)(currentTime & 0xFF); //time

         if(isCurrentMessage==true)
         {
             outgoingMessage[6]=(byte)224;//message type
             outgoingMessage[7]=0;//message type
         }
         else
         {
             outgoingMessage[6]=(byte)128;//message type
             outgoingMessage[7]=(byte) historicDataSize;//message type
         }


        CRC32 crc32 = new CRC32();
        for(int i=0;i<8;i++) crc32.update(outgoingMessage[i]);
        long checksum=crc32.getValue();;
        crc32.reset();

        outgoingMessage[8]= (byte)((checksum >> 24) & 0xFF); //checksum
        outgoingMessage[9]= (byte)((checksum >> 16) & 0xFF); //checksum
        outgoingMessage[10]= (byte)((checksum >> 8) & 0xFF); //checksum
        outgoingMessage[11]= (byte)(checksum & 0xFF); //checksum
    }



    static private void setIncomingCurrentData(int[] data)
    {
        currentDataMeas.timeMeasure=tempTime;
        currentDataMeas.info=data[12];
        currentDataMeas.groundMoistureMeasure=data[13];
        currentDataMeas.airMoistureMeasureMeasure=data[15];
        currentDataMeas.temperatureMeasure=data[14];

        int dayPassed=currentDataMeas.info;
        dayPassed= (dayPassed>>7)&1;
        if(dayPassed==1) currentDataMeas.insolationMeasure=data[16];
        else currentDataMeas.insolationMeasure=-1;

    }

    static private boolean dataCame=false;
    static public boolean haveDataCame()
    {
        if(dataCame==true)
        {
            dataCame=false;
            return true;
        }

        return false;
    }


    static private boolean checkChecksum(int[] data)
    {
        long incomingChecksum=0;
        incomingChecksum |=(long) data[8]<<24 & 0xFF000000;
        incomingChecksum |=(long) data[9]<<16 & 0x00FF0000;
        incomingChecksum |=(long) data[10]<<8 & 0x0000FF00;
        incomingChecksum |=(long) data[11] & 0x000000FF;

        CRC32 crc32 = new CRC32();
        for(int i=0;i<8;i++) crc32.update(data[i]);

        if(data[1]==0)
        {
            if(data[0]>12) for(int i=12;i<data[0];i++) crc32.update(data[i]);
        }
        else
        {
            int tempSize=0;
            tempSize |= data[0]<<8 & 0x0000FF00;
            tempSize |= data[1] & 0x000000FF;
            for(int i=12;i<tempSize;i++) crc32.update(data[i]);
        }

        long calculatedChecksum=crc32.getValue();;
        crc32.reset();


        if(incomingChecksum==calculatedChecksum) return true;


        return false;

    }


    static public void getLatestData()
    {
        generateMessageDataRequest(true, 0);
        final int[][] data = {null};
        final int[] counter = {0};
        boolean finish=false;

        while(finish==false)
         {
                data[0] = BluetoothConnection.readData();
                if (data[0] == null)
                {
                    BluetoothConnection.sendData(outgoingMessage);
                }
                else
                {
                    if(checkChecksum(data[0])==false) continue;
                    dataCame=true;
                    setIncomingCurrentData(data[0]);
                    finish=true;
                    return;
                }

                if(counter[0]==10)
                {
                    dataCame=false;
                    finish=true;
                    return;
                }
                counter[0]++;
             try {
                 sleep(2000);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }

    }


    static public boolean sendNorms()
    {
        byte[] outgoingNorms;
        outgoingNorms= new byte[19];

        outgoingNorms[0]=19; //size
        outgoingNorms[1]=0;  //size

        long currentTime= System.currentTimeMillis()/1000;

        outgoingNorms[2]= (byte)((currentTime >> 24) & 0xFF); //time
        outgoingNorms[3]= (byte)((currentTime >> 16) & 0xFF); //time
        outgoingNorms[4]= (byte)((currentTime >> 8) & 0xFF); //time
        outgoingNorms[5]= (byte)(currentTime & 0xFF); //time

        outgoingNorms[6]=(byte)192;//message type
        outgoingNorms[7]=0;//message type

        outgoingNorms[12]=(byte)Norms.getGroundHumidityMin();
        outgoingNorms[13]=(byte)Norms.getGroundHumidityMax();
        outgoingNorms[14]=(byte)Norms.getTemperatureMin();
        outgoingNorms[15]=(byte)Norms.getTemperatureMax();
        outgoingNorms[16]=(byte)Norms.getAirHumidityMin();
        outgoingNorms[17]=(byte)Norms.getAirHumidityMax();
        outgoingNorms[18]=(byte)Norms.getInsolation();

        CRC32 crc32 = new CRC32();
        for(int i=0;i<8;i++) crc32.update(outgoingNorms[i]);
        for(int i=12;i<19;i++) crc32.update(outgoingNorms[i]);
        long checksum=crc32.getValue();;
        crc32.reset();

        outgoingNorms[8]= (byte)((checksum >> 24) & 0xFF); //checksum
        outgoingNorms[9]= (byte)((checksum >> 16) & 0xFF); //checksum
        outgoingNorms[10]= (byte)((checksum >> 8) & 0xFF); //checksum
        outgoingNorms[11]= (byte)(checksum & 0xFF); //checksum


        final int[][] data = {null};
        final int[] counter = {0};
        boolean finish=false;

        while(finish==false)
        {
            data[0] = BluetoothConnection.readData();
            if (data[0] == null)
            {
                BluetoothConnection.sendData(outgoingNorms);
            }
            else
            {
                if(checkChecksum(data[0])==false) continue;
                dataCame=true;
                setIncomingCurrentData(data[0]);
                finish=true;
                return true;
            }

            if(counter[0]==10)
            {
                dataCame=false;
                finish=true;
                return false;
            }
            counter[0]++;
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;

    }


    static private class HistoricDataMeas{

        static int insolationMeasure=-1;
        static long timeMeasure=-1;
        static int info;
        static int dataNumber=-1;
        static int[] groundMoistureMeasure;
        static int[] airMoistureMeasureMeasure;
        static int[] temperatureMeasure;
    }
    static private HistoricDataMeas historicDataMeas=new HistoricDataMeas();


    static private void setIncomingHistoricData(int[] data)
    {
        int incomingHistoricDataSize= 0;
        if(data[1]==0) incomingHistoricDataSize=data[0];
        else
        {
            incomingHistoricDataSize |= data[0]<<8 & 0x0000FF00;
            incomingHistoricDataSize |= data[1] & 0x000000FF;
        }
        incomingHistoricDataSize=incomingHistoricDataSize-12-2;
        int numberOfHistoricMeasure=data[7];
        HistoricDataMeas.groundMoistureMeasure = new int[numberOfHistoricMeasure];
        HistoricDataMeas.airMoistureMeasureMeasure = new int[numberOfHistoricMeasure];
        HistoricDataMeas.temperatureMeasure = new int[numberOfHistoricMeasure];

        HistoricDataMeas.timeMeasure=tempTime;
        HistoricDataMeas.info=data[12];

        int dayPassed=HistoricDataMeas.info;
        dayPassed= (dayPassed>>7)&1;
        if(dayPassed==1) HistoricDataMeas.insolationMeasure=data[16];
        else HistoricDataMeas.insolationMeasure=-1;

        HistoricDataMeas.dataNumber=numberOfHistoricMeasure;
        HistoricDataMeas.groundMoistureMeasure[0]=data[13];
        HistoricDataMeas.airMoistureMeasureMeasure[0]=data[15];
        HistoricDataMeas.temperatureMeasure[0]=data[14];

        for(int i=1;i<numberOfHistoricMeasure;i++)
        {

                HistoricDataMeas.groundMoistureMeasure[i]=data[17+(3*(i-1))];
                HistoricDataMeas.airMoistureMeasureMeasure[i]=data[19+(3*(i-1))];
                HistoricDataMeas.temperatureMeasure[i]=data[18+(3*(i-1))];
        }


    }

    static public int[] getHistoricGroundMoistureMeasure()
    {
        return HistoricDataMeas.groundMoistureMeasure;
    }

    static public int[] getHistoricAirMoistureMeasureMeasure()
    {
        return HistoricDataMeas.airMoistureMeasureMeasure;
    }

    static public int[] getHistoricTemperatureMeasure()
    {
        return HistoricDataMeas.temperatureMeasure;
    }

    static public int getHistoricInsolationMeasure()
    {
        return HistoricDataMeas.insolationMeasure;
    }

    static public int getHistoricInfo()
    {
        return HistoricDataMeas.info;
    }

    static public long getHistoricTime()
    {
        return HistoricDataMeas.timeMeasure;
    }

    static public int getNumberOfHistoricMeasure()
    {
        return HistoricDataMeas.dataNumber;
    }


    static public void getHistoricData(int numberOfHistoricData)
    {
        generateMessageDataRequest(false, numberOfHistoricData);

        final int[][] data = {null};
        final int[] counter = {0};
        boolean finish=false;

        while(finish==false) {
            data[0] = BluetoothConnection.readData();
            if (data[0] == null) {
                BluetoothConnection.sendData(outgoingMessage);
            } else {
                if (checkChecksum(data[0]) == false) continue;
                dataCame = true;
                setIncomingHistoricData(data[0]);
                finish = true;
                return;
            }

            if (counter[0] == 10) {
                dataCame = false;
                finish = true;
                return;
            }
            counter[0]++;
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
