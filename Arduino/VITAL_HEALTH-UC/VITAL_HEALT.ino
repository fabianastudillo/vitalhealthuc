#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

int ledPin = 5;
int Boton = 0;
int valor = 0;
int contador = 0;
int estadoanteriorboton = 0;

//-----------------------------FRECUENCIA CARDIACA----------------------------//
#define PLOTT_DATA 
#define MAX_BUFFER 100

uint32_t prevData[MAX_BUFFER];
uint32_t sumData=0;
uint32_t maxData=0;
uint32_t avgData=0;
uint32_t roundrobin=0;
uint32_t countData=0;
uint32_t period=0;
uint32_t lastperiod=0;
uint32_t millistimer=millis();
double frequency;
int beatspermin=0;
uint32_t newData;
int bpm = 0;

void freqDetec() {
  if (countData==MAX_BUFFER) {
   if (prevData[roundrobin] < avgData*1.45 && newData >= avgData*1.15){ 
    period = millis()-millistimer;
    millistimer = millis();
    maxData = 0;
   }
  }
 roundrobin++;
 if (roundrobin >= MAX_BUFFER) {
    roundrobin=0;
 }
 if (countData<MAX_BUFFER) {
    countData++;
    sumData+=newData;
 } else {
    sumData+=newData-prevData[roundrobin];
 }
 avgData = sumData/countData;
 if (newData>maxData) {
  maxData = newData;
 }
#ifdef PLOTT_DATA
  Serial.print(newData);
 Serial.print("\t");
 Serial.print(avgData);
 Serial.print("\t");
 Serial.print(avgData*1.5);
 Serial.print("\t");
 Serial.print(maxData);
 Serial.print("\t");
 Serial.println(beatspermin);
#endif
 prevData[roundrobin] = newData;//store previous value
}

//------------------------------BLE-------------------------------------------//
BLECharacteristic *pCharacteristic;

bool deviceConnected = false;
std::string rxValue;

#define SERVICE_UUID           "00002a37-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID_TX "00002a37-0000-1000-8000-00805f9b34fb"


class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

//---------------------------VARIABLES ECG-----------------------------------//
int SENSOR = 36;          //Pin usado para el ADC
int ECG = 0;        //Inicio para EMA Y

//I-II
float EMA_ALPHA_LOW_I = 0.4;
float EMA_ALPHA_HIGH_I = 0.09;
int EMA_SIGNAL_LOW_I = 0;
int EMA_SIGNAL_HIGH_I = 0;

int BANDPASS_I = 0;
int STOPBAND_I = 0;

//III
float EMA_ALPHA_LOW_III = 0.9;
float EMA_ALPHA_HIGH_III = 0.8;

int EMA_SIGNAL_LOW_III = 0;
int EMA_SIGNAL_HIGH_III = 0;

int HIGHPASS_III = 0;
int BANDPASS_III = 0;

//aVR
float EMA_ALPHA_LOW_aVR = 0.5;
float EMA_ALPHA_HIGH_aVR = 0.09;

int EMA_SIGNAL_LOW_aVR = 0;
int EMA_SIGNAL_HIGH_aVR = 0;

int HIGHPASS_aVR = 0;
int BANDPASS_aVR = 0;

//aVL-aVF
float EMA_ALPHA_LOW_aVL = 0.8;
float EMA_ALPHA_HIGH_aVL = 0.6;

int EMA_SIGNAL_LOW_aVL = 0;
int EMA_SIGNAL_HIGH_aVL = 0;

int HIGHPASS_aVL = 0;
int BANDPASS_aVL = 0;

//V1
float EMA_ALPHA_LOW_V1 = 0.88;
float EMA_ALPHA_HIGH_V1 = 0.9;

int EMA_SIGNAL_LOW_V1 = 0;
int EMA_SIGNAL_HIGH_V1 = 0;

int HIGHPASS_V1 = 0;
int BANDPASS_V1 = 0;
int STOPBAND_V1 = 0;

