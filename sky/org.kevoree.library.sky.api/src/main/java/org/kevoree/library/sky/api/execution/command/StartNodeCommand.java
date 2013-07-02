package org.kevoree.library.sky.api.execution.command;

import org.kevoree.ContainerRoot;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.library.sky.api.execution.KevoreeNodeManager;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/06/13
 * Time: 12:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class StartNodeCommand implements PrimitiveCommand {

    private ContainerRoot iaasModel;
    private String targetChildName;
    private KevoreeNodeManager nodeManager;

    public StartNodeCommand(ContainerRoot iaasModel, String targetChildName, KevoreeNodeManager nodeManager) {
        this.iaasModel = iaasModel;
        this.targetChildName = targetChildName;
        this.nodeManager = nodeManager;
    }

    public boolean execute() {
        //TODO PRUNE MODEL

        return nodeManager.startNode(iaasModel, targetChildName, iaasModel);
    }

    public void undo() {
        nodeManager.removeNode(iaasModel, targetChildName);
    }

    public String toString() {
        return "AddNode " + targetChildName;
    }
}
