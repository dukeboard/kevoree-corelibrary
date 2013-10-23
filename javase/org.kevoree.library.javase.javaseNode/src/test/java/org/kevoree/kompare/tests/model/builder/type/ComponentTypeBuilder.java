package org.kevoree.kompare.tests.model.builder.type;

import org.kevoree.KevoreeFactory;
import org.kevoree.TypeDefinition;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ComponentTypeBuilder extends TypeDefinitionBuilder {
    protected ComponentTypeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    @Override
    protected TypeDefinition build() throws Exception {
        typeDefinition = factory.createComponentType();
//                ((ComponentType)typeDefinition).
        // TODO specify required and provided port
        return super.build();
    }
}
