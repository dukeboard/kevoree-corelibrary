package org.kevoree.library.defaultNodeTypes;

import org.kevoree.Channel;
import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.MBinding;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.library.defaultNodeTypes.command.*;
import org.kevoree.library.defaultNodeTypes.planning.JavaSePrimitive;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 1/29/13
 * Time: 11:22 AM
 */
public class CommandMapper {

    AbstractNodeType nodeType = null;
    java.util.ArrayList<EndAwareCommand> toClean = new java.util.ArrayList<EndAwareCommand>();

    protected Map<String, Object> registry;

    public CommandMapper(Map<String, Object> registry) {
        this.registry = registry;
    }

    public void setNodeType(AbstractNodeType n) {
        nodeType = n;
    }

    public void doEnd() {
        for (EndAwareCommand cmd : toClean) {
            cmd.doEnd();
        }
        toClean.clear();
    }

    public PrimitiveCommand buildPrimitiveCommand(org.kevoreeadaptation.AdaptationPrimitive p, String nodeName) {

        String pTypeName = p.getPrimitiveType().getName();
        if (pTypeName.equals(JavaSePrimitive.UpdateDictionaryInstance)) {
            if (((Instance) p.getRef()).getName().equals(nodeName)) {
                return new SelfDictionaryUpdate((Instance) p.getRef(), nodeType, registry);
            } else {
                return new UpdateDictionary((Instance) p.getRef(), nodeName, registry);
            }

        }

        if (pTypeName.equals(JavaSePrimitive.AddFragmentBinding)) {
            return new AddFragmentBindingCommand((Channel) p.getRef(), p.getTargetNodeName(), nodeName, registry);
        }
        if (pTypeName.equals(JavaSePrimitive.RemoveFragmentBinding)) {
            return new RemoveFragmentBindingCommand((Channel) p.getRef(), p.getTargetNodeName(), nodeName, registry);
        }

        if (pTypeName.equals(JavaSePrimitive.StartInstance)) {
            return new StartStopInstance((Instance) p.getRef(), nodeName, true, registry);
        }
        if (pTypeName.equals(JavaSePrimitive.StopInstance)) {
            return new StartStopInstance((Instance) p.getRef(), nodeName, false, registry);
        }

        if (pTypeName.equals(JavaSePrimitive.AddBinding)) {
            return new AddBindingCommand((MBinding) p.getRef(), nodeName, registry);
        }
        if (pTypeName.equals(JavaSePrimitive.RemoveBinding)) {
            return new RemoveBindingCommand((MBinding) p.getRef(), nodeName, registry);
        }

        if (pTypeName.equals(JavaSePrimitive.AddDeployUnit)) {
            return new AddDeployUnit((DeployUnit) p.getRef(), nodeType.getBootStrapperService(), registry);
        }
        if (pTypeName.equals(JavaSePrimitive.RemoveDeployUnit)) {
            RemoveDeployUnit res = new RemoveDeployUnit((DeployUnit) p.getRef(), nodeType.getBootStrapperService(), registry);
            toClean.add(res);
            return res;
        }
        if (pTypeName.equals(JavaSePrimitive.UpdateDeployUnit)) {
            UpdateDeployUnit res = new UpdateDeployUnit((DeployUnit) p.getRef(), nodeType.getBootStrapperService(), registry);
            toClean.add(res);
            return res;
        }

        if (pTypeName.equals(JavaSePrimitive.AddInstance)) {
            return new AddInstance((Instance) p.getRef(), nodeName, nodeType.getModelService(), nodeType.getKevScriptEngineFactory(), nodeType.getBootStrapperService(),nodeType, registry);
        }
        if (pTypeName.equals(JavaSePrimitive.RemoveInstance)) {
            return new RemoveInstance((Instance) p.getRef(), nodeName, nodeType.getModelService(), nodeType.getKevScriptEngineFactory(), nodeType.getBootStrapperService(),nodeType, registry);
        }

        if (pTypeName.equals(JavaSePrimitive.AddThirdParty)) {
            return new AddDeployUnit((DeployUnit) p.getRef(), nodeType.getBootStrapperService(), registry);
        }
        if (pTypeName.equals(JavaSePrimitive.RemoveThirdParty)) {
            return new RemoveDeployUnit((DeployUnit) p.getRef(), nodeType.getBootStrapperService(), registry);
        }

        //LoggerFactory.getLogger(this.getClass()).error("Return Noop command for "+pTypeName);

        return new NoopCommand();
    }


}
