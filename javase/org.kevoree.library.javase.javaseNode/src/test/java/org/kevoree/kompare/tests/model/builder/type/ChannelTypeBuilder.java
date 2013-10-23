package org.kevoree.kompare.tests.model.builder.type;

import org.kevoree.KevoreeFactory;
import org.kevoree.TypeDefinition;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:49
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ChannelTypeBuilder extends TypeDefinitionBuilder {
    protected ChannelTypeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    @Override
    protected TypeDefinition build() throws Exception {
        typeDefinition = factory.createChannelType();
//        ((ChannelType)typeDefinition).
                // TODO define lower and uppper bounds
        return super.build();
    }
}
