package org.kevoree.kompare.tests.model.builder.type;

import org.kevoree.AdaptationPrimitiveType;
import org.kevoree.KevoreeFactory;
import org.kevoree.kompare.tests.model.builder.Builder;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 11:05
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class AdaptationPrimitiveTypeBuilder extends Builder<AdaptationPrimitiveType> {
    protected AdaptationPrimitiveTypeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    @Override
    protected AdaptationPrimitiveType build() throws Exception {
        AdaptationPrimitiveType adaptationPrimitiveType = factory.createAdaptationPrimitiveType();
        if (name != null) {
            adaptationPrimitiveType.setName(name);
        } else {
            throw new Exception("Name must be specified");
        }
        return adaptationPrimitiveType;
    }
}
