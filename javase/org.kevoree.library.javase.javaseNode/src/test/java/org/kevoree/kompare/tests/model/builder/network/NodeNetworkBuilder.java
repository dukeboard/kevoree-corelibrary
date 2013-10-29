package org.kevoree.kompare.tests.model.builder.network;

import org.kevoree.ContainerNode;
import org.kevoree.KevoreeFactory;
import org.kevoree.NodeNetwork;
import org.kevoree.kompare.tests.model.builder.Builder;

import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 10:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NodeNetworkBuilder extends Builder<NodeNetwork> {

    private ContainerNode init;
    private ContainerNode target;

    private Random random = new Random();

    protected NodeNetworkBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public void setInit(ContainerNode init) {
        this.init = init;
    }

    public void setTarget(ContainerNode target) {
        this.target = target;
    }

    @Override
    protected NodeNetwork build() throws Exception {
        NodeNetwork nodeNetwork = factory.createNodeNetwork();
        if (init != null) {
            nodeNetwork.setInitBy(init);
        } else {
            throw new Exception("NetworkType must be specified");
        }
        if (target != null) {
            nodeNetwork.setTarget(target);
        } else {
            throw new Exception("NetworkType must be specified");
        }
        nodeNetwork.setGenerated_KMF_ID(random.nextInt() + "");
        // TODO add NodeLinks
        return null;
    }
}
