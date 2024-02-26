// ESS BLE peripheral. Copyright (c) Thomas Amberg, FHNW

// Based on https://github.com/adafruit/Adafruit_nRF52_Arduino
// /tree/master/libraries/Bluefruit52Lib/examples/Peripheral
// Copyright (c) Adafruit.com, all rights reserved.

// Licensed under the MIT license, see LICENSE or
// https://choosealicense.com/licenses/mit/

#include "Adafruit_TinyUSB.h" // Fix https://github.com/adafruit/Adafruit_nRF52_Arduino/issues/653
#include <bluefruit.h>
#include "Adafruit_SHT31.h"

// Bluetooth SIG Base UUID:
// 0x0000????-0000-1000-8000-00805F9B34FB

// ESS peripheral, 16-bit UUIDs
// 0x181A ESS Service // See https://www.bluetooth.com/specifications/specs/environmental-sensing-service-1-0/ => ESS_V1.0.0.pdf
//   0x2A6E Temperature Chr. [R, N] // See https://btprodspecificationrefs.blob.core.windows.net/assigned-values/16-bit%20UUID%20Numbers%20Document.pdf
//     0x2901 Characteristic User Description Descr. (optional, not implemented)
//     0x2906 Valid Range Descr. (optional, not implemented)
//     0x290B Environmental Sensing Configuration Descr. (optional, not implemented)
//     0x290C Environmental Sensing Measurement Descr. (optional, implemented, indoor)
//     0x290D Environmental Sensing Trigger Setting Descr. (optional, not implemented)
//   0x2A6F Humidity Chr. [R, N] // See https://btprodspecificationrefs.blob.core.windows.net/assigned-values/16-bit%20UUID%20Numbers%20Document.pdf
//     0x290C Environmental Sensing Measurement Descr. (optional, implemented, indoor)
//   0x2A6F Humidity Chr. [R, N] // See https://btprodspecificationrefs.blob.core.windows.net/assigned-values/16-bit%20UUID%20Numbers%20Document.pdf
//     0x290C Environmental Sensing Measurement Descr. (optional, implemented, outdoor)

// The arrays below are ordered "least significant byte first" (lsb):
uint8_t const essServiceUuid[] = { // 0x181A
  0xFB, 0x34, 0x9B, 0x5F, 0x80, 0x00, 0x00, 0x80, 
  0x00, 0x10, 0x00, 0x00, 0x1A, 0x18, 0x00, 0x00 };
uint8_t const tempCharacteristicUuid[] = { // 0x2A6E
  0xFB, 0x34, 0x9B, 0x5F, 0x80, 0x00, 0x00, 0x80, 
  0x00, 0x10, 0x00, 0x00, 0x6E, 0x2A, 0x00, 0x00 };
uint8_t const humiCharacteristicUuid[] = { // 0x2A6F
  0xFB, 0x34, 0x9B, 0x5F, 0x80, 0x00, 0x00, 0x80, 
  0x00, 0x10, 0x00, 0x00, 0x6F, 0x2A, 0x00, 0x00 };
uint8_t const envSensingMeasurementDescriptorUuid[] = { // 0x290C
  0xFB, 0x34, 0x9B, 0x5F, 0x80, 0x00, 0x00, 0x80, 
  0x00, 0x10, 0x00, 0x00, 0x0C, 0x29, 0x00, 0x00 };
uint8_t const envSensingMeasurementDescriptorIndoor[] = { // See ESS_V1.0.0.pdf
  0x00, 0x00, // Flags, reserved for future use: 0x0000 (lsb)
  0x01, // Sampling functions, instant.: 0x01 (or ... or unspecified: 0x00)
  0x00, 0x00, 0x00, // Measurement period, not in use: 0x000000 (lsb)
  0x01, 0x00, 0x00, // Update interval s, not in use: 0x000000 (lsb)
  0x14, // Application, indoor: 0x14 (or ... or unspecified: 0x00)
  0xFF // Measurement uncertainty, information not available: 0xFF
};
uint8_t const envSensingMeasurementDescriptorOutdoor[] = { // See ESS_V1.0.0.pdf
  0x00, 0x00, // Flags, reserved for future use: 0x0000 (lsb)
  0x01, // Sampling functions, instant.: 0x01 (or ... or unspecified: 0x00)
  0x00, 0x00, 0x00, // Measurement period, not in use: 0x000000 (lsb)
  0x01, 0x00, 0x00, // Update interval s, not in use: 0x000000 (lsb)
  0x13, // Application, outdoor: 0x13 (or ... or unspecified: 0x00)
  0xFF // Measurement uncertainty, information not available: 0xFF
};

Adafruit_SHT31 sht31 = Adafruit_SHT31();
BLEService essService = BLEService(essServiceUuid);
BLECharacteristic tempCharacteristic = BLECharacteristic(tempCharacteristicUuid);
BLECharacteristic humiCharacteristic = BLECharacteristic(humiCharacteristicUuid);
BLECharacteristic humiCharacteristic2 = BLECharacteristic(humiCharacteristicUuid);

