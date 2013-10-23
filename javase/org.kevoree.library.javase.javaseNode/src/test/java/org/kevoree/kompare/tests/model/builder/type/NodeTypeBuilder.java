package org.kevoree.kompare.tests.model.builder.type;

import org.kevoree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:45
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NodeTypeBuilder extends TypeDefinitionBuilder {

    private List<AdaptationPrimitiveType> adaptationPrimitiveTypes = new ArrayList<AdaptationPrimitiveType>();
    private List<AdaptationPrimitiveTypeRef> adaptationPrimitiveTypeRefs = new ArrayList<AdaptationPrimitiveTypeRef>();

    protected NodeTypeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public NodeTypeBuilder addAdaptationPrimitiveType(AdaptationPrimitiveType adaptationPrimitiveType) {
        adaptationPrimitiveTypes.add(adaptationPrimitiveType);
        return this;
    }

    public NodeTypeBuilder removeAdaptationPrimitiveType(AdaptationPrimitiveType adaptationPrimitiveType) {
        adaptationPrimitiveTypes.remove(adaptationPrimitiveType);
        return this;
    }

    public NodeTypeBuilder addAdaptationPrimitiveTypeRef(AdaptationPrimitiveTypeRef adaptationPrimitiveTypeRef) {
        adaptationPrimitiveTypeRefs.add(adaptationPrimitiveTypeRef);
        return this;
    }

    public NodeTypeBuilder removeAdaptationPrimitiveTypeRef(AdaptationPrimitiveTypeRef adaptationPrimitiveTypeRef) {
        adaptationPrimitiveTypeRefs.remove(adaptationPrimitiveTypeRef);
        return this;
    }

    @Override
    protected TypeDefinition build() throws Exception {
        typeDefinition = factory.createNodeType();
        ((NodeType)typeDefinition).setManagedPrimitiveTypes(adaptationPrimitiveTypes);
        ((NodeType)typeDefinition).setManagedPrimitiveTypeRefs(adaptationPrimitiveTypeRefs);
        return super.build();
    }
}
