package org.kevoree.kompare.tests.model.builder.dictionary;

import org.kevoree.ContainerNode;
import org.kevoree.DictionaryAttribute;
import org.kevoree.DictionaryValue;
import org.kevoree.KevoreeFactory;
import org.kevoree.kompare.tests.model.builder.Builder;

import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 10:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DictionaryValueBuilder extends Builder<DictionaryValue> {

    private DictionaryAttribute attribute;
    private String value;
    private ContainerNode targetNode;

    private Random random = new Random();


    protected DictionaryValueBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public DictionaryValueBuilder setAttribute(DictionaryAttribute attribute) {
        this.attribute = attribute;
        return this;
    }

    public DictionaryValueBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    public DictionaryValueBuilder setTargetNode(ContainerNode targetNode) {
        this.targetNode = targetNode;
        return this;
    }

    @Override
    protected DictionaryValue build() throws Exception {
        DictionaryValue dictionaryValue = factory.createDictionaryValue();
        if (attribute != null) {
            dictionaryValue.setAttribute(attribute);
        } else {
            throw new Exception("Attribute must be specified");
        }
        if (value != null) {
            dictionaryValue.setValue(value);
        } else {
            throw new Exception("Value must be specified");
        }

        if (attribute.getFragmentDependant() && targetNode != null) {
            dictionaryValue.setTargetNode(targetNode);
        } else if (attribute.getFragmentDependant()) {
            throw new Exception("TargetNode must be specified because the Attribute is fragment dependant");
        }

        dictionaryValue.setGenerated_KMF_ID(random.nextInt() + "");
        return dictionaryValue;
    }
}
