// Scanner BLE central. Copyright (c) Thomas Amberg, FHNW

// Based on https://github.com/adafruit/Adafruit_nRF52_Arduino
// /tree/master/libraries/Bluefruit52Lib/examples/Central
// Copyright (c) Adafruit.com, all rights reserved.

// Licensed under the MIT license, see LICENSE or
// https://choosealicense.com/licenses/mit/

#include <bluefruit.h>

BLEClientService clientHeartRateMonitorService(UUID16_SVC_HEART_RATE);
BLEClientCharacteristic clientHeartRateMeasurementCharacteristic(UUID16_CHR_HEART_RATE_MEASUREMENT);
BLEClientCharacteristic clientBodySensorLocationCharacteristic(UUID16_CHR_BODY_SENSOR_LOCATION);

void scanCallback(ble_gap_evt_adv_report_t* report) {
  Bluefruit.Central.connect(report);
}

void connectCallback(uint16_t conn_handle) {
  Serial.println("Connected");
  Serial.print("HRM service ... ");
  if (!clientHeartRateMonitorService.discover(conn_handle)) {
    Serial.println("not found.");
    Bluefruit.disconnect(conn_handle);
    return;
  }
  Serial.println("found.");
  
  Serial.print("Heart Rate Measurement characteristic ... ");
  if (!clientHeartRateMeasurementCharacteristic.discover()) {
    Serial.println("not found.");
    Bluefruit.disconnect(conn_handle);
    return;
  }
  Serial.println("found.");

  Serial.print("Body Sensor Location characteristic ... ");
  if (clientBodySensorLocationCharacteristic.discover()) { // optional
    Serial.print("found: ");
    const char* body_str[] = { "Other", "Chest", "Wrist", "Finger", "Hand", "Ear Lobe", "Foot" };
    uint8_t loc_value = clientBodySensorLocationCharacteristic.read8();   
    Serial.println(body_str[loc_value]);
  } else {
    Serial.println("not found.");
  }

  Serial.print("Heart Rate Measurement characteristic notifications ... ");
  if (clientHeartRateMeasurementCharacteristic.enableNotify()) {
    Serial.println("enabled.");
  } else {
    Serial.println("failed.");
  }
}

void disconnectCallback(uint16_t conn_handle, uint8_t reason) {
  (void) conn_handle;
  (void) reason;
  Serial.print("Disconnected, reason = 0x");
  Serial.println(reason, HEX);
}

void hrmNotifyCallback(BLEClientCharacteristic* chr, uint8_t* data, uint16_t len) {
  Serial.print("Heart Rate Measurement: ");
  if (data[0] & 0x01) { // 16-bit measurement
    uint16_t value;
    memcpy(&value, &data[1], 2);
    Serial.println(value);
  } else { // 8-bit measurement
    Serial.println(data[1]);
  }
}

void setup() {
  Serial.begin(115200);
  while (!Serial) { delay(10); }

  Bluefruit.begin(0, 1); // max 1 connection as central, saves memory
  Bluefruit.setName("nRF52840 Central");
  Bluefruit.setConnLedInterval(250);

  clientHeartRateMonitorService.begin(); // sequence matters
  clientBodySensorLocationCharacteristic.begin(); // added to clientHeartRateMonitorService
  clientHeartRateMeasurementCharacteristic.setNotifyCallback(hrmNotifyCallback);
  clientHeartRateMeasurementCharacteristic.begin(); // added to clientHeartRateMonitorService

  Bluefruit.Central.setConnectCallback(connectCallback);
  Bluefruit.Central.setDisconnectCallback(disconnectCallback);

  Bluefruit.Scanner.setRxCallback(scanCallback);
  Bluefruit.Scanner.restartOnDisconnect(true);
  Bluefruit.Scanner.setInterval(160, 80); // in unit of 0.625 ms
  Bluefruit.Scanner.filterUuid(clientHeartRateMonitorService.uuid);
  Bluefruit.Scanner.useActiveScan(false);
  Bluefruit.Scanner.start(0); // non-stop
}

void loop() {
  // skip
}
