package org.kevoree.kompare.tests.model.builder.dictionary;

import org.kevoree.DictionaryAttribute;
import org.kevoree.DictionaryType;
import org.kevoree.DictionaryValue;
import org.kevoree.KevoreeFactory;
import org.kevoree.kompare.tests.model.builder.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 09:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DictionaryTypeBuilder extends Builder<DictionaryType> {

    private Random random = new Random();

    private List<DictionaryAttribute> attributes = new ArrayList<DictionaryAttribute>();
    private List<DictionaryValue> defaultValues = new ArrayList<DictionaryValue>();

    protected DictionaryTypeBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public DictionaryTypeBuilder addAttribute(DictionaryAttribute attribute) {
        attributes.add(attribute);
        return this;
    }

    public DictionaryTypeBuilder removeAttribute(DictionaryAttribute attribute) {
        attributes.remove(attribute);
        return this;
    }

    public DictionaryTypeBuilder addDefaultValue(DictionaryValue value) {
        if (attributes.contains(value.getAttribute())) {
            defaultValues.add(value);
        }
        return this;
    }

    public DictionaryTypeBuilder removeDefaultValue(DictionaryValue value) {
        defaultValues.remove(value);
        return this;
    }


    @Override
    protected DictionaryType build() throws Exception {
        DictionaryType dictionaryType = factory.createDictionaryType();
        dictionaryType.setAttributes(attributes);
        dictionaryType.setDefaultValues(defaultValues);
        dictionaryType.setGenerated_KMF_ID(random.nextInt() + "");
        return dictionaryType;
    }
}
