package org.kevoree.library.javase.voldemort;

import org.kevoree.library.javase.voldemort.pojo.Server;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 10/07/13
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class Tester {

    public static  void main(String argv[]) throws IOException, InterruptedException {



        VoldemortManager t = new VoldemortManager();
        t.setId(0);


        t.install();
        Server server1 = new Server(0,"localhost",8081,6666,6667);
        server1.setPartitions("0,1");

        t.addServer(server1);


        t.start();
        Thread.sleep(8000);

        Server server2 = new Server(1,"localhost",8081,6666,6667);
        server2.setPartitions("2,3");
        t.addServer(server2);

            t.update();


        Thread.sleep(8000000);



        t.stop();


    }
}
