package org.kevoree.library.sky.minicloud.nodeType;

import org.kevoree.annotation.*;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.api.CloudNode;
import org.kevoree.library.sky.api.KevoreeNodeRunnerFactory;
import org.kevoree.library.sky.api.PaaSNode;
import org.kevoree.library.sky.api.execution.CommandMapper;
import org.kevoree.library.sky.api.execution.KevoreeNodeManager;
import org.kevoree.library.sky.api.execution.KevoreeNodeRunner;
import org.kevoree.library.sky.api.planning.PlanningManager;
import org.kevoree.library.sky.minicloud.MiniCloudKevoreeNodeRunner;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@DictionaryType({
        @DictionaryAttribute(name = "VMARGS", optional = true)
})
@NodeType
@PrimitiveCommands(value = {
        @PrimitiveCommand(name = CloudNode.ADD_NODE, maxTime = 120000)
}, values = {CloudNode.REMOVE_NODE})
public class MiniCloudNode extends JavaSENode implements CloudNode, PaaSNode {

    private KevoreeNodeManager nodeManager;

    @Start
    @Override
    public void startNode() {
        Log.debug("Starting node type of {}", this.getName());
        super.startNode();
        nodeManager = new KevoreeNodeManager(new MiniCloudNodeRunnerFactory());
        kompareBean = new PlanningManager(this);
        mapper = new CommandMapper(nodeManager);
        mapper.setNodeType(this);
    }

    @Stop
    @Override
    public void stopNode() {
        Log.debug("Stopping node type of {}", this.getName());
        nodeManager.stop();
        super.stopNode();
    }

    public class MiniCloudNodeRunnerFactory implements KevoreeNodeRunnerFactory {
        @Override
        public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
            return new MiniCloudKevoreeNodeRunner(nodeName, MiniCloudNode.this);
        }
    }
}
