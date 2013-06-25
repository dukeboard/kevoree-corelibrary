package org.kevoree.library.sky.lxc;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.log.Log;

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

    public  WatchContainers(LxcHostNode node){

        this.lxcHostNode = node;
    }

    private void updateNetworkProperties(ContainerRoot model, String remoteNodeName, String address) {
        Log.debug("set " + remoteNodeName + " " + address);
        KevoreePlatformHelper.instance$.updateNodeLinkProp(model, remoteNodeName, remoteNodeName, org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP(), address, "LAN", 100);
    }


    public static boolean isIpFormat(String ip){
        Pattern pattern;
        Matcher matcher;
        pattern= Pattern.compile("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}");
        matcher = pattern.matcher(ip);
        return matcher.find();
    }

    @Override
    public void run() {

        for( ContainerNode containerNode : lxcHostNode.getModelService().getLastModel().getNodes()  ){

            if(!containerNode.getName().equals(lxcHostNode.getNodeName())){

                UUIDModel uuidModel = lxcHostNode.getModelService().getLastUUIDModel();
                String ip =   LxcManager.getIP(containerNode.getName());
                Log.info("IP Scanning {} {}",containerNode.getName(),ip);

                if(ip != null){

                    if(isIpFormat(ip)){

                        ModelCloner cloner = new ModelCloner();
                        ContainerRoot readWriteModel = cloner.clone(lxcHostNode.getModelService().getLastModel());
                        updateNetworkProperties(readWriteModel, containerNode.getName(), ip);

                        lxcHostNode.getModelService().compareAndSwapModel(uuidModel, readWriteModel);
                    }
                    else
                    {
                        Log.error("The format of the ip is wrong");
                    }


                }
            }

        }

    }
}