void connectedCallback(uint16_t connectionHandle) {
  char centralName[32] = { 0 };
  BLEConnection *connection = Bluefruit.Connection(connectionHandle);
  connection->getPeerName(centralName, sizeof(centralName));
  Serial.print(connectionHandle);
  Serial.print(", connected to ");
  Serial.print(centralName);
  Serial.println();
}

void disconnectedCallback(uint16_t connectionHandle, uint8_t reason) {
  Serial.print(connectionHandle);
  Serial.print(" disconnected, reason = ");
  Serial.println(reason); // see https://github.com/adafruit/Adafruit_nRF52_Arduino
  // /blob/master/cores/nRF5/nordic/softdevice/s140_nrf52_6.1.1_API/include/ble_hci.h
  Serial.println("Advertising ...");
}

void setupEssService() {
  essService.begin(); // Must be called before calling .begin() on its characteristics

  tempCharacteristic.setProperties(CHR_PROPS_READ | CHR_PROPS_NOTIFY);
  tempCharacteristic.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  tempCharacteristic.setFixedLen(2);
  tempCharacteristic.begin();
  tempCharacteristic.addDescriptor(
    envSensingMeasurementDescriptorUuid,
    envSensingMeasurementDescriptorIndoor,
    sizeof(envSensingMeasurementDescriptorIndoor),
    SECMODE_OPEN, SECMODE_NO_ACCESS);

  humiCharacteristic.setProperties(CHR_PROPS_READ | CHR_PROPS_NOTIFY);
  humiCharacteristic.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  humiCharacteristic.setFixedLen(2);
  humiCharacteristic.begin();
  humiCharacteristic.addDescriptor(
    envSensingMeasurementDescriptorUuid,
    envSensingMeasurementDescriptorIndoor,
    sizeof(envSensingMeasurementDescriptorIndoor),
    SECMODE_OPEN, SECMODE_NO_ACCESS);

  humiCharacteristic2.setProperties(CHR_PROPS_READ | CHR_PROPS_NOTIFY);
  humiCharacteristic2.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  humiCharacteristic2.setFixedLen(2);
  humiCharacteristic2.begin();
  humiCharacteristic2.addDescriptor(
    envSensingMeasurementDescriptorUuid,
    envSensingMeasurementDescriptorOutdoor,
    sizeof(envSensingMeasurementDescriptorOutdoor),
    SECMODE_OPEN, SECMODE_NO_ACCESS);
}

void startAdvertising() {
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();
  Bluefruit.Advertising.addService(essService);
  Bluefruit.Advertising.addName();

  // See https://developer.apple.com/library/content/qa/qa1931/_index.html   
  const int fastModeInterval = 32; // * 0.625 ms = 20 ms
  const int slowModeInterval = 244; // * 0.625 ms = 152.5 ms
  const int fastModeTimeout = 30; // s
  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(fastModeInterval, slowModeInterval);
  Bluefruit.Advertising.setFastTimeout(fastModeTimeout);
  // 0 = continue advertising after fast mode, until connected
  Bluefruit.Advertising.start(0);
  Serial.println("Advertising ...");
}

void setup() {
  Serial.begin(115200);
  while (!Serial) { delay(10); } // only if usb connected
  Serial.println("Setup");

  sht31.begin(0x44);
  sht31.heater(true);

  Bluefruit.begin();
  Bluefruit.setName("nRF52840");
  Bluefruit.Periph.setConnectCallback(connectedCallback);
  Bluefruit.Periph.setDisconnectCallback(disconnectedCallback);

  setupEssService();
  startAdvertising();
}

void loop() {
  if (Bluefruit.connected()) {
    float indoorTemp = sht31.readTemperature();
    int t = (int) (indoorTemp * 100.0f); // fixed precision
    uint8_t tHi = (uint8_t) (t >> 8);
    uint8_t tLo = (uint8_t) (t >> 0);
    uint8_t tData[2] = { tLo, tHi }; // lsb
    if (tempCharacteristic.notify(tData, sizeof(tData))) {
      Serial.print("Notified, indoor temperature = ");
      Serial.println(indoorTemp);
    }
    
    float indoorHumi = sht31.readHumidity();
    int h1 = (int) (indoorHumi * 100.0f); // fixed precision
    uint8_t h1Hi = (uint8_t) (h1 >> 8);
    uint8_t h1Lo = (uint8_t) (h1 >> 0);
    uint8_t h1Data[2] = { h1Lo, h1Hi }; // lsb
    if (humiCharacteristic.notify(h1Data, sizeof(h1Data))) {
      Serial.print("Notified, indoor humidity = ");
      Serial.println(indoorHumi);
    }

    int a = analogRead(A0); // quasi random, 0-1024
    float outdoorHumi = map(a, 0, 1024, 0, 10000) / 100.0f;
    int h2 = (int) (outdoorHumi * 100.0f); // fixed precision
    uint8_t h2Hi = (uint8_t) (h2 >> 8);
    uint8_t h2Lo = (uint8_t) (h2 >> 0);
    uint8_t h2Data[2] = { h2Lo, h2Hi }; // lsb
    if (humiCharacteristic2.notify(h2Data, sizeof(h2Data))) {
      Serial.print("Notified, outdoor humidity = ");
      Serial.println(outdoorHumi);
    }
  }
  delay(1000); // ms
}
