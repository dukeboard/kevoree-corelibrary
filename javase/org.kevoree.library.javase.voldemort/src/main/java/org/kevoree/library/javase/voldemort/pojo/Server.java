package org.kevoree.library.javase.voldemort.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 11/07/13
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
public class Server {

    private int id;
    private String host;
    private int httpport;
    private int socketport;
    private int adminport;
    private String partitions="";

    public Server(int id, String host, int httpport,int socketport,int adminport){
        this.id = id;
        this.host = host;
        this.httpport = httpport;
        this.socketport = socketport;
        this.adminport = adminport;
    }


    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }

    public int getId() {
        return id;
    }

    public String toString(){
        StringBuilder clusterxml = new StringBuilder();
        StringBuilder serverpropeties = new StringBuilder();
        clusterxml.append("<server>\n");
        clusterxml.append("<id>" + getId() + "</id>\n");
        clusterxml.append("<host>" + host + "</host>\n");
        clusterxml.append("<http-port>" + httpport + "</http-port>\n");
        clusterxml.append("<socket-port>" + socketport + "</socket-port>\n");
        clusterxml.append("<admin-port>" + adminport + "</admin-port>\n");
        clusterxml.append("<partitions>"+getPartitions());

        clusterxml.append("</partitions>\n");
        clusterxml.append("</server>");
        return  clusterxml.toString();
    }

}
