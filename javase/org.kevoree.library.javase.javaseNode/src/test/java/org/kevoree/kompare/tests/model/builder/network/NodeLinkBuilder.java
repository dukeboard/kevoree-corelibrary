package org.kevoree.kompare.tests.model.builder.network;

import org.kevoree.KevoreeFactory;
import org.kevoree.NodeLink;
import org.kevoree.kompare.tests.model.builder.Builder;

import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/10/13
 * Time: 10:01
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NodeLinkBuilder extends Builder<NodeLink> {

    private int estimateRate;
    private String lastCheck;
    private String networkType;
    private String zoneID;

    private Random random = new Random();

    protected NodeLinkBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public void setEstimateRate(int estimateRate) {
        this.estimateRate = estimateRate;
    }

    public void setLastCheck(String lastCheck) {
        this.lastCheck = lastCheck;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public void setZoneID(String zoneID) {
        this.zoneID = zoneID;
    }

    @Override
    protected NodeLink build() throws Exception {
        NodeLink nodeLink = factory.createNodeLink();
        nodeLink.setEstimatedRate(estimateRate);

        if (lastCheck != null) {
            nodeLink.setLastCheck(lastCheck);
        }/* else {
            throw new Exception("LastCheck must be specified");
        }*/
        if (networkType != null) {
            nodeLink.setNetworkType(networkType);
        } else {
            throw new Exception("NetworkType must be specified");
        }
        if (zoneID != null) {
            nodeLink.setZoneID(zoneID);
        }/* else {
            throw new Exception("LastCheck must be specified");
        }*/
        nodeLink.setGenerated_KMF_ID(random.nextInt() + "");
        // TODO ad NetworkProperty
        return nodeLink;
    }
}
