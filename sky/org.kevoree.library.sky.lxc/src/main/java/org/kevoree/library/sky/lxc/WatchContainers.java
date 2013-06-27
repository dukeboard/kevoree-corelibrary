package org.kevoree.library.sky.lxc;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.library.sky.lxc.utils.IPAddressValidator;
import org.kevoree.log.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Log.info("WatchContainers is Scanning and monitoring the containers");
        for( ContainerNode containerNode : model.getNodes()  ){

            if(!containerNode.getName().equals(lxcHostNode.getNodeName())){

                if(LxcManager.isRunning(containerNode.getName())){

                    String ip =   LxcManager.getIP(containerNode.getName());
                    if(ip != null){

                        if(ipvalidator.validate(ip)){
                            if(!getIPModel(model,containerNode.getName()).contains(ip)){
                                Log.info("The Container {} has the IP address => {}",containerNode.getName(),ip);
                                UUIDModel uuidModel = lxcHostNode.getModelService().getLastUUIDModel();
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


                }    else {

                    if(containerNode.getStarted()){
                        Log.warn("The container {} is not running", containerNode.getName());
                        lxcManager.start_container(containerNode);
                    }

                }
            }

        }
    }
}
