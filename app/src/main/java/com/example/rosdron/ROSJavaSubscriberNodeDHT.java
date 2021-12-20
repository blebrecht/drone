package com.example.rosdron;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.StringUtils;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Subscriber;

import sensor_msgs.NavSatFix;
import sensores_prueba.DHT;
import std_msgs.Int64;

public final class ROSJavaSubscriberNodeDHT extends AbstractNodeMain {
    private final java.lang.String rosTopicName;
    private final java.lang.String rosNodeName;
    private MainActivity mainActivity;

    /**
     * @param rosTopicName the name of the topic to subscribe
     * @param rosNodeName  he name of the ROS node
     */
    public ROSJavaSubscriberNodeDHT(final java.lang.String rosTopicName, final java.lang.String rosNodeName, MainActivity mainActivity) {
        //Let's require that rosNodeName and rosTopicName are not null to eagerly identify this error
        //These checks are completely optional
        Preconditions.checkArgument(StringUtils.isNotBlank(rosNodeName));
        Preconditions.checkArgument(StringUtils.isNotBlank(rosTopicName));

        this.rosTopicName = rosTopicName;
        this.rosNodeName = rosNodeName;
        this.mainActivity = mainActivity;
    }

    /**
     * @return
     */
    @Override
    public final GraphName getDefaultNodeName() {
        return GraphName.of(this.rosNodeName);
    }

    /**
     * Is executed once after node is connected.
     *
     * @param connectedNode a {@link ConnectedNode} that will be provided as an argument
     *
     * @see AbstractNodeMain#onStart(ConnectedNode)
     */

    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        final Subscriber<DHT> subscriber = connectedNode.newSubscriber(this.rosTopicName, DHT._TYPE);

        //Se pudo iniciar correctamente el suscriptor
        mainActivity.getConexionManager().conexionCorrecta();

        subscriber.addMessageListener(new MessageListener<DHT>() {
            @Override
            public void onNewMessage(DHT message) {
                System.out.println("Llegan datos: "+message.getTemperatura()+" "+message.getHumedad());
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.getTextView().setText("Temperatura: "+message.getTemperatura()+" Humedad: "+message.getHumedad());
                    }
                });
            }
        });
    }


}
