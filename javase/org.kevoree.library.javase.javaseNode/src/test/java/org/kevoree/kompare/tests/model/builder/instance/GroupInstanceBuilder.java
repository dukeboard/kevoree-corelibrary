package org.kevoree.kompare.tests.model.builder.instance;

import org.kevoree.*;

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

    @Override
    public InstanceBuilder setTypeDefinition(TypeDefinition typeDefinition) {
        if (typeDefinition instanceof Group) {
            this.typeDefinition = typeDefinition;
        } else {
            System.out.println("WARN !! The Type definition which is given as parameter to build ComponentInstance is not a ComponentType: " + typeDefinition);
        }
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
