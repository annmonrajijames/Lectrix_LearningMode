# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

# If CMAKE_DISABLE_SOURCE_CHANGES is set to true and the source directory is an
# existing directory in our source tree, calling file(MAKE_DIRECTORY) on it
# would cause a fatal error, even though it would be a no-op.
if(NOT EXISTS "C:/Users/annmon.james/esp/v5.4/esp-idf/components/bootloader/subproject")
  file(MAKE_DIRECTORY "C:/Users/annmon.james/esp/v5.4/esp-idf/components/bootloader/subproject")
endif()
file(MAKE_DIRECTORY
  "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader"
  "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix"
  "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix/tmp"
  "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix/src/bootloader-stamp"
  "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix/src"
  "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix/src/bootloader-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix/src/bootloader-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "C:/Lectrix_Annmon/Git_projects/Lectrix_LearningMode/ESP32_Hardware/softAP/build/bootloader-prefix/src/bootloader-stamp${cfgdir}") # cfgdir has leading slash
endif()