//V2
float EMA_ALPHA_LOW_V2 = 0.55;  //
float EMA_ALPHA_HIGH_V2 = 0.92;//


int LOWPASS_V2 = 0;
int EMA_SIGNAL_LOW_V2 = 0;
int EMA_SIGNAL_HIGH_V2 = 0;

int BANDPASS_V2 = 0;
int STOPBAND_V2 = 0;

//V3
float EMA_ALPHA_LOW_V3 = 0.55;  //
float EMA_ALPHA_HIGH_V3 = 0.6;//

int EMA_SIGNAL_LOW_V3 = 0;
int EMA_SIGNAL_HIGH_V3 = 0;

int LOWPASS_V3 = 0;
int BANDPASS_V3 = 0;
int STOPBAND_V3 = 0;

//V4
float EMA_ALPHA_LOW_V4 = 1.5;
float EMA_ALPHA_HIGH_V4 = 0.4;

int EMA_SIGNAL_LOW_V4 = 0;
int EMA_SIGNAL_HIGH_V4 = 0;

int HIGHPASS_V4 = 0;
int BANDPASS_V4 = 0;
int STOPBAND_V4 = 0;

//V5
float EMA_ALPHA_LOW_V5 = 1.2;
float EMA_ALPHA_HIGH_V5 = 0.4;

int EMA_SIGNAL_LOW_V5 = 0;
int EMA_SIGNAL_HIGH_V5 = 0;

int HIGHPASS_V5 = 0;
int BANDPASS_V5 = 0;
int STOPBAND_V5 = 0;

//V6
float EMA_ALPHA_LOW_V6 = 1.1;
float EMA_ALPHA_HIGH_V6 = 0.6;

int EMA_SIGNAL_LOW_V6 = 0;
int EMA_SIGNAL_HIGH_V6 = 0;

int HIGHPASS_V6 = 0;
int BANDPASS_V6 = 0;
int STOPBAND_V6 = 0;

//BPM

float EMA_ALPHA_LOW_BPM = 0.55;  //                  
float EMA_ALPHA_HIGH_BPM = 0.92;//                     

int EMA_SIGNAL_LOW_BPM = 0;          
int EMA_SIGNAL_HIGH_BPM = 0;

int HIGHPASS_BPM = 0;
int LOWPASS_BPM = 0;
int BANDPASS_BPM = 0;
int STOPBAND_BPM = 0;
//---------------------------------------------SETUP----------------------------------------------//

void setup() {

  //------------------------------------INICIALIZACION BLE----------------------------------------//
  Serial.begin(115200);

  Serial.println(F("Vital Health UC..."));

  // Create the BLE Device
  BLEDevice::init("Vital Health UC ECG"); // Give it a name

  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );

  pCharacteristic->addDescriptor(new BLE2902());

//  pCharacteristic->setCallbacks(new MyCallbacks());

  // Start the service
  pService->start();

  // Start advertising
  pServer->getAdvertising()->start();
  Serial.println("Waiting a client connection to notify...");

  //--------------------------------DECLARACION DE ENTRADAS DE FILTROS--------------------------------//

  //I-II
  EMA_SIGNAL_LOW_I = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_I = analogRead(SENSOR);
  //III
  EMA_SIGNAL_LOW_III = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_III = analogRead(SENSOR);
  //aVR
  EMA_SIGNAL_LOW_aVR = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_aVR = analogRead(SENSOR);
  //aVL-aVF
  EMA_SIGNAL_LOW_aVL = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_aVL = analogRead(SENSOR);
  //V1
  EMA_SIGNAL_LOW_V1 = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_V1 = analogRead(SENSOR);
  //V2
  EMA_SIGNAL_LOW_V2 = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_V2 = analogRead(SENSOR);
  //V3
  EMA_SIGNAL_LOW_V3 = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_V3 = analogRead(SENSOR);
  //V4
  EMA_SIGNAL_LOW_V4 = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_V4 = analogRead(SENSOR);
  //V5
  EMA_SIGNAL_LOW_V5 = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_V5 = analogRead(SENSOR);
  //V6
  EMA_SIGNAL_LOW_V6 = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_V6 = analogRead(SENSOR);
  //BPM
  EMA_SIGNAL_LOW_BPM = analogRead(SENSOR);
  EMA_SIGNAL_HIGH_BPM = analogRead(SENSOR);

}

