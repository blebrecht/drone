#!/bin/bash

nmcli d wifi hotspot ifname wlan0 ssid RED password PASSWORD
export ROS_MASTER_URI=http://10.42.0.1:11311
export ROS_HOSTNAME=10.42.0.1
source /home/ubuntu/ros/devel/setup.sh
roslaunch sensores_prueba sensores_prueba.launch

