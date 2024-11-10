#include "Adafruit_TinyUSB.h" // Fix https://github.com/adafruit/Adafruit_nRF52_Arduino/issues/653

//int sensorPin = 9; // external, Grove D4
int sensorPin = 7; // onboard, user button

void setup() { // called once
  //pinMode(sensorPin, INPUT); // external
  pinMode(sensorPin, INPUT_PULLUP); // onboard
  Serial.begin(115200);
}

void loop() { // called in a loop
  int value = digitalRead(sensorPin);
  Serial.println(value);
  delay(100); // ms
}
