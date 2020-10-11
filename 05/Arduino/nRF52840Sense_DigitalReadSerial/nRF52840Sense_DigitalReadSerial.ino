//int sensorPin = 9; // external, Grove D4
int sensorPin = 7; // onboard, user button

void setup() { // called once
  //pinMode(sensorPin, INPUT); // external
  pinMode(sensorPin, INPUT_PULLUP);
  Serial.begin(9600);
}

void loop() { // called in a loop
  int value = digitalRead(pin);
  Serial.println(value);
  delay(100); // ms
}