//-------------------------------------------LOOP----------------------------------------------------//
void loop() {
  if (deviceConnected) {

    //----------------------------------------VARIABLES------------------------------------------------//
    ECG = analogRead(SENSOR);

    //BPM

    EMA_SIGNAL_LOW_BPM = (EMA_ALPHA_LOW_BPM*ECG) + ((1-EMA_ALPHA_LOW_BPM)*EMA_SIGNAL_LOW_BPM);         
    EMA_SIGNAL_HIGH_BPM = (EMA_ALPHA_HIGH_BPM*ECG) + ((1-EMA_ALPHA_HIGH_BPM)*EMA_SIGNAL_HIGH_BPM);

    HIGHPASS_BPM = ECG - EMA_SIGNAL_LOW_BPM; 
    LOWPASS_BPM = EMA_SIGNAL_LOW_BPM; 
    BANDPASS_BPM = EMA_SIGNAL_HIGH_BPM - EMA_SIGNAL_LOW_BPM;       
    STOPBAND_BPM = ECG - BANDPASS_BPM;

        //--------------------------------------IMPRESION DE DATOS------------------------------------//
        /*char texto[120];
        snprintf(texto, 120, "I %d II %d III %d aVR %d aVL %d aVF %d V1 %d V2 %d V3 %d V4 %d V5 %d V6 %d", ECG, STOPBAND_I + 2500, HIGHPASS_III + 6000, BANDPASS_aVR + 7000, BANDPASS_aVL+ 8500, HIGHPASS_aVL+ 9500, HIGHPASS_V1 + 10500, EMA_SIGNAL_HIGH_V2 + 9500, LOWPASS_V3 + 10500, BANDPASS_V4 + 13500, BANDPASS_V5 + 14500, BANDPASS_V6 + 15500);
        Serial.println(texto);
        pCharacteristic->setValue(texto);
        pCharacteristic->notify(texto);  */   

        char texto[32];

        valor = digitalRead(Boton);
        digitalWrite(ledPin, valor);
        if(valor != estadoanteriorboton){
        if(valor == 1){
        contador++;
        
        if (contador == 13) {
        contador = 0;
        }
        }
        }
        estadoanteriorboton = valor;

     switch (contador)
        {
        case 1:

          //I        
          snprintf(texto, 32, "G%dM%d I;%d$", ECG, beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 2:

          //I-II
          EMA_SIGNAL_LOW_I = (EMA_ALPHA_LOW_I*ECG) + ((1-EMA_ALPHA_LOW_I)*EMA_SIGNAL_LOW_I);         
          EMA_SIGNAL_HIGH_I = (EMA_ALPHA_HIGH_I*ECG) + ((1-EMA_ALPHA_HIGH_I)*EMA_SIGNAL_HIGH_I);
          BANDPASS_I = EMA_SIGNAL_HIGH_I - EMA_SIGNAL_LOW_I;       
          STOPBAND_I = ECG - BANDPASS_I;
          
          snprintf(texto, 32, "G%dM%d II;%d$", STOPBAND_I, beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 3:

          //III
          EMA_SIGNAL_LOW_III = (EMA_ALPHA_LOW_III*ECG) + ((1-EMA_ALPHA_LOW_III)*EMA_SIGNAL_LOW_III);          
          EMA_SIGNAL_HIGH_III = (EMA_ALPHA_HIGH_III*ECG) + ((1-EMA_ALPHA_HIGH_III)*EMA_SIGNAL_HIGH_III);
          HIGHPASS_III = ECG - EMA_SIGNAL_LOW_III;         
          
          snprintf(texto, 32, "G%dM%d III;%d$", HIGHPASS_III + 1800 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 4:

           //aVR
          EMA_SIGNAL_LOW_aVR = (EMA_ALPHA_LOW_aVR*ECG) + ((1-EMA_ALPHA_LOW_aVR)*EMA_SIGNAL_LOW_aVR);          
          EMA_SIGNAL_HIGH_aVR = (EMA_ALPHA_HIGH_aVR*ECG) + ((1-EMA_ALPHA_HIGH_aVR)*EMA_SIGNAL_HIGH_aVR);
          BANDPASS_aVR = EMA_SIGNAL_HIGH_aVR - EMA_SIGNAL_LOW_aVR;  
          
          snprintf(texto, 32, "G%dM%d aVR;%d$", BANDPASS_aVR + 1800 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 5:
          
          //aVL
          EMA_SIGNAL_LOW_aVL = (EMA_ALPHA_LOW_aVL*ECG) + ((1-EMA_ALPHA_LOW_aVL)*EMA_SIGNAL_LOW_aVL);          
          EMA_SIGNAL_HIGH_aVL = (EMA_ALPHA_HIGH_aVL*ECG) + ((1-EMA_ALPHA_HIGH_aVL)*EMA_SIGNAL_HIGH_aVL);
          BANDPASS_aVL = EMA_SIGNAL_HIGH_aVL - EMA_SIGNAL_LOW_aVL; 
          
          snprintf(texto, 32, "G%dM%d aVL;%d$", BANDPASS_aVL + 1800 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 6:

          //aVF
          EMA_SIGNAL_LOW_aVL = (EMA_ALPHA_LOW_aVL*ECG) + ((1-EMA_ALPHA_LOW_aVL)*EMA_SIGNAL_LOW_aVL);          
          EMA_SIGNAL_HIGH_aVL = (EMA_ALPHA_HIGH_aVL*ECG) + ((1-EMA_ALPHA_HIGH_aVL)*EMA_SIGNAL_HIGH_aVL);      
          HIGHPASS_aVL = ECG - EMA_SIGNAL_LOW_aVL;  
          
          snprintf(texto, 32, "G%dM%d aVF;%d$", HIGHPASS_aVL + 1800 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 7:

           //V1
          EMA_SIGNAL_LOW_V1 = (EMA_ALPHA_LOW_V1*ECG) + ((1-EMA_ALPHA_LOW_V1)*EMA_SIGNAL_LOW_V1);         
          EMA_SIGNAL_HIGH_V1 = (EMA_ALPHA_HIGH_V1*ECG) + ((1-EMA_ALPHA_HIGH_V1)*EMA_SIGNAL_HIGH_V1);     
          HIGHPASS_V1 = ECG*5 - EMA_SIGNAL_LOW_V1*5;
          BANDPASS_V1 = EMA_SIGNAL_HIGH_V1 - EMA_SIGNAL_LOW_V1;       
          STOPBAND_V1 = ECG - BANDPASS_V1;  
          
          snprintf(texto, 32, "G%dM%d V1;%d$", HIGHPASS_V1 + 1800 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 8:

          //V2
          EMA_SIGNAL_LOW_V2 = (EMA_ALPHA_LOW_V2*ECG) + ((1-EMA_ALPHA_LOW_V2)*EMA_SIGNAL_LOW_V2);         
          EMA_SIGNAL_HIGH_V2 = (EMA_ALPHA_HIGH_V2*ECG) + ((1-EMA_ALPHA_HIGH_V2)*EMA_SIGNAL_HIGH_V2);
      
          LOWPASS_V2 = EMA_SIGNAL_LOW_V2; 
          BANDPASS_V2 = EMA_SIGNAL_HIGH_V2 - EMA_SIGNAL_LOW_V2;       
          STOPBAND_V2 = ECG - BANDPASS_V2;
          
          snprintf(texto, 32, "G%dM%d V2;%d$", STOPBAND_V2, beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 9:

          //V3
          EMA_SIGNAL_LOW_V3 = (EMA_ALPHA_LOW_V3*ECG) + ((1-EMA_ALPHA_LOW_V3)*EMA_SIGNAL_LOW_V3);         
          EMA_SIGNAL_HIGH_V3 = (EMA_ALPHA_HIGH_V3*ECG) + ((1-EMA_ALPHA_HIGH_V3)*EMA_SIGNAL_HIGH_V3);
      
          LOWPASS_V3 = EMA_SIGNAL_LOW_V3; 
          BANDPASS_V3 = EMA_SIGNAL_HIGH_V3 - EMA_SIGNAL_LOW_V3;       
          STOPBAND_V3 = ECG - BANDPASS_V3;
          
          snprintf(texto, 32, "G%dM%d V3;%d$", STOPBAND_V3, beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 10:

          //V4
          EMA_SIGNAL_LOW_V4 = (EMA_ALPHA_LOW_V4*ECG) + ((1-EMA_ALPHA_LOW_V4)*EMA_SIGNAL_LOW_V4);          
          EMA_SIGNAL_HIGH_V4 = (EMA_ALPHA_HIGH_V4*ECG) + ((1-EMA_ALPHA_HIGH_V4)*EMA_SIGNAL_HIGH_V4);
      
          HIGHPASS_V4 = ECG - EMA_SIGNAL_LOW_V4; 
          BANDPASS_V4 = EMA_SIGNAL_HIGH_V4 - EMA_SIGNAL_LOW_V4;
          STOPBAND_V4 = ECG - BANDPASS_V4;
          
          snprintf(texto, 32, "G%dM%d V4;%d$", STOPBAND_V4 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 11:

          //V5
          EMA_SIGNAL_LOW_V5 = (EMA_ALPHA_LOW_V5*ECG) + ((1-EMA_ALPHA_LOW_V5)*EMA_SIGNAL_LOW_V5);          
          EMA_SIGNAL_HIGH_V5 = (EMA_ALPHA_HIGH_V5*ECG) + ((1-EMA_ALPHA_HIGH_V5)*EMA_SIGNAL_HIGH_V5);
      
          HIGHPASS_V5 = ECG - EMA_SIGNAL_LOW_V5; 
          BANDPASS_V5 = EMA_SIGNAL_HIGH_V5 - EMA_SIGNAL_LOW_V5;
          STOPBAND_V5 = ECG - BANDPASS_V5;
          
          snprintf(texto, 32, "G%dM%d V5;%d$", STOPBAND_V5 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        
          case 12:

          //V6
          EMA_SIGNAL_LOW_V6 = (EMA_ALPHA_LOW_V6*ECG) + ((1-EMA_ALPHA_LOW_V6)*EMA_SIGNAL_LOW_V6);          
          EMA_SIGNAL_HIGH_V6 = (EMA_ALPHA_HIGH_V6*ECG) + ((1-EMA_ALPHA_HIGH_V6)*EMA_SIGNAL_HIGH_V6);
      
          HIGHPASS_V6 = ECG - EMA_SIGNAL_LOW_V6; 
          BANDPASS_V6 = EMA_SIGNAL_HIGH_V6 - EMA_SIGNAL_LOW_V6;
          STOPBAND_V6 = ECG - BANDPASS_V6;
          
          snprintf(texto, 32, "G%dM%d V6;%d$", STOPBAND_V6 , beatspermin, ECG );
          pCharacteristic->setValue(texto);
          pCharacteristic->notify(texto);
          break;
        }
        
        newData = STOPBAND_BPM;
            freqDetec();
            if (period!=lastperiod) {
               frequency = 1000/(double)period;//timer rate/period
               if (frequency*60 > 20 && frequency*60 < 200) { // supress unrealistic Data
                beatspermin=frequency*60;
                #ifndef PLOTT_DATA
                Serial.print(frequency);
                Serial.print(" hz");
                Serial.print(" ");
                Serial.print(beatspermin);
                Serial.println(" bpm");
                #endif
                  lastperiod=period;
     }
  }
  delay(25);
  }
}
