package org.kevoree.kompare.tests.model.builder.network;

import org.kevoree.KevoreeFactory;
import org.kevoree.NetworkProperty;
import org.kevoree.kompare.tests.model.builder.Builder;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 10:00
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NetworkPropertyBuilder extends Builder<NetworkProperty> {

    private String lastCheck;
    private String value;

    protected NetworkPropertyBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public void setLastCheck(String lastCheck) {
        this.lastCheck = lastCheck;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected NetworkProperty build() throws Exception {
        NetworkProperty networkProperty = factory.createNetworkProperty();
        if (lastCheck != null) {
            networkProperty.setLastCheck(lastCheck);
        } else {
            throw new Exception("LastCheck must be specified");
        }
        if (value != null) {
            networkProperty.setValue(value);
        } else {
            throw new Exception("Value must be specified");
        }
        if (name != null) {
            networkProperty.setName(name);
        } else {
            throw new Exception("Name must be specified");
        }
        return networkProperty;
    }
}
