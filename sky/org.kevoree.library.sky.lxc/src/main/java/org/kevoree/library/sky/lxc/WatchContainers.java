package org.kevoree.library.sky.lxc;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.library.sky.lxc.utils.IPAddressValidator;
import org.kevoree.log.Log;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 25/06/13
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */
public class WatchContainers implements Runnable {

    private LxcHostNode lxcHostNode;
    private LxcManager lxcManager;
    private IPAddressValidator ipvalidator = new IPAddressValidator();
    private boolean starting  = true;
    public WatchContainers(LxcHostNode lxcHostNode, LxcManager lxcManager) {
        this.lxcHostNode = lxcHostNode;
        this.lxcManager = lxcManager;
    }

    private void updateNetworkProperties(ContainerRoot model, String remoteNodeName, String address) {
        Log.debug("set " + remoteNodeName + " " + address);
        KevoreePlatformHelper.instance$.updateNodeLinkProp(model, remoteNodeName, remoteNodeName, org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP(), address, "LAN", 100);
    }

    public List<String> getIPModel(ContainerRoot model,String remoteNodeName)
    {
        return KevoreePropertyHelper.instance$.getNetworkProperties(model, remoteNodeName, org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP());
    }


    @Override
    public void run() {
        ContainerRoot model = lxcHostNode.getModelService().getLastModel();
        UUIDModel uuidModel = null;
        List<String> lxNodes = lxcManager.getContainers();
        if (lxNodes.size() > model.getNodes().size()){
            try {
                ContainerRoot  scanned = lxcManager.buildModelCurrentLxcState(lxcHostNode.getKevScriptEngineFactory(), lxcHostNode.getNodeName());
                if( scanned.getNodes().size() > model.getNodes().size()){
                    uuidModel=  lxcHostNode.getModelService().getLastUUIDModel();
                    lxcHostNode.getModelService().compareAndSwapModel(uuidModel, scanned);
                    Log.debug("UPDATE Containers");
                }
                starting = false;
            } catch (Exception e) {
                Log.error("Updating model from lxc-ls");
            }
        } else
        {

            for(ContainerNode n : model.getNodes()){

                if(!lxNodes.contains(n.getName())){

                    // todo remove

                }


            }

        }


        Log.debug("WatchContainers is Scanning and monitoring the containers");


        for( ContainerNode containerNode :   lxcHostNode.getModelElement().getHosts()){

            if(LxcManager.isRunning(containerNode.getName())){

                String ip =   LxcManager.getIP(containerNode.getName());
                if(ip != null){

                    if(ipvalidator.validate(ip)){
                        if(!getIPModel(model,containerNode.getName()).contains(ip)){
                            Log.info("The Container {} has the IP address => {}",containerNode.getName(),ip);
                            uuidModel=  lxcHostNode.getModelService().getLastUUIDModel();
                            ModelCloner cloner = new ModelCloner();
                            ContainerRoot readWriteModel = cloner.clone(lxcHostNode.getModelService().getLastModel());
                            updateNetworkProperties(readWriteModel, containerNode.getName(), ip);
                            lxcHostNode.getModelService().compareAndSwapModel(uuidModel, readWriteModel);
                        }
                    }
                    else
                    {
                        Log.error("The format of the ip is wrong or not define");
                    }
                }
                Integer ram=1024 ,cpu_frequency= 1024;
                String cpus="0";
                try
                {
                    ram = Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(containerNode, "RAM", false, ""));
                    LxcManager.setlimitMemory(containerNode.getName(),ram);
                }catch (Exception e){
                    Log.warn("RAM Limit is not set for {} ",containerNode.getName());
                }

                try
                {
                    cpu_frequency = Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(containerNode, "CPU_FREQUENCY", false, ""));
                    LxcManager.setlimitCPU(containerNode.getName(),cpu_frequency);
                }catch (Exception e){
                    Log.warn("CPU_FREQUENCY Limit is not set for {} ",containerNode.getName());
                }

                try
                {
                    cpus = KevoreePropertyHelper.instance$.getProperty(containerNode, "CPU_CORE", false, "");
                    LxcManager.setCPUAffinity(containerNode.getName(),cpus);
                }catch (Exception e){
                    Log.warn("CPU_CORE Limit is not set for "+containerNode.getName()+" cpus "+cpus);
                }


            }    else {

                if(containerNode.getStarted()){
                    Log.warn("The container {} is not running", containerNode.getName());
                    lxcManager.start_container(containerNode);
                }

            }




        }
    }
}
