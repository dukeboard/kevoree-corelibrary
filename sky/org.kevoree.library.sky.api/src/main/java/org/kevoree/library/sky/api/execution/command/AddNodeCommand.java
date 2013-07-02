package org.kevoree.library.sky.api.execution.command;

import org.kevoree.ContainerRoot;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.library.sky.api.execution.KevoreeNodeManager;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public class AddNodeCommand implements PrimitiveCommand {

    private ContainerRoot iaasModel;
    private String targetChildName;
    private KevoreeNodeManager nodeManager;

    public AddNodeCommand(ContainerRoot iaasModel, String targetChildName, KevoreeNodeManager nodeManager) {
        this.iaasModel = iaasModel;
        this.targetChildName = targetChildName;
        this.nodeManager = nodeManager;
    }

    public boolean execute() {
        //TODO PRUNE MODEL

        return nodeManager.addNode(iaasModel, targetChildName, iaasModel);
    }

    public void undo() {
        nodeManager.removeNode(iaasModel, targetChildName);
    }

    public String toString() {
        return "addNode " + targetChildName;
    }
}