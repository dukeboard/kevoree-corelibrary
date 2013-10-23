package org.kevoree.library.sky.api.execution.command;

import org.kevoree.ContainerRoot;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.library.sky.api.execution.KevoreeNodeManager;

import java.util.Map;

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

    private Map<String, Object> registry;

    public RemoveNodeCommand(ContainerRoot iaasModel, String targetChildName, KevoreeNodeManager nodeManager, Map<String, Object> registry) {
        this.iaasModel = iaasModel;
        this.targetChildName = targetChildName;
        this.nodeManager = nodeManager;
        this.registry = registry;
    }

    public boolean execute() {
        if (nodeManager.removeNode(iaasModel, targetChildName)) {
            registry.remove(iaasModel.findNodesByID(targetChildName).path());
            return true;
        } else {
            return false;
        }
    }

    public void undo() {
        nodeManager.addNode(iaasModel, targetChildName, iaasModel);
        registry.put(iaasModel.findNodesByID(targetChildName).path(), iaasModel.findNodesByID(targetChildName).path());
    }

    public String toString() {
        return "RemoveNode " + targetChildName;
    }
}