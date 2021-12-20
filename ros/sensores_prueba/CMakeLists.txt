cmake_minimum_required(VERSION 3.0.2)
project(sensores_prueba)

find_package(catkin REQUIRED COMPONENTS roscpp std_msgs message_generation)

add_message_files(
  FILES

  DHT.msg
  DatosAnemometro.msg
)

generate_messages(
   DEPENDENCIES
   std_msgs
)

catkin_install_python(PROGRAMS sub_sensores.py pub_anemometro.py
  DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION}
)

catkin_package()

include_directories(${catkin_INCLUDE_DIRS})

add_executable(pub_dht pub_dht.cpp)
target_link_libraries(pub_dht ${catkin_LIBRARIES} -lwiringPi)
add_dependencies(pub_dht ${catkin_EXPORTED_TARGETS})
