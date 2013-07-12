package org.kevoree.library.javase.voldemort.pojo;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 11/07/13
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
public class Store {
    private  String name ="kevoree";
    private  String routing = "server";



    public String toString(){
        StringBuilder storeconf = new StringBuilder();
        storeconf.append("\n<store>\n");
        storeconf.append("<name>"+name+"</name>\n");
        storeconf.append("<persistence>bdb</persistence>\n");
        storeconf.append("<routing>server</routing>\n");
        storeconf.append("<replication-factor>10</replication-factor>\n");
        storeconf.append("<required-reads>1</required-reads>\n");
        storeconf.append("<required-writes>1</required-writes>\n");
        storeconf.append("<key-serializer>\n");
        storeconf.append("<type>string</type>\n");
        storeconf.append("</key-serializer>\n");
        storeconf.append(" <value-serializer>\n");
        storeconf.append(" <type>string</type>\n");
        storeconf.append(" </value-serializer>\n");
        storeconf.append(" <retention-days>1</retention-days>\n");
        storeconf.append("</store>\n");
        return  storeconf.toString();
    }
}
