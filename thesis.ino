//definicje wartosci domyślnych oraz uzywanych pinow
#define defaultTemperatureMin 17
#define defaultTemperatureMax 30
#define defaultGroundHumidityMin 40
#define defaultGroundHumidityMax 80 
#define defaultAirHumidityMin 50
#define defaultAirHumidityMax 75
#define defaultInsolation 50
#define PinFotoresistor A5
#define PinGroundHum A0
#define PinDHT 7
#define numberOfMeasPerDay 144
#define PinVccGroundMoistureSensor 8
#define PinVccFotoresistor 9
#define PinVccDht 10


//biblioteki
#include <EEPROM.h>
#include "DHT.h"
#include <Narcoleptic.h>
#include <CRC32.h>


unsigned long time=0;
unsigned long timeOfLastMeasurement=0;

CRC32 crc;

//ustawiane wartosci graniczne parametrow
byte groundHumidityConfigMin;
byte groundHumidityConfigMax;
byte temperatureConfigMin;
byte temperatureConfigMax;
byte airHumidityConfigMin;
byte airHumidityConfigMax;
byte insolationConfig;

//wyniki pomiarow
byte groundHumidityMeasurement[numberOfMeasPerDay];
byte temperatureMeasurement[numberOfMeasPerDay];
byte airHumidityMeasurement[numberOfMeasPerDay];
byte insolationMeasurement[numberOfMeasPerDay];

bool dayPassed;
byte measCount;
byte lastIdx;
byte numberOfHistoricMeasurmentsInMemory;

//informacje o przekroczeniu parametru
bool groundHumidityExceeded;
bool temperatureExceeded;
bool airHumidityExceeded;
bool insolationExceeded;

DHT dht;


uint8_t incomingMessage[20]; 
uint8_t outgoingMessage[20];


//inicjalizacja parametrow 
void setup() {
  pinMode(PinDHT, INPUT_PULLUP);
  dht.setup(PinDHT);

  pinMode(PinVccGroundMoistureSensor,OUTPUT);
  pinMode(PinVccFotoresistor,OUTPUT);
  pinMode(PinVccDht,OUTPUT);


  digitalWrite(PinVccGroundMoistureSensor,LOW);
  digitalWrite(PinVccFotoresistor, LOW);
  digitalWrite(PinVccDht, LOW);


 
 Serial.begin(9600);
 reset();
  
  //wczytanie danych z pamieci EEPROM lub ustawienie domyslnych
  if(checkConfig()) { readDataFromEeprom(); } 
  else 
   {
     groundHumidityConfigMin = defaultGroundHumidityMin;
     groundHumidityConfigMax = defaultGroundHumidityMax;
     temperatureConfigMin = defaultTemperatureMin;
     temperatureConfigMax = defaultTemperatureMax;
     airHumidityConfigMin = defaultAirHumidityMin;
     airHumidityConfigMax = defaultAirHumidityMax;
     insolationConfig = defaultInsolation;
   }

   delay(1000);
   measurement();
   time=time+1;
   timeOfLastMeasurement=time;
}

void reset()
{
 resetResults();

 measCount=0;
 lastIdx=0;
 numberOfHistoricMeasurmentsInMemory=0;

 resetIncomingMessage();
 resetOutgoingMessage();

 groundHumidityExceeded=false;
 temperatureExceeded=false;
 airHumidityExceeded=false;
 insolationExceeded=false;
 
}

//resetuje wyniki pomiarow
void resetResults()
{
  for(size_t i=0;i<numberOfMeasPerDay;++i)
  {
    insolationMeasurement[i]=0;
    groundHumidityMeasurement[i]=0;
    temperatureMeasurement[i]=0;
    airHumidityMeasurement[i]=0;
  }
 measCount=0;
 lastIdx=0;
 numberOfHistoricMeasurmentsInMemory=0;

  dayPassed=false;
}

//sprawdza czy w pamieci EEPROM jest konfiguracja
bool checkConfig()
{
  byte answer = EEPROM.read(0);
  if(answer==0b10101010) return true;
  return false; 
}

