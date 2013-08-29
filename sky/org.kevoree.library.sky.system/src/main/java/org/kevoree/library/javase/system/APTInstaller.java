package org.kevoree.library.javase.system;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/07/13
 * Time: 14:30
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class APTInstaller extends SystemCommand {

    @Override
    @Start
    public void startSystemComponent() throws Exception {
        this.getDictionary().put("START_COMMAND", "sudo DEBIAN_FRONTEND=noninteractive apt-get install -y " + this.getDictionary().get("START_COMMAND").toString());
        this.getDictionary().put("STOP_COMMAND", "sudo DEBIAN_FRONTEND=noninteractive apt-get autoremove --purge -y" + this.getDictionary().get("START_COMMAND").toString());
        super.startSystemComponent();
    }

    @Override
    @Stop
    public void stopSystemComponent() throws Exception {
        super.stopSystemComponent();
    }
}
