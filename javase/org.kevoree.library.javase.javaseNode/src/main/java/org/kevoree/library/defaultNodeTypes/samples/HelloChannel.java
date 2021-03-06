package org.kevoree.library.defaultNodeTypes.samples;

import org.kevoree.annotation.ChannelType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 16/11/2013
 * Time: 23:28
 */
@ChannelType
public class HelloChannel extends AbstractChannelFragment {

    @Start
    public void start() {
        System.out.println("Hello from channel");
    }

    @Stop
    public void stop() {
        System.out.println("Bye from channel");
    }

    @Override
    public Object dispatch(Message message) {

        System.out.println("Dispatch :-) "+message.get_content());

        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String s, String s2) {
        return null;
    }
}