//odczytuje konfiguracje z EEPROM
void readDataFromEeprom()
{
     groundHumidityConfigMin = EEPROM.read(1);
     groundHumidityConfigMax = EEPROM.read(2);
     temperatureConfigMin = EEPROM.read(3);
     temperatureConfigMax = EEPROM.read(4);
     airHumidityConfigMin = EEPROM.read(5);
     airHumidityConfigMax = EEPROM.read(6);
     insolationConfig =  EEPROM.read(7);
}

//Restuje wiadomosci
void resetIncomingMessage()
{
  for(size_t i=0;i<20;++i){ incomingMessage[i]=0; }
}
void resetOutgoingMessage()
{
  for(size_t i=0;i<20;++i){outgoingMessage[i]=0; }
}

//wczytanie przychodzacej konfiguracji
void configuration()
{
  
  groundHumidityConfigMin =(byte)incomingMessage[12];
  groundHumidityConfigMax =(byte)incomingMessage[13];
  temperatureConfigMin = (byte)incomingMessage[14];
  temperatureConfigMax = (byte)incomingMessage[15];
  airHumidityConfigMin = (byte)incomingMessage[16];
  airHumidityConfigMax = (byte)incomingMessage[17];
  insolationConfig =  (byte)incomingMessage[18];

 groundHumidityExceeded=false;
 temperatureExceeded=false;
 airHumidityExceeded=false;
 insolationExceeded=false;

  EEPROM.write(0, 0b10101010);
  EEPROM.write(1, groundHumidityConfigMin);
  EEPROM.write(2, groundHumidityConfigMax);
  EEPROM.write(3, temperatureConfigMin);
  EEPROM.write(4, temperatureConfigMax);
  EEPROM.write(5, airHumidityConfigMin);
  EEPROM.write(6, airHumidityConfigMax);
  EEPROM.write(7, insolationConfig);
}

//pobiera pomiary z czujnikow
void measurement()
{
  
 if(measCount==numberOfMeasPerDay)
 {
  measCount=0;
 }
  
  digitalWrite(PinVccGroundMoistureSensor,HIGH);
  digitalWrite(PinVccFotoresistor, HIGH);
  digitalWrite(PinVccDht, HIGH);

  delay(1000);

  long tempGround=analogRead(PinGroundHum);
  long tempInsolation=analogRead(PinFotoresistor);
  int tempTemperature=dht.getTemperature();
  int tempHumidity=dht.getHumidity();

  digitalWrite(PinVccGroundMoistureSensor,LOW);
  digitalWrite(PinVccFotoresistor, LOW);
  digitalWrite(PinVccDht, LOW);
  tempGround=(tempGround*100)/1024;
  tempGround=100-tempGround;//because 100== min hum, 0 ==max
  tempInsolation=(tempInsolation*100)/1024;

  insolationMeasurement[measCount]=static_cast<byte>(tempInsolation);
  groundHumidityMeasurement[measCount]=static_cast<byte>(tempGround);
  temperatureMeasurement[measCount]=static_cast<byte>(tempTemperature);
  airHumidityMeasurement[measCount]=static_cast<byte>(tempHumidity);

  checkMeasurement(measCount);
  lastIdx=measCount;
  measCount++;
  if(measCount==numberOfMeasPerDay) dayPassed=true;
  if(numberOfHistoricMeasurmentsInMemory<numberOfMeasPerDay)numberOfHistoricMeasurmentsInMemory++;
  
}

//ustawia parametry przekroczenia norm
void checkMeasurement(byte lastCountIndex)
{
  if((groundHumidityConfigMin>groundHumidityMeasurement[lastCountIndex])||(groundHumidityMeasurement[lastCountIndex]>groundHumidityConfigMax)) groundHumidityExceeded=true;
  if((temperatureConfigMin>temperatureMeasurement[lastCountIndex])||(temperatureMeasurement[lastCountIndex]>temperatureConfigMax)) temperatureExceeded=true;
  if((airHumidityConfigMin>airHumidityMeasurement[lastCountIndex])||(airHumidityMeasurement[lastCountIndex]>airHumidityConfigMax)) airHumidityExceeded=true;
  if(dayPassed&&(getMediumInsolation()<insolationConfig)) insolationExceeded=true;
}

//sprawdza czy ktoras norma jest przekroczona
bool checkNorms()
{
  if(groundHumidityExceeded ||temperatureExceeded || airHumidityExceeded ||insolationExceeded)
  {
    return true;
  } 
  return false;
}

