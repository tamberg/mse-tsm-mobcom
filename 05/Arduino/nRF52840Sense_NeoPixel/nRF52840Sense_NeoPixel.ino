#include "Adafruit_NeoPixel.h"

Adafruit_NeoPixel pixel = Adafruit_NeoPixel(1, PIN_NEOPIXEL, NEO_GRB + NEO_KHZ800);

void setup() {
  pixel.begin();
  uint32_t color = pixel.Color(127, 0, 127);
  pixel.setPixelColor(0, color);
  pixel.show();
}

void loop() {
    // skip
}
