package org.kevoree.kompare.tests.model.builder;

import org.kevoree.KevoreeFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class Builder<T> {

    protected KevoreeFactory factory;

    protected String name;

    protected Builder(KevoreeFactory factory) {
        this.factory = factory;
    }

    public Builder setName(String name) {
        this.name = name;
        return this;
    }

    protected abstract T build() throws Exception;
}
