# Android nRF Toolbox Hygrometer

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
* Test with the 
