package org.kevoree.kompare.tests.model.builder.instance;

import org.kevoree.Dictionary;
import org.kevoree.Instance;
import org.kevoree.KevoreeFactory;
import org.kevoree.TypeDefinition;
import org.kevoree.kompare.tests.model.builder.Builder;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class InstanceBuilder extends Builder<Instance> {
    protected Instance instance;

    private Dictionary dictionary;
    private boolean started = true;
    protected TypeDefinition typeDefinition;

    protected InstanceBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public InstanceBuilder setInstance(Instance instance) {
        this.instance = instance;
        return this;
    }

    public InstanceBuilder setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
        return this;
    }

    public InstanceBuilder setStarted(boolean started) {
        this.started = started;
        return this;
    }

    public abstract InstanceBuilder setTypeDefinition(TypeDefinition typeDefinition);

    @Override
    protected Instance build() throws Exception {
        if (name != null) {
            instance.setName(name);
        } else {
            throw new Exception("Name must be specified");
        }
        if (dictionary != null) {
            instance.setDictionary(dictionary);
        } else {
            throw new Exception("Name must be specified");
        }
        if (typeDefinition != null) {
            instance.setTypeDefinition(typeDefinition);
        } else {
            throw new Exception("Name must be specified");
        }
        instance.setStarted(started);
        return instance;
    }
}
