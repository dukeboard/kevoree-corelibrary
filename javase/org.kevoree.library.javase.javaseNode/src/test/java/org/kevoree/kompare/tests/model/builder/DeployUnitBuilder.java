package org.kevoree.kompare.tests.model.builder;

import org.kevoree.DeployUnit;
import org.kevoree.KevoreeFactory;
import org.kevoree.NodeType;
import org.kevoree.TypeDefinition;

import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DeployUnitBuilder extends Builder {

    private static Random random = new Random();

    private String groupName;
    private String version;
    private String hashCode;
    private NodeType targetNodeType;
    private String type;
    private String url;

    protected DeployUnitBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public static void setRandom(Random random) {
        DeployUnitBuilder.random = random;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public void setTargetNodeType(NodeType targetNodeType) {
        this.targetNodeType = targetNodeType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected TypeDefinition build() throws Exception {
        DeployUnit deployUnit = factory.createDeployUnit();
        if (groupName != null) {
            deployUnit.setGroupName(groupName);
        } else {
            throw new Exception("GroupName must be specified");
        }
        if (name != null) {
            deployUnit.setName(name);
        } else {
            throw new Exception("Name must be specified");
        }
        if (version != null) {
            deployUnit.setVersion(version);
        } else {
            throw new Exception("Version must be specified");
        }
        if (hashCode != null) {
            deployUnit.setHashcode(hashCode);
        } else {
            throw new Exception("HashCode must be specified");
        }
        if (targetNodeType != null) {
            deployUnit.setTargetNodeType(targetNodeType);
        } else {
            throw new Exception("TargetNodeType must be specified");
        }
        if (type != null) {
            deployUnit.setType(type);
        } else {
            throw new Exception("Type must be specified");
        }
        if (url != null) {
            deployUnit.setUrl(url);
        } else {
            throw new Exception("Url must be specified");
        }
        return null;
    }
}
