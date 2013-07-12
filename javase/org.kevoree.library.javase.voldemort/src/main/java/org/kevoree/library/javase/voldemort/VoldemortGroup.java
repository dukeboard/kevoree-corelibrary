package org.kevoree.library.javase.voldemort;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.library.javase.voldemort.pojo.Server;
import org.kevoree.log.Log;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 10/07/13
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
@DictionaryType({
        @DictionaryAttribute(name = "id", defaultValue = "", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "httpport", defaultValue = "", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "socketport", defaultValue = "", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "adminport", defaultValue = "", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "partitions", defaultValue = "", optional = true, fragmentDependant = true)

})
@GroupType
@Library(name = "JavaSE")
public class VoldemortGroup extends AbstractGroupType {

    private VoldemortManager voldemortManager=null;
    private Server localserver=null;

    @Start
    public void start(){
        try {
            voldemortManager = new VoldemortManager();
            voldemortManager.install();
            configureVoldemort();
            voldemortManager.start();
        } catch (Exception e) {
            Log.error("Start voldemort",e);
        }
    }

    @Stop
    public void stop(){
        voldemortManager.stop();
    }
    @Update
    public void update(){

    }

    @Override
    public void triggerModelUpdate() {
        configureVoldemort();
        voldemortManager.update();
    }

    @Override
    public void push(ContainerRoot containerRoot, String s) throws Exception {

    }

    @Override
    public ContainerRoot pull(String s) throws Exception {
        return null;
    }

    public void configureVoldemort(){

        voldemortManager.clearConf();
        voldemortManager.setId(Integer.parseInt(getDictionary().get("id").toString()));

        localserver= new Server(voldemortManager.getId(),getIp(getNodeName()),Integer.parseInt(getDictionary().get("httpport").toString()),Integer.parseInt(getDictionary().get("socketport").toString()),Integer.parseInt(getDictionary().get("adminport").toString()));

        localserver.setPartitions( getDictionary().get("partitions").toString());
        System.out.println(localserver.toString());
        voldemortManager.addServer(localserver);

        // sub nodes
        Group group = getModelElement();
        for (ContainerNode subNode : group.getSubNodes()) {
            if (!subNode.getName().equals(this.getNodeName())) {
                Group groupOption = getModelService().getLastModel().findGroupsByID(getName());

                int idsub = Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(groupOption, "id", true, subNode.getName()));
                int httpportsub =  Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(groupOption, "httpport", true, subNode.getName()));
                int socketportsub =  Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(groupOption, "socketport", true, subNode.getName()));
                int adminportsub =  Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(groupOption, "adminport", true, subNode.getName()));

                Server    serversub= new Server(idsub,getIp(subNode.getName()), httpportsub,socketportsub,adminportsub);
                String partitionssub =        KevoreePropertyHelper.instance$.getProperty(groupOption, "partitions", true, subNode.getName());
                serversub.setPartitions(partitionssub);
                System.out.println(serversub);
                voldemortManager.addServer(serversub);
            }
        }

    }

    public String getIp(String node){
        List<String> ips = KevoreePropertyHelper.instance$.getNetworkProperties(getModelService().getLastModel(), node, org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP());
        ips.remove("127.0.0.1");
        // current node
        String ip= "localhost";
        if(ips.size() == 0){
            try {
                ip = Inet4Address.getLocalHost().getHostName().toString();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        } else {
            ip  = ips.get(0);
        }
        return ip;
    }
}
