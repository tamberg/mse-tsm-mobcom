#include "Adafruit_TinyUSB.h"; // Fix https://github.com/adafruit/Adafruit_nRF52_Arduino/issues/653
#include "Adafruit_SHT31.h"

const int buttonPin = 7; // onboard user button
const int redLedPin = LED_RED; // onboard red LED
const int blueLedPin = LED_BLUE; // onboard blue LED

float threshold;
int t0;

int state = 0;

Adafruit_SHT31 sht31 = Adafruit_SHT31();

int pressed(int value) {
    return value == LOW; // inverted due to pull-up
}

void setup() {
  Serial.begin(115200);
  pinMode(redLedPin, OUTPUT);
  pinMode(blueLedPin, OUTPUT);
  digitalWrite(redLedPin, LOW);
  digitalWrite(blueLedPin, LOW);
  pinMode(buttonPin, INPUT_PULLUP); // pull-up
  sht31.begin(0x44);
  sht31.heater(true);
}

void loop() {
  int b = digitalRead(buttonPin);
  float h = sht31.readHumidity();
  Serial.print(b);
  Serial.print(", ");
  Serial.print(state);
  Serial.print(", ");
  Serial.println(h);
  if (state == 0 && pressed(b)) {
    threshold = h + 10.0; // %
    digitalWrite(redLedPin, HIGH);
    state = 1;
  } else if (state == 1 && !pressed(b)) {
    state = 2;
  } else if (state == 2 && !isnan(h) && h > threshold) {
    digitalWrite(redLedPin, LOW);
    digitalWrite(blueLedPin, HIGH);
    state = 3;
  } else if (state == 3 && pressed(b)) {
    digitalWrite(redLedPin, HIGH);
    digitalWrite(blueLedPin, LOW);
    t0 = millis();
    state = 4;
  } else if (state == 4 && !pressed(b)) {
    state = 5;
  } else if (state == 5 && (millis() - t0) > 1000) {
    digitalWrite(redLedPin, LOW);
    state = 0;
  }
  delay(1);
}
