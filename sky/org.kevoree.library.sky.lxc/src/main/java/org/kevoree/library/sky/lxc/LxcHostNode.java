package org.kevoree.library.sky.lxc;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.api.CommandMapper;
import org.kevoree.library.sky.api.KevoreeNodeManager;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.PlanningManager;
import org.kevoree.library.sky.api.nodeType.CloudNode;
import org.kevoree.library.sky.api.nodeType.KevoreeNodeRunnerFactory;
import org.kevoree.log.Log;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/06/13
 * Time: 13:48
 */

@Library(name = "SKY")
@NodeType
@PrimitiveCommands(value = {
        @PrimitiveCommand(name = CloudNode.ADD_NODE, maxTime = LxcHostNode.ADD_TIMEOUT),
        @PrimitiveCommand(name = CloudNode.REMOVE_NODE, maxTime = LxcHostNode.REMOVE_TIMEOUT)
})
public class LxcHostNode extends JavaSENode implements CloudNode {

    public static final long ADD_TIMEOUT = 300000l;
    public static final long REMOVE_TIMEOUT = 180000l;
    private boolean done = false;
    private LxcManager lxcManager = new LxcManager();
    private  WatchContainers watchContainers;
    private KevoreeNodeManager nodeManager;
    private ScheduledThreadPoolExecutor executor = null;


    @Start
    @Override
    public void startNode() {
        super.startNode();
        watchContainers = new WatchContainers(this,lxcManager);
        nodeManager = new KevoreeNodeManager(new LXCNodeRunnerFactory());
        kompareBean = new PlanningManager(this);
        mapper = new CommandMapper(nodeManager);
        mapper.setNodeType(this);

        executor = new ScheduledThreadPoolExecutor(ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
        executor.scheduleAtFixedRate(watchContainers,10,15,TimeUnit.SECONDS);

        getModelService().registerModelListener(new LXCModelListener());

    }

    @Stop
    @Override
    public void stopNode() {
        executor.shutdownNow();
        nodeManager.stop();
        super.stopNode();
    }


    public class LXCNodeRunnerFactory implements KevoreeNodeRunnerFactory {
        @Override
        public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
            return new LxcNodeRunner(nodeName, LxcHostNode.this);
        }
    }

    public LxcManager getLxcManager() {
        return lxcManager;
    }

    private class LXCModelListener implements ModelListener {
        @Override
        public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
            return true;
        }

        @Override
        public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
            return true;
        }

        @Override
        public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
            return true;
        }

        @Override
        public void modelUpdated() {
            if (!done) {
                done = true;

                try {
                    // install scripts
                    lxcManager.install();
                    // check if there is a clone source
                    lxcManager.createClone();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error("Fatal Kevoree LXC configuration");
                }

                if (lxcManager.getContainers().size() > 0) {

                    ContainerRoot target = null;
                    // looking for previous containers
                    try {
                        target = lxcManager.buildModelCurrentLxcState(getKevScriptEngineFactory(), getNodeName());

                        getModelService().unregisterModelListener(this);
                        getModelService().atomicUpdateModel(target);
                        getModelService().registerModelListener(this);
                    } catch (Exception e) {
                        Log.error("Getting Current LXC State", e);
                    }

                }
            }

        }

        @Override
        public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
        }

        @Override
        public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
        }
    }
}
