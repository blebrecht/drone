package sensores_prueba;

import org.ros.internal.message.Message;

public interface DHT extends Message {

    public static final String _TYPE = "sensores_prueba/DHT";

    public static final String _DEFINITION = "float32 temperatura\nfloat32 humedad";

    float getTemperatura();

    void setTemperatura(double paramFloat);

    float getHumedad();

    void setHumedad(double paramFloat);
}
