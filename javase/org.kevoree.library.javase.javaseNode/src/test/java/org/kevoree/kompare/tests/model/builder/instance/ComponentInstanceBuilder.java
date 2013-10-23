package org.kevoree.kompare.tests.model.builder.instance;

import org.kevoree.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ComponentInstanceBuilder extends InstanceBuilder {
    protected ComponentInstanceBuilder(KevoreeFactory factory) {
        super(factory);
    }

    @Override
    public InstanceBuilder setTypeDefinition(TypeDefinition typeDefinition) {
        if (typeDefinition instanceof ComponentType) {
            this.typeDefinition = typeDefinition;
        } else {
            System.out.println("WARN !! The Type definition which is given as parameter to build ComponentInstance is not a ComponentType: " + typeDefinition);
        }
        return this;
    }

    @Override
    protected Instance build() throws Exception {
        instance = factory.createComponentInstance();
//        ((ComponentInstance)instance).
        // TODO set required and provided ports
        return super.build();
    }
}
