package org.kevoree.library.defaultNodeTypes.samples;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.cloner.DefaultModelCloner;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.modeling.api.ModelCloner;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 16/11/2013
 * Time: 10:34
 */
@ComponentType
public class HelloWorld extends AbstractComponentType {

    ModelCloner cloner = new DefaultModelCloner();

    @Start
    public void start() {
        System.out.println("I'm just beginning my life !");
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                    ContainerRoot newModel = cloner.clone(getModelService().getLastModel());
                    newModel.findNodesByID(getNodeName()).getComponents().get(0).setStarted(false);
                    getModelService().updateModel(newModel);
                } catch (InterruptedException e) {
                }
            }
        }.start();
    }

    @Stop
    public void stop() {
        System.out.println("Bye all ! :-)");
    }


}
