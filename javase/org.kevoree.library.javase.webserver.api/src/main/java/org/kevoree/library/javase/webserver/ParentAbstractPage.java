package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Update;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/01/12
 * Time: 08:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ComponentType
public abstract class ParentAbstractPage extends AbstractPage {

    @Override
    @Start
    public void startPage() {
        if (!this.getDictionary().get("urlpattern").toString().endsWith("**")) {
            this.getDictionary().put("urlpattern", this.getDictionary().get("urlpattern").toString() + "**");
            Log.debug("Parent abstract page start with pattern = {}", this.getDictionary().get("urlpattern").toString());
        }
        super.startPage();
    }

    @Override
    @Update
    public void updatePage() {
        startPage();
    }
}
