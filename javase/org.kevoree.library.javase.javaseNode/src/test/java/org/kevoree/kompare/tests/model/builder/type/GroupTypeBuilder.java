package org.kevoree.kompare.tests.model.builder.type;

import org.kevoree.KevoreeFactory;
import org.kevoree.TypeDefinition;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class GroupTypeBuilder extends TypeDefinitionBuilder {
    protected GroupTypeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    @Override
    protected TypeDefinition build() throws Exception {
        typeDefinition = factory.createGroupType();
        return super.build();
    }
}
