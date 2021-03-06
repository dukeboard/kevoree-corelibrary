package org.kevoree.library.sky.api.execution.command;

import org.kevoree.ContainerRoot;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.library.sky.api.execution.KevoreeNodeManager;

import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/06/13
 * Time: 12:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class StopNodeCommand implements PrimitiveCommand {

    private ContainerRoot iaasModel;
    private String targetChildName;
    private KevoreeNodeManager nodeManager;

    private Map<String, Object> registry;

    public StopNodeCommand(ContainerRoot iaasModel, String targetChildName, KevoreeNodeManager nodeManager, Map<String, Object> registry) {
        this.iaasModel = iaasModel;
        this.targetChildName = targetChildName;
        this.nodeManager = nodeManager;
        this.registry = registry;
    }

    public boolean execute() {
        //TODO PRUNE MODEL

        return nodeManager.stopNode(iaasModel, targetChildName);
    }

    public void undo() {
        nodeManager.startNode(iaasModel, targetChildName, iaasModel);
    }

    public String toString() {
        return "stopNode " + targetChildName;
    }
}
