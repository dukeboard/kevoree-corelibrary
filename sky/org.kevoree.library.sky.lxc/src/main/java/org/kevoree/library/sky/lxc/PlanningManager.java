package org.kevoree.library.sky.lxc;

import org.kevoree.framework.AbstractNodeType;
import org.kevoree.library.sky.api.nodeType.CloudNode;
import org.kevoreeadaptation.AdaptationPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/06/13
 * Time: 10:00
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class PlanningManager extends org.kevoree.library.sky.api.PlanningManager {

    public PlanningManager(AbstractNodeType skyNode) {
        super(skyNode);
    }

    @Override
    public void createNextStep(List<AdaptationPrimitive> commands) {
        List<AdaptationPrimitive> addNodeCommands = new ArrayList<AdaptationPrimitive>(10);
        List<AdaptationPrimitive> currentCommands = new ArrayList<AdaptationPrimitive>();
        for (int i = 0; i < commands.size(); i++) {
            AdaptationPrimitive command = commands.get(i);
            if (command.getPrimitiveType().getName().equals(CloudNode.ADD_NODE)) {
                addNodeCommands.add(command);
                if (addNodeCommands.size() == 10) {
                    super.createNextStep(addNodeCommands);
                    addNodeCommands.clear();
                }
            } else {
                currentCommands.add(command);
            }
        }
        super.createNextStep(addNodeCommands);
        super.createNextStep(currentCommands);
    }
}