//oblicza srednie naslonecznienie
byte getMediumInsolation()
{
  int temp=0;
  for(size_t i=0; i<numberOfMeasPerDay;++i)
  {
    temp=temp+insolationMeasurement[i];
  }
  return temp/numberOfMeasPerDay;
}

//generuje naglowek wiadomosci bez sumy kontrolnej
void generateHeader(int dataSize, bool dataType)//1- current, 0- historic
{
  if(dataSize<=243)outgoingMessage[0]=12+dataSize;
  else
  {
  int size = dataSize+12;
  outgoingMessage[0]= (size >> 8) & 0xFF;
  outgoingMessage[1]= size & 0xFF;
  }
  
  outgoingMessage[2]= (time >> 24) & 0xFF;
  outgoingMessage[3]= (time >> 16) & 0xFF;
  outgoingMessage[4]= (time >> 8) & 0xFF;
  outgoingMessage[5]= time & 0xFF;


  if(dataType==1) outgoingMessage[6]= 0b01100000;
  else outgoingMessage[6]= 0b00000000;

  if(dataType==1)outgoingMessage[7]=0;
}

//generuje sume kontrolna i wpisuje ja do wiadomosci
void generateChecksum(int dataSize)
{
  for(size_t i=0;i<8;++i)
  {
    crc.update(outgoingMessage[i]);
  }
  for(size_t i=12;i<12+dataSize;++i)
  {
    crc.update(outgoingMessage[i]);
  }

  uint32_t checksum = crc.finalize();
  crc.reset();

  outgoingMessage[8]= (checksum >> 24) & 0xFF;
  outgoingMessage[9]= (checksum >> 16) & 0xFF;
  outgoingMessage[10]= (checksum >> 8) & 0xFF;
  outgoingMessage[11]= checksum & 0xFF;
}

void generatePartOfMessageWithCurrentData()
{
  outgoingMessage[12]=createFirstByteOfData();
  outgoingMessage[13]=groundHumidityMeasurement[lastIdx];
  outgoingMessage[14]=temperatureMeasurement[lastIdx];
  outgoingMessage[15]=airHumidityMeasurement[lastIdx];
  if(dayPassed) outgoingMessage[16]=(getMediumInsolation());
  else outgoingMessage[16]=0;
}


//generuje wiadomosc wychodzaca z ostanim pomiarem
void generateMessageWithLatestData()
{
  resetOutgoingMessage();
  generateHeader(5,1);
  generatePartOfMessageWithCurrentData();

  generateChecksum(5);
}


//wysyla wiadomosc z ostanimi pomiarami
void sendLatestData()
{
  generateMessageWithLatestData();
  for(size_t i=0;i<17;++i)
  {
   Serial.print(outgoingMessage[i]);
   Serial.print(" ");
   Serial.flush();
  }
}

uint32_t generateHistoricChecksum(int historicDataSize)
{
  crc.reset();
  for(size_t i=0;i<8;++i)
  {
    crc.update(outgoingMessage[i]);
  }
  for(size_t i=12;i<17;++i)
  {
    crc.update(outgoingMessage[i]);
  }

  if(historicDataSize>1)
  {
    int index=0;

    for(int i=1;i<historicDataSize;i++)
    {
      if(lastIdx-i>=0) index=lastIdx-i;
      else index=143-(i-lastIdx);

      crc.update(groundHumidityMeasurement[index]);
      crc.update(temperatureMeasurement[index]);
      crc.update(airHumidityMeasurement[index]);
    }

  }

  uint32_t checksum = crc.finalize();
  crc.reset();

  return checksum;
}

