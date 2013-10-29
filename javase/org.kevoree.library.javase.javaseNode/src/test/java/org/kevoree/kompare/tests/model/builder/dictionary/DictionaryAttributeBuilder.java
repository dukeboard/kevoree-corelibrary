package org.kevoree.kompare.tests.model.builder.dictionary;

import org.kevoree.DictionaryAttribute;
import org.kevoree.KevoreeFactory;
import org.kevoree.kompare.tests.model.builder.Builder;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 10:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DictionaryAttributeBuilder extends Builder<DictionaryAttribute> {

    private boolean fragmentDependent = false;
    private boolean optional = false;
    private boolean state = false;
    private String dataType;

    protected DictionaryAttributeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public DictionaryAttributeBuilder setFragmentDependent(boolean fragmentDependent) {
        this.fragmentDependent = fragmentDependent;
        return this;
    }

    public DictionaryAttributeBuilder setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public DictionaryAttributeBuilder setState(boolean state) {
        this.state = state;
        return this;
    }

    public DictionaryAttributeBuilder setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    @Override
    protected DictionaryAttribute build() throws Exception {
        DictionaryAttribute dictionaryAttribute = factory.createDictionaryAttribute();

        dictionaryAttribute.setFragmentDependant(fragmentDependent);
        dictionaryAttribute.setOptional(optional);
        dictionaryAttribute.setState(state);
        if (dataType != null) {
            dictionaryAttribute.setDatatype(dataType);
        }/* else {
            throw new Exception("Name must be specified");
        }*/

        if (name != null) {
            dictionaryAttribute.setName(name);
        } else {
            throw new Exception("Name must be specified");
        }
        return dictionaryAttribute;
    }
}
