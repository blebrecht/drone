#include <ros/ros.h>
#include <sensores_prueba/DHT.h>

#include <wiringPi.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
 
#define MAX_TIMINGS	85
#define DHT_PIN		7
 
int data[5] = { 0, 0, 0, 0, 0 };

struct DatosDHT{

	float temperatura;
	float humedad;
	bool error;
};

DatosDHT read_dht_data_test()
{
	DatosDHT datos = {20,40,false};
	return datos;
}
 
DatosDHT read_dht_data()
{
	uint8_t laststate	= HIGH;
	uint8_t counter		= 0;
	uint8_t j			= 0, i;
 
	data[0] = data[1] = data[2] = data[3] = data[4] = 0;
 
	/* pull pin down for 18 milliseconds */
	pinMode( DHT_PIN, OUTPUT );
	digitalWrite( DHT_PIN, LOW );
	delay( 18 );
 
	/* prepare to read the pin */
	pinMode( DHT_PIN, INPUT );
 
	/* detect change and read data */
	for ( i = 0; i < MAX_TIMINGS; i++ )
	{
		counter = 0;
		while ( digitalRead( DHT_PIN ) == laststate )
		{
			counter++;
			delayMicroseconds( 1 );
			if ( counter == 255 )
			{
				break;
			}
		}
		laststate = digitalRead( DHT_PIN );
 
		if ( counter == 255 )
			break;
 
		/* ignore first 3 transitions */
		if ( (i >= 4) && (i % 2 == 0) )
		{
			/* shove each bit into the storage bytes */
			data[j / 8] <<= 1;
			if ( counter > 50 )
				data[j / 8] |= 1;
			j++;
		}
	}
 
	/*
	 * check we read 40 bits (8bit x 5 ) + verify checksum in the last byte
	 * print it out if data is good
	 */
	if ( (j >= 40) &&
	     (data[4] == ( (data[0] + data[1] + data[2] + data[3]) & 0xFF) ) )
	{
		float h = (float)((data[0] << 8) + data[1]) / 10;
		if ( h > 100 )
		{
			h = data[0];	// for DHT11
		}
		float c = (float)(((data[2] & 0x7F) << 8) + data[3]) / 10;
		if ( c > 125 )
		{
			c = data[2];	// for DHT11
		}
		if ( data[2] & 0x80 )
		{
			c = -c;
		}
		float f = c * 1.8f + 32;
		
		DatosDHT datos = {c,h,false};
		return datos;
		//printf( "Humidity = %.1f %% Temperature = %.1f *C (%.1f *F)\n", h, c, f );
	}else  {
		printf( "Data not good, skip\n" );
		DatosDHT datos = {0,0,true};
		return datos;
	}
}

int main (int argc, char **argv) {
	wiringPiSetup();
	ros::init(argc, argv, "pub_dht");
	ros::NodeHandle nh;
	ros::Publisher pub = nh.advertise<sensores_prueba::DHT>("datos_dht", 1000) ;

	ros::Rate rate(1);

	float ultimaTemperatura = -100;
	float ultimaHumedad = -100;
	
	while(ros::ok()) {
		//Obtener datos del sensor
		DatosDHT datos = read_dht_data();
		if(!datos.error){
			//Si no existe ningun error, almacenar los datos del sensor en las siguientes variables.
			ultimaTemperatura = datos.temperatura;
			ultimaHumedad = datos.humedad;
		}

		//Crear mensaje ROS
		//Solo crear el mensaje si las variables tienen valores validos.
		if(ultimaTemperatura != -100 && ultimaHumedad != -100){
			sensores_prueba::DHT msg;
			msg.temperatura = ultimaTemperatura;
			msg.humedad = ultimaHumedad;

			pub.publish(msg);
			ROS_INFO_STREAM("Publicando: " << msg.temperatura << " " << msg.humedad);
		}

		rate.sleep();
	}
}