//wysyla wiadomosc z historycznymi pomiarami
void sendHistoricData()
{
  resetOutgoingMessage();
  byte numberOfHistoricMeasure=(byte)incomingMessage[7];
  if(numberOfHistoricMeasure>144) numberOfHistoricMeasure=144;
  if(numberOfHistoricMeasure>numberOfHistoricMeasurmentsInMemory) numberOfHistoricMeasure=numberOfHistoricMeasurmentsInMemory;

  int numberOfHistoricBytes=3*(numberOfHistoricMeasure-1);
  generateHeader(numberOfHistoricBytes+5,0);
  outgoingMessage[7]=numberOfHistoricMeasure;
  generatePartOfMessageWithCurrentData();

  uint32_t checksum=generateHistoricChecksum(numberOfHistoricMeasure);
  outgoingMessage[8]= (checksum >> 24) & 0xFF;
  outgoingMessage[9]= (checksum >> 16) & 0xFF;
  outgoingMessage[10]= (checksum >> 8) & 0xFF;
  outgoingMessage[11]= checksum & 0xFF;
  
  for(size_t i=0;i<17;++i)
  {
    Serial.print(outgoingMessage[i]);
    Serial.print(" ");
    Serial.flush();
  }

  int index=0;
  for(int i=1;i<numberOfHistoricMeasure;i++)
  {
    if(lastIdx-i>=0) index=lastIdx-i;
    else index=143-(i-lastIdx);

    outgoingMessage[13]=groundHumidityMeasurement[index];
    outgoingMessage[14]=temperatureMeasurement[index];
    outgoingMessage[15]=airHumidityMeasurement[index];

    Serial.print(outgoingMessage[13]);
    Serial.print(" ");
    Serial.flush();
    Serial.print(outgoingMessage[14]);
    Serial.print(" ");
    Serial.flush();
    Serial.print(outgoingMessage[15]);
    Serial.print(" ");
    Serial.flush();
  }
}


//tworzy pierwszy bajt danych w wychodzacej wiadomosci
byte createFirstByteOfData()
{
  bool byteOne[8];
  byte answer=0;
  for(size_t i=0;i<8;++i) byteOne[i]=0;
  
  byteOne[0]=dayPassed;

  if(checkNorms()==true) byteOne[1]=1; 
  if(groundHumidityExceeded==true) byteOne[2]=1;
  if(temperatureExceeded==true) byteOne[3]=1;
  if(airHumidityExceeded==true) byteOne[4]=1;
  if(insolationExceeded==true) byteOne[5]=1;

  for(int i=0;i<8;i++)
   {
      answer |= (byteOne[i]<<(7-i));
   }
   return answer;
}

//aktualizuje czas
void updateTime()
{
  long temp = time - timeOfLastMeasurement;
  time=0;

time |= (uint32_t)incomingMessage[2] << 24;
time |= (uint32_t)incomingMessage[3] << 16;
time |= (uint32_t)incomingMessage[4] << 8;
time |= incomingMessage[5];
 
 timeOfLastMeasurement = time -temp;
}

//sprawdza sumę kontrolną odebranej wiadomosci
bool checksumCorrect()
{
  uint32_t incomingChecksum=0;
  incomingChecksum |= (uint32_t)incomingMessage[8]<<24;
  incomingChecksum |= (uint32_t)incomingMessage[9]<<16;
  incomingChecksum |= (uint32_t)incomingMessage[10]<<8;
  incomingChecksum |= incomingMessage[11];
crc.reset();
  for(size_t i=0;i<8;++i)
  {
    crc.update(incomingMessage[i]);
  }

if(incomingMessage[0]>12)
{
  for(size_t i=12;i<incomingMessage[0];++i)
  {
    crc.update(incomingMessage[i]);
  }
}

  uint32_t calculatedChecksum = crc.finalize();
  crc.reset();

  if(incomingChecksum==calculatedChecksum) return true;
  return false;

}

//reaguje na odebrana wiadomosc przychodzaca
void handleRecivedMessage()
{
  
  if(checksumCorrect()==false) return;

  updateTime();
  long start=millis();
  if(incomingMessage[6]==0b11100000)
  {
    sendLatestData();
  } 
  else if(incomingMessage[6]==0b10000000)
  {
    sendHistoricData();
  } 
  else if(incomingMessage[6]==0b11000000)
  {
    configuration();
    sendLatestData();
  } 
  long end=millis();
  time=time+((end-start)/1000);
}

void loop() {

  Narcoleptic.delay(5000);
  long start=millis();
  delay(1000);
  if((timeOfLastMeasurement+600)<=time)
  {
    measurement();
    timeOfLastMeasurement=time;
  }

  long end=millis();

   time=time+5+((end-start)/1000);

}

//funkcja wywyolywana po loop jesli dostepne sa dane na Serialu
void serialEvent()
{
  while(Serial.available())
  {
    Serial.readBytes(incomingMessage,20);
  }
  
  handleRecivedMessage();

}
