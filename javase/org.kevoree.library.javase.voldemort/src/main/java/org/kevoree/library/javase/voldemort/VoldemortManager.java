package org.kevoree.library.javase.voldemort;

import org.kevoree.library.javase.voldemort.pojo.Server;
import org.kevoree.library.javase.voldemort.pojo.Store;
import org.kevoree.library.javase.voldemort.utils.FileManager;
import org.kevoree.library.javase.voldemort.utils.SystemHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 10/07/13
 * Time: 18:06
 * To change this template use File | Settings | File Templates.
 */
public class VoldemortManager {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String current_path="";
    private HashMap<Integer,Server> cluster = new HashMap<Integer, Server>();

    public void copy(String file, String path) throws IOException {
        FileManager.copyFileFromStream(VoldemortManager.class.getClassLoader().getResourceAsStream(file), current_path+path, file, true);
    }

    public void allow_exec(String path_file_exec) throws IOException {
        if (SystemHelper.getOS() != SystemHelper.OS.WIN32 && SystemHelper.getOS() != SystemHelper.OS.WIN64) {
            Runtime.getRuntime().exec("chmod 777 " + path_file_exec);
        } else {
            // win32
            System.err.println("ERROR");
        }
    }

    public void install() throws IOException, InterruptedException {
        current_path =  System.getProperty("java.io.tmpdir")+File.separatorChar+"voldemort"+File.separatorChar+"kevoree"+File.separatorChar;
       FileManager.deleteDir(new File(current_path));
        System.out.println("Install voldemort-1.3.0");
        copy("voldemort-1.3.0.tar.gz", "");
        Process lxcstartprocess = new ProcessBuilder("tar","xf",current_path+"voldemort-1.3.0.tar.gz").directory(new File(current_path)).redirectErrorStream(false).start();

        lxcstartprocess.waitFor();

    }


    public void configure()  {

        File configrep = new File(current_path+File.separatorChar+"voldemort-1.3.0"+File.separatorChar+"config"+File.separatorChar+"kevoree_config"+File.separatorChar+"config");
        configrep.mkdirs();
        //  copy("stores.xml", "kevoree_config"+File.separatorChar+"config");

        StringBuilder serverproperties = new StringBuilder();

        serverproperties.append("node.id="+id+"\n");

        serverproperties.append("max.threads=100\n");
        serverproperties.append("enable.repair=true\n");

        serverproperties.append("http.enable=true\n");
        serverproperties.append("socket.enable=true\n");
        serverproperties.append("jmx.enable=true\n");


        serverproperties.append("#BDB\n");
        serverproperties.append("bdb.write.transactions=true\n");
        serverproperties.append("bdb.flush.transactions=false\n");
        serverproperties.append("bdb.cache.size=100MB\n");

        if(cluster.size()  == 1){
            serverproperties.append("enable.nio.connector=true\n");
            serverproperties.append("request.format=vp3\n");
            serverproperties.append("storage.configs=voldemort.store.bdb.BdbStorageConfiguration, voldemort.store.readonly.ReadOnlyStorageConfiguration\n");
        }


        StringBuilder clusterxml = new StringBuilder();
        clusterxml.append("<cluster>\n");
        clusterxml.append("<name>kevoree_cluster</name>\n");
        for(Server s : cluster.values()){
            clusterxml.append(s.toString());
        }
        clusterxml.append("</cluster>\n");


        StringBuilder storexml = new StringBuilder();
        Store store = new Store();
        storexml.append("<stores>");
        storexml.append(store.toString());
        storexml.append("</stores>");

        try
        {

            FileManager.writeFile(configrep.getAbsolutePath() + File.separatorChar + "server.properties", serverproperties.toString(), false);
            FileManager.writeFile(configrep.getAbsolutePath()+File.separatorChar+"cluster.xml",clusterxml.toString(),false);
            FileManager.writeFile(configrep.getAbsolutePath()+File.separatorChar+"stores.xml",storexml.toString(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() throws InterruptedException, IOException {
        System.out.println("Starting voldemort-1.3.0");
        stop();
        configure();
        Process lxcstartprocess = new ProcessBuilder(current_path+"voldemort-1.3.0/bin/voldemort-server.sh",current_path+"voldemort-1.3.0/config/kevoree_config").redirectErrorStream(true).start();
        FileManager.display_message_process(lxcstartprocess.getInputStream());
    }

    public void stop() {
        Process lxcstartprocess = null;
        try {
            lxcstartprocess = new ProcessBuilder(current_path+"voldemort-1.3.0/bin/voldemort-stop.sh").redirectErrorStream(true).start();
            try {
                lxcstartprocess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void addServer(Server server){
        cluster.put(server.getId(), server);
    }

    public void removeServer(Server server) {
        cluster.remove(server.getId());
    }

    public void clearConf() {
        cluster.clear();
    }

    public void update() {

        Process lxcstartprocess = null;
        try {
            String currentCluster = current_path+"voldemort-1.3.0/config/kevoree_config_old";
            String targetCluster = current_path+"voldemort-1.3.0/config/kevoree_config";
            FileManager.copyDirectory(new File(targetCluster), new File(currentCluster));
            configure();

            lxcstartprocess = new ProcessBuilder(current_path+"voldemort-1.3.0/bin/voldemort-rebalance.sh","--current-cluster",currentCluster,"--current-stores",currentCluster,"--target-cluster",targetCluster).redirectErrorStream(true).start();

            FileManager.display_message_process(lxcstartprocess.getInputStream());
            try {
                lxcstartprocess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
