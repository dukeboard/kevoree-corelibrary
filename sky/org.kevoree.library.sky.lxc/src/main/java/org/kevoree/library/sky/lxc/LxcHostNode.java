package org.kevoree.library.sky.lxc;

import org.kevoree.annotation.*;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.api.CloudNode;
import org.kevoree.library.sky.api.KevoreeNodeRunnerFactory;
import org.kevoree.library.sky.api.execution.CommandMapper;
import org.kevoree.library.sky.api.execution.KevoreeNodeManager;
import org.kevoree.library.sky.api.execution.KevoreeNodeRunner;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/06/13
 * Time: 13:48
 */

@NodeType
@PrimitiveCommands(value = {
        @PrimitiveCommand(name = CloudNode.ADD_NODE, maxTime = LxcHostNode.ADD_TIMEOUT),
        @PrimitiveCommand(name = CloudNode.REMOVE_NODE, maxTime = LxcHostNode.REMOVE_TIMEOUT)
})
@DictionaryType({
        @DictionaryAttribute(name = LxcHostNode.MAX_CONCURRENT_ADD_NODE, optional = true, defaultValue = "5"),     //crappy !!!!
        @DictionaryAttribute(name = "daysRetentionContainersDestroyed", optional = false, defaultValue ="30")
})
public class LxcHostNode extends JavaSENode implements CloudNode {

    public static final long ADD_TIMEOUT = 300000l;
    public static final long REMOVE_TIMEOUT = 180000l;
    public static final long CREATE_CLONE_TIMEOUT = 180000l;
    static final String MAX_CONCURRENT_ADD_NODE = "MAX_CONCURRENT_ADD_NODE";
    private LxcManager lxcManager = new LxcManager();
    private WatchContainers watchContainers;
    private WatchBackupsContainers watchBackupsContainers;
    private KevoreeNodeManager nodeManager;
    private ScheduledThreadPoolExecutor executor = null;
    private int maxAddNode;
    private  CreateBaseClone createBaseClone;


    @Start
    @Override
    public void startNode() {
        super.startNode();

        List<String> urls = new ArrayList<String>();
        urls.add("https://oss.sonatype.org/content/groups/public/");//not mandatory but for early release
        File watchdog = getBootStrapperService().resolveArtifact("org.kevoree.watchdog", "org.kevoree.watchdog", "RELEASE", "deb",urls);
        lxcManager.setWatchdogLocalFile(watchdog);

        createBaseClone = new CreateBaseClone(this, lxcManager);
        watchContainers = new WatchContainers(this, lxcManager);
        watchBackupsContainers = new WatchBackupsContainers(this,lxcManager);
        watchBackupsContainers.setDaysretentions(getDaysRetentions());

        nodeManager = new KevoreeNodeManager(new LXCNodeRunnerFactory());
        maxAddNode = 5;
        try {
            maxAddNode = Integer.parseInt(getDictionary().get(MAX_CONCURRENT_ADD_NODE).toString());
        } catch (NumberFormatException e) {

        }
        kompareBean = new PlanningManager(this, maxAddNode);
        mapper = new CommandMapper(nodeManager);
        mapper.setNodeType(this);
        Thread clone = new Thread(createBaseClone);
        clone.start();

        executor = new ScheduledThreadPoolExecutor(ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
        executor.scheduleAtFixedRate(watchContainers, 15, 15, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(watchBackupsContainers, 30, 10, TimeUnit.SECONDS);
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

    public Integer getDaysRetentions(){
        try {
            return     Integer.parseInt(getDictionary().get("daysRetentionContainersDestroyed").toString());
        }   catch (Exception e ){
            return 30;
        }
    }

    @Update
    public void update(){
        watchBackupsContainers.setDaysretentions(getDaysRetentions());
    }



}
