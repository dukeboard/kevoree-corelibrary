package org.kevoree.library.sky.lxc;


import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.library.sky.api.execution.KevoreeNodeRunner;
import org.kevoree.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/06/13
 * Time: 13:48
 */
public class LxcNodeRunner extends KevoreeNodeRunner {

//    private  String nodeName= "";
    private LxcHostNode iaasNode;
    private  LxcManager lxcManager =new LxcManager();

    public LxcNodeRunner(String nodeName,LxcHostNode iaasNode) {
        super(nodeName);
        this.iaasNode = iaasNode;
//        this.nodeName = nodeName;
    }

    @Override
    public boolean addNode(ContainerRoot iaasModel, ContainerRoot childBootStrapModel) {
        Log.debug("addNode " + getNodeName() + "  parent = " + iaasNode.getNodeName());
        ContainerNode node =    iaasModel.findByPath("nodes[" + iaasNode.getName() + "]/hosts[" + getNodeName() + "]", ContainerNode.class);

        return    lxcManager.create_container(getNodeName(), iaasNode, node, iaasModel);
    }

    @Override
    public boolean startNode(ContainerRoot iaasModel, ContainerRoot childBootStrapModel) {
        Log.debug("startNode " + getNodeName() + "  parent = " + iaasNode.getNodeName());
        ContainerNode node =    iaasModel.findByPath("nodes[" + iaasNode.getName() + "]/hosts[" + getNodeName() + "]", ContainerNode.class);
        return    lxcManager.start_container(node);
    }

    @Override
    public boolean stopNode() {
        return   lxcManager.stop_container(getNodeName());
    }

    @Override
    public boolean removeNode() {
        return   lxcManager.remove_container(getNodeName());
    }

    @Override
    public File getOutFile() {
        // /var/lib/lxc/<nodeName>/rootfs seems to be the path to the file system of the container
        // So the log file is located to /var/lib/lxc/<nodeName>/rootfs/usr/share/kevoree/<nodeName>.log
        if (super.getOutFile() == null) {
            super.setOutFile(new File("/var/lib/lxc/" + getNodeName() + "/rootfs/usr/share/kevoree/" + getNodeName() + ".log"));
        }
        return super.getOutFile();
    }

    @Override
    public File getErrFile() {
        // by default, there is no err file
        if (super.getErrFile() == null) {
            try {
                super.setErrFile(File.createTempFile("emptyErrLogFile", ".log"));
            } catch (IOException ignored) {
            }
        }
        return super.getErrFile();
    }
}
