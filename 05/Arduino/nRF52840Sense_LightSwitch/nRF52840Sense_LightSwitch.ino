#include "Adafruit_TinyUSB.h"; // Fix https://github.com/adafruit/Adafruit_nRF52_Arduino/issues/653

int buttonPin = 7; // onboard button
int ledPin = 4; // onboard, blue LED

void setup() {
  pinMode(buttonPin, INPUT_PULLUP);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(115200);
}

void loop() {
  int state = digitalRead(buttonPin);
  Serial.println(state);
  if (state == LOW) { // inverted, active low
    digitalWrite(ledPin, HIGH); // on
  } else {
    digitalWrite(ledPin, LOW); // off
  }
  delay(1);
}
