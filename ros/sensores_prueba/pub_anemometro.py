#!/usr/bin/env python
import rospy
import serial
from sensores_prueba.msg import DatosAnemometro


ser = None


def getDatos():
    line = ""
    while True:
      for c in ser.read():
        if chr(c) == " ":
          line = line+";" 
          continue
        
        line = line+chr(c)  
          
        if chr(c) == "\n":
          if len(line) > 10:
            line = line.replace(";;",";")
            array = line.split(";")
            windSpeed = float(array[1])
            temperatura = float(array[11])
            humedad = float(array[13])
            
            datosAnemometro = DatosAnemometro()
            datosAnemometro.velocidadViento = windSpeed
            datosAnemometro.temperatura = temperatura
            datosAnemometro.humedad = humedad
            return datosAnemometro
                 

def configSerial():
    global ser
    ser = serial.Serial(
    port='/dev/ttyUSB0',\
    baudrate=115200,\
    parity=serial.PARITY_NONE,\
    stopbits=serial.STOPBITS_ONE,\
    bytesize=serial.EIGHTBITS,\
        timeout=0)


def publisher():
    configSerial()
    pub = rospy.Publisher('datos_anemometro', DatosAnemometro, queue_size=100)
    rospy.init_node('pub_anemometro', anonymous=True)
    rate = rospy.Rate(5)
    
    while not rospy.is_shutdown():
        datosAnemometro = getDatos()
        if datosAnemometro is not None:
          pub.publish(datosAnemometro)
        
        rate.sleep()
    
    ser.close()


if __name__ == '__main__':
    publisher()
