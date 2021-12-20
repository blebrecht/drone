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

public final class ROSJavaSubscriberNodeDronGPS extends AbstractNodeMain {
    private final String rosTopicName;
    private final String rosNodeName;
    private MainActivity mainActivity;

    /**
     * @param rosTopicName the name of the topic to subscribe
     * @param rosNodeName  he name of the ROS node
     */
    public ROSJavaSubscriberNodeDronGPS(final String rosTopicName, final String rosNodeName, MainActivity mainActivity) {
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
        System.out.println("pasa1");
        final Subscriber<NavSatFix> subscriber = connectedNode.newSubscriber(this.rosTopicName, sensor_msgs.NavSatFix._TYPE);
        System.out.println("pasa2");
        //Se pudo iniciar correctamente el suscriptor
        mainActivity.getConexionManager().setError(false);

        subscriber.addMessageListener(new MessageListener<NavSatFix>() {
            @Override
            public void onNewMessage(sensor_msgs.NavSatFix message) {
                System.out.println("pasa3");
                System.out.println("Llega altura: "+message.getAltitude());
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.getTextView2().setText("Altura: "+message.getAltitude()+"");
                    }
                });

            }
        });

    }

    @Override
    public void onError(Node node, Throwable throwable) {
        System.out.println("pasa error");
    }
}
