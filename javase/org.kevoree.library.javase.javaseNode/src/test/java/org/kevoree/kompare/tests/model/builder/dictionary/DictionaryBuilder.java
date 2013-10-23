package org.kevoree.kompare.tests.model.builder.dictionary;

import org.kevoree.Dictionary;
import org.kevoree.DictionaryValue;
import org.kevoree.KevoreeFactory;
import org.kevoree.kompare.tests.model.builder.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 10:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DictionaryBuilder extends Builder<Dictionary> {
    private Random random = new Random();

    private List<DictionaryValue> values = new ArrayList<DictionaryValue>();

    protected DictionaryBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public DictionaryBuilder addValue(DictionaryValue value) {
        values.add(value);
        return this;
    }

    public DictionaryBuilder removeValue(DictionaryValue value) {
        values.remove(value);
        return this;
    }

    @Override
    protected Dictionary build() throws Exception {
        Dictionary dictionary = factory.createDictionary();
        dictionary.setValues(values);
        dictionary.setGenerated_KMF_ID(random.nextInt() + "");
        return dictionary;
    }
}
