# Android nRF Toolbox Hygrometer

## Prerequisites
[Feather nRF52840 Sense](https://github.com/tamberg/mse-tsm-mobcom/wiki/Feather-nRF52840-Sense) with [nRF52840Sense_HygrometerBlePeripheral.ino](../../../06/Arduino/nRF52840Sense_HygrometerBlePeripheral/nRF52840Sense_HygrometerBlePeripheral.ino) running.

## Get the repository
Get the https://github.com/NordicSemiconductor/Android-nRF-Toolbox project<sup>*</sup>.

    $ git clone https://github.com/NordicSemiconductor/Android-nRF-Toolbox

<sup>*</sup>Side-by-side with this repository, not inside.

## Apply the patch
Apply the [0001-Added-hygrometer.patch](0001-Added-hygrometer.patch) patch.

    $ cd Android-nRF-Toolbox
    $ git am --3way 0001-Added-hygrometer.patch

## Build the project
*Android Studio > Build > Make Project*

## Run the project
* *Android Studio > Run > Run 'app'*
