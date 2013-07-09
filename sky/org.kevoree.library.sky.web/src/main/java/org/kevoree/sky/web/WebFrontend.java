package org.kevoree.sky.web;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.log.Log;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.EmbeddedResourceHandler;
import org.webbitserver.handler.StaticFileHandler;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 09/07/13
 * Time: 11:52
 */

@Library(name = "Sky")
@ComponentType
public class WebFrontend extends AbstractComponentType {

    private WebServer webServer;
    private ModelServiceSocketHandler mhandler = null;

    @Start
    public void startServer() {
        try {
            mhandler = new ModelServiceSocketHandler(this.getModelService());
            webServer = WebServers.createWebServer(8080)
                    .add(new MetaDataHandler(this.getModelService()))
                    .add("/model/service", mhandler)
                   // .add(new StaticFileHandler("/Users/duke/Documents/dev/dukeboard/kevoree-corelibrary/sky/org.kevoree.library.sky.web/src/main/resources")) // path to web content
                    .add(new EmbedHandler()) // path to web content
                    .start()
                    .get();
        } catch (Exception e) {
            Log.error("Error while starting Kloud Web front end", e);
        }
    }

    @Stop
    public void stopServer() {
        webServer.stop();
        mhandler.destroy();
    }

}
