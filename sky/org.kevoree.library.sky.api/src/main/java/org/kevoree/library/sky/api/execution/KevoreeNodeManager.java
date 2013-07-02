package org.kevoree.library.sky.api.execution;

import org.kevoree.ContainerRoot;
import org.kevoree.library.sky.api.KevoreeNodeRunnerFactory;
import org.kevoree.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class KevoreeNodeManager {


    List<KevoreeNodeRunner> runners = new ArrayList<KevoreeNodeRunner>();

    private KevoreeNodeRunnerFactory kevoreeNodeRunnerFactory;

    public KevoreeNodeManager(KevoreeNodeRunnerFactory kevoreeNodeRunnerFactory) {
        this.kevoreeNodeRunnerFactory = kevoreeNodeRunnerFactory;
    }

    public void stop() {
        Log.debug("try to stop all nodes");

        for (KevoreeNodeRunner runner : runners) {
            runner.stopNode();
        }
        runners.clear();
    }

    public boolean addNode(ContainerRoot iaasModel, String targetChildName, ContainerRoot targetChildModel) {
        Log.debug("try to add {}", targetChildName);

        KevoreeNodeRunner newRunner = kevoreeNodeRunnerFactory.createKevoreeNodeRunner(targetChildName);
        runners.add(newRunner);

        return newRunner.addNode(iaasModel, targetChildModel);
    }


    public boolean startNode(ContainerRoot iaasModel, String targetChildName, ContainerRoot targetChildModel) {
        Log.debug("try to start {}", targetChildName);

        KevoreeNodeRunner runner = null;
        for (KevoreeNodeRunner r : runners) {
            if (r.getNodeName().equals(targetChildName)) {
                runner = r;
            }
        }
        if (runner != null) {
            return runner.startNode(iaasModel, targetChildModel);
        } else {
            return true;
        }
    }

    public boolean stopNode(ContainerRoot iaasModel, String targetChildName) {
        Log.debug("try to stop {}", targetChildName);

        KevoreeNodeRunner runner = null;
        for (KevoreeNodeRunner r : runners) {
            if (r.getNodeName().equals(targetChildName)) {
                runner = r;
            }
        }
        if (runner != null) {
            return runner.stopNode();
        } else {
            return true;
        }
    }

    public boolean removeNode(ContainerRoot iaasModel, String targetChildName) {
        Log.debug("try to remove {}", targetChildName);

        KevoreeNodeRunner runner = null;
        for (KevoreeNodeRunner r : runners) {
            if (r.getNodeName().equals(targetChildName)) {
                runner = r;
            }
        }
        if (runner != null) {
            return runner.removeNode();
        } else {
            return true;
        }
    }
}