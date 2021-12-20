package com.example.rosdron;

import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ros.RosCore;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConexionManager {

    private MainActivity mainActivity;
    private RosCore rosCore;
    private NodeMainExecutor nodeMainExecutor;
    private boolean error;
    public ConexionManager(MainActivity mainActivity){
        this.error = true;
        this.mainActivity = mainActivity;
    }

    private static final NodeConfiguration getNodeConfiguration(final String rosHostIp, final String nodeName, final URI rosMasterUri) {
        //Create a node configuration
        final NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(rosHostIp);
        nodeConfiguration.setNodeName(nodeName);
        nodeConfiguration.setMasterUri(rosMasterUri);
        return nodeConfiguration;
    }

    public void setError(boolean error){
        this.error = error;
    }

    public void terminar(){
        rosCore.getMasterServer().shutdown();
        rosCore.shutdown();
        //rosCore.getMasterServer().unregisterSubscriber()
        System.out.println("ROS SHUTDOWN");
    }


    public void iniciar(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //esperarErrorConexion();
            conectarROS();
            handler.post(() -> {
                //UI Thread work here
            });
        });
    }

    public void conectarROS(){
        try  {
            final String subscriberName1 = "/blebrecht/subscriber/";
            final String subscriberName2 = "/blebrecht/subscriber2/";

            final String topicName1 = "datos_dht";
            final String topicName2 = "/dji_osdk_ros/gps_position/";

            final String rosHostIp = "10.42.0.1";
            final int rosHostPort = 11311;

            /*
            final String subscriberName = "/blebrecht/subscriber/";
            final String topicName = "/numeros_aleatorios/";
            final String rosHostIp = "192.168.20.17";
            final int rosHostPort = 11311;
             */

            //Create a publically available roscore in port rosHostPort.
            rosCore = RosCore.newPublic(rosHostPort);
            //This will start the created java ROS Core.
            rosCore.start();
            try {
                final URI rosMasterUri = new URI("http://"+rosHostIp+":"+rosHostPort);
                //Before proceeding any further we need to make sure that the roscore is already started.
                //The following line will wait for the roscore to start for a maximum of 2 seconds
                final boolean started = rosCore.awaitStart(2_000, TimeUnit.MILLISECONDS);

                if (started) {
                    //An executor is needed to spawn ROS nodes from Java
                    nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
                    // Create a subscriber for the specified topic name and publisher name
                    ROSJavaSubscriberNodeDHT rosJavaSubscriberNodeMain1 = new ROSJavaSubscriberNodeDHT(topicName1, subscriberName1, mainActivity);
                    NodeConfiguration subscriberNodeConfiguration1 = getNodeConfiguration(rosHostIp, subscriberName1, rosMasterUri);
                    nodeMainExecutor.execute(rosJavaSubscriberNodeMain1, subscriberNodeConfiguration1);

                    ROSJavaSubscriberNodeDronGPS rosJavaSubscriberNodeMain2 = new ROSJavaSubscriberNodeDronGPS(topicName2, subscriberName2, mainActivity);
                    NodeConfiguration subscriberNodeConfiguration2 = getNodeConfiguration(rosHostIp, subscriberName2, rosMasterUri);
                    nodeMainExecutor.execute(rosJavaSubscriberNodeMain2, subscriberNodeConfiguration2);

                    TimeUnit.SECONDS.sleep(10);
                    System.out.println("Termina espera de 10 segundos");
                    if(error){
                        terminar();
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainActivity.getErrorManager().mostrarError("No se ha podido conectar con ROS.");
                                mainActivity.reiniciarBotonConectar();
                            }
                        });
                    }

                }
            } catch (final Exception exception) {
                //in case of an exception print the stacktrace and exit with EXIT_ERROR(1) value
                System.err.println(ExceptionUtils.getStackTrace(exception));
            }
            //Exit with value EXIT_OK(0).
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void conexionCorrecta(){
        setError(false);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.bloquearBotonConectar();
            }
        });

    }
}
