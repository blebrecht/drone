#!/usr/bin/env python
import rospy
from sensores_prueba.msg import DHT
from sensores_prueba.msg import DatosAnemometro
from geometry_msgs.msg import Vector3Stamped
from sensor_msgs.msg import BatteryState
from sensor_msgs.msg import NavSatFix
from sensor_msgs.msg import TimeReference
from std_msgs.msg import String
from dji_osdk_ros.msg import FCTimeInUTC
from dji_osdk_ros.msg import GPSUTC

from datetime import datetime
import pytz

from influxdb_client import InfluxDBClient
from influxdb_client.client.write_api import ASYNCHRONOUS

bucket = "dron" # database
organization = ""
token = ""
client = InfluxDBClient(url="http://localhost:8086", token=token, org=organization)
write_api = client.write_api(write_options=ASYNCHRONOUS)

# Variables de datos
temperatura = 0
temperaturaAnem = 0
humedad = 0
humedadAnem = 0
velocidadViento = 0
velocidadX = 0
velocidadY = 0
velocidadZ = 0
porcentajeBateria = 0
altitud = 0
latitud = 0
longitud = 0

# EN ESTA NUEVA VERSION CADA CALLBACK ALMACENA VARIABLES GLOBALES PARA LUEGO EN EL CALLBACK DEL TIEMPO
# ALMACENAR TODO EN LA BASE DE DATOS

def callback(data):
    global temperatura
    temperatura = data.temperatura
    global humedad
    humedad = data.humedad
    rospy.loginfo("RECIBIENDO: "+str(temperatura)+"T "+str(humedad)+"H")
    
def callbackAnemometro(data):
    global velocidadViento
    velocidadViento = data.velocidadViento
    global temperaturaAnem
    temperaturaAnem = data.temperatura
    global humedadAnem
    humedadAnem = data.humedad
    rospy.loginfo("RECIBIENDO: Velocidad Viento: "+str(velocidadViento)+"  Temperatura: "+str(temperaturaAnem)+"  Humedad: "+str(humedadAnem))
    
def callbackVelocidad(data):
    global velocidadX
    velocidadX = data.vector.x
    global velocidadY
    velocidadY = data.vector.y
    global velocidadZ
    velocidadZ = data.vector.z
    #rospy.loginfo("RECIBIENDO VELOCIDAD DRON: "+str(vx)+" "+str(vy)+" "+str(vz))
    
def callbackBateria(data):
    global porcentaje
    porcentaje = data.percentage
	
def callbackGPS(data):
    global altitud
    altitud = data.altitude
    global latitud
    latitud = data.latitude
    global longitud
    longitud = data.longitude
    rospy.loginfo("RECIBIENDO ALTURA DRON: "+str(altitud)+" LATITUD: "+str(latitud)+" LONGITUD: "+str(longitud))
    
def callbackTiempo(data):
    timeDesdeStart = str(data.fc_timestamp_us)
    timeFecha = str(data.fc_utc_yymmdd)
    timeHora = str(data.fc_utc_hhmmss)
    millis = timeDesdeStart[len(timeDesdeStart)-6:len(timeDesdeStart)]

    rospy.loginfo("RECIBIENDO TIEMPO INT: "+str(timeDesdeStart)+ " "+str(timeFecha)+" "+str(timeHora))
    timeFechaStr = "20"+timeFecha[0:2]+"-"+timeFecha[2:4]+"-"+timeFecha[4:6]+"T"
    timeHoraStr = timeHora[0:2]+":"+timeHora[2:4]+":"+timeHora[4:6]+"."+millis+"Z"
    timeFinalStr = timeFechaStr+timeHoraStr

    dateTimeUTC = datetime.strptime(timeFinalStr, '%Y-%m-%dT%H:%M:%S.%fZ')
    dateTimeLocal = dateTimeUTC.replace(tzinfo=pytz.UTC).astimezone(tz=pytz.timezone('America/Santiago'))

    current_time = dateTimeLocal.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
    
	# Guardar los datos
    write_api.write(bucket,organization, [{"measurement": "datos_telemetria", "tags": {}, "fields": {"altitud": altitud, "latitud": latitud, "longitud": longitud, "porcentaje_bateria": porcentaje, "velocidad_x": velocidadX, "velocidad_y": velocidadY, "velocidad_z": velocidadZ}, "time": current_time}])
    #write_api.write(bucket,organization, [{"measurement": "datos_dht", "tags": {}, "fields": {"temperatura": temperatura, "humedad": humedad}, "time": current_time}])
    write_api.write(bucket,organization, [{"measurement": "datos_anemometro", "tags": {}, "fields": {"temperatura": temperaturaAnem, "humedad": humedadAnem
    , "velocidad_viento": velocidadViento}, "time": current_time}])
        

    
def listener():
    rospy.init_node('sub_sensores', anonymous=True)
    #rospy.Subscriber("datos_dht", DHT, callback)
    rospy.Subscriber("datos_anemometro", DatosAnemometro, callbackAnemometro)
    rospy.Subscriber("dji_osdk_ros/velocity", Vector3Stamped, callbackVelocidad)
    rospy.Subscriber("dji_osdk_ros/battery_state", BatteryState, callbackBateria)
    rospy.Subscriber("dji_osdk_ros/gps_position", NavSatFix, callbackGPS)
    rospy.Subscriber("dji_osdk_ros/time_sync_fc_time_utc", FCTimeInUTC, callbackTiempo)
    rospy.spin()

if __name__ == '__main__':
    listener()
