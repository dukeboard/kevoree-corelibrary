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

public class RemoveNodeCommand implements PrimitiveCommand {

    private ContainerRoot iaasModel;
    private String targetChildName;
    private KevoreeNodeManager nodeManager;

    public RemoveNodeCommand(ContainerRoot iaasModel, String targetChildName, KevoreeNodeManager nodeManager) {
        this.iaasModel = iaasModel;
        this.targetChildName = targetChildName;
        this.nodeManager = nodeManager;
    }

    public boolean execute() {
        return nodeManager.removeNode(iaasModel, targetChildName);
    }

    public void undo() {
        nodeManager.addNode(iaasModel, targetChildName, iaasModel);
    }

    public String toString() {
        return "RemoveNode " + targetChildName;
    }
}