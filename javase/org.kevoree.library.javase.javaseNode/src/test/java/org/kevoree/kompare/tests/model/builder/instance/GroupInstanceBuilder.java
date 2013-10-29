package org.kevoree.kompare.tests.model.builder.instance;

import org.kevoree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 09:41
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class GroupInstanceBuilder extends InstanceBuilder {
    protected GroupInstanceBuilder(KevoreeFactory factory) {
        super(factory);
    }

    List<ContainerNode> subNodes = new ArrayList<ContainerNode>();

    @Override
    public InstanceBuilder setTypeDefinition(TypeDefinition typeDefinition) {
        if (typeDefinition instanceof Group) {
            this.typeDefinition = typeDefinition;
        } else {
            System.out.println("WARN !! The Type definition which is given as parameter to build ComponentInstance is not a ComponentType: " + typeDefinition);
        }
        return this;
    }

    public GroupInstanceBuilder addSubNode(ContainerNode subNode) {
        subNodes.add(subNode);
        return this;
    }

    public GroupInstanceBuilder removeSubNode(ContainerNode subNode) {
        subNodes.remove(subNode);
        return this;
    }

    @Override
    protected Instance build() throws Exception {
        instance = factory.createComponentInstance();
//        ((Group)instance).
        // TODO set sub nodes
        return super.build();
    }
}
