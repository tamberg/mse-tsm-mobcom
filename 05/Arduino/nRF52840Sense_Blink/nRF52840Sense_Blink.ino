//int ledPin = 9; // external, Grove D4
//int ledPin = 4; // onboard, blue LED
int ledPin = 13; // onboard, red LED

void setup() { // called once
  pinMode(ledPin, OUTPUT);
}

void loop() { // called in a loop
  digitalWrite(ledPin, HIGH); // on
  delay(500); // ms
  digitalWrite(ledPin, LOW); // off
  delay(500); // ms
}
