// Blinky BLE peripheral. Copyright (c) Thomas Amberg, FHNW. All rights reserved.

// Works with https://github.com/NordicSemiconductor/Android-nRF-Blinky

// Based on https://github.com/adafruit/Adafruit_nRF52_Arduino
// /tree/master/libraries/Bluefruit52Lib/examples/Peripheral
// Copyright (c) Adafruit.com, all rights reserved.

// Licensed under the MIT license, see LICENSE or
// https://choosealicense.com/licenses/mit/

#include "Adafruit_TinyUSB.h"; // Fix https://github.com/adafruit/Adafruit_nRF52_Arduino/issues/653
#include <bluefruit.h>

// Custom peripheral, use 128-bit UUIDs
// 00001523-1212-efde-1523-785feabcd123 Blinky Service
// 00001524-1212-efde-1523-785feabcd123   Button Chr. [R, N]
// 00001525-1212-efde-1523-785feabcd123   LED Chr. [R, W]

// The arrays below are ordered "least significant byte first":
uint8_t const blinkyServiceUuid[] = { 0x23, 0xd1, 0xbc, 0xea, 0x5f, 0x78, 0x23, 0x15, 0xde, 0xef, 0x12, 0x12, 0x23, 0x15, 0x00, 0x00 };
uint8_t const buttonCharacteristicUuid[] = { 0x23, 0xd1, 0xbc, 0xea, 0x5f, 0x78, 0x23, 0x15, 0xde, 0xef, 0x12, 0x12, 0x24, 0x15, 0x00, 0x00 };
uint8_t const ledCharacteristicUuid[] = { 0x23, 0xd1, 0xbc, 0xea, 0x5f, 0x78, 0x23, 0x15, 0xde, 0xef, 0x12, 0x12, 0x25, 0x15, 0x00, 0x00 };

int buttonPin = 7; // onboard button
int ledPin = LED_RED; // onboard LED

BLEService blinkyService = BLEService(blinkyServiceUuid);
BLECharacteristic buttonCharacteristic = BLECharacteristic(buttonCharacteristicUuid);
BLECharacteristic ledCharacteristic = BLECharacteristic(ledCharacteristicUuid);

int pressed(int value) {
    return value == LOW; // inverted due to pull-up
}

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

void cccdCallback(uint16_t connectionHandle, BLECharacteristic* characteristic, uint16_t cccdValue) {
  if (characteristic->uuid == buttonCharacteristic.uuid) {
    Serial.print("Button state 'Notify', ");
    if (characteristic->notifyEnabled()) {
      Serial.println("enabled");
    } else {
      Serial.println("disabled");
    }
  }
}

void writeCallback(uint16_t connectionHandle, BLECharacteristic* characteristic, uint8_t* data, uint16_t len) {
  if (characteristic->uuid == ledCharacteristic.uuid) {
    Serial.print("Heater State 'Write', LED ");
    bool state = data[0] != 0x00;
    digitalWrite(ledPin, state);
    Serial.println(state ? "on" : "off");
  }
}

void setupblinkyService() {
  blinkyService.begin(); // Must be called before calling .begin() on its characteristics

  buttonCharacteristic.setProperties(CHR_PROPS_READ | CHR_PROPS_NOTIFY);
  buttonCharacteristic.setPermission(SECMODE_OPEN, SECMODE_NO_ACCESS);
  buttonCharacteristic.setFixedLen(1);
  buttonCharacteristic.setCccdWriteCallback(cccdCallback);  // Optionally capture CCCD updates
  buttonCharacteristic.begin();

  ledCharacteristic.setProperties(CHR_PROPS_READ | CHR_PROPS_WRITE | CHR_PROPS_WRITE_WO_RESP);
  ledCharacteristic.setPermission(SECMODE_OPEN, SECMODE_OPEN);
  ledCharacteristic.setFixedLen(1);
  ledCharacteristic.setWriteCallback(writeCallback, true);
  ledCharacteristic.begin();
}

void startAdvertising() {
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();
  Bluefruit.Advertising.addService(blinkyService);
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

  pinMode(buttonPin, INPUT_PULLUP);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);

  Bluefruit.begin();
  Bluefruit.setName("nRF52840");
  Bluefruit.Periph.setConnectCallback(connectedCallback);
  Bluefruit.Periph.setDisconnectCallback(disconnectedCallback);

  setupblinkyService();
  startAdvertising();
}

void loop() {
  if (Bluefruit.connected()) {
    int value = digitalRead(buttonPin);
    int state = pressed(value);
    uint8_t bytes[1] = { (uint8_t) state };
    if (buttonCharacteristic.notify(bytes, sizeof(bytes))) {
      Serial.print("Notified, button state = ");
      Serial.println(state);
    } else {
      Serial.println("Notify not set, or not connected");
    }
  }
  delay(100); // ms
}
