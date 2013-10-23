package org.kevoree.kompare.tests.model.builder.instance;

import org.kevoree.Channel;
import org.kevoree.Instance;
import org.kevoree.KevoreeFactory;
import org.kevoree.TypeDefinition;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 09:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ChannelInstanceBuilder extends InstanceBuilder {
    protected ChannelInstanceBuilder(KevoreeFactory factory) {
        super(factory);
    }

    @Override
    public InstanceBuilder setTypeDefinition(TypeDefinition typeDefinition) {
        if (typeDefinition instanceof Channel) {
            this.typeDefinition = typeDefinition;
        } else {
            System.out.println("WARN !! The Type definition which is given as parameter to build ComponentInstance is not a ComponentType: " + typeDefinition);
        }
        return this;
    }

    @Override
    protected Instance build() throws Exception {
        instance = factory.createComponentInstance();
//        ((Channel)instance).
        // TODO set bindings
        return super.build();
    }
}
