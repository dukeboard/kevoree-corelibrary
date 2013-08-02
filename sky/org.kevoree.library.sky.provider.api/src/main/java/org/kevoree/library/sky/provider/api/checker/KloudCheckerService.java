package org.kevoree.library.sky.provider.api.checker;

import org.kevoree.api.service.core.checker.CheckerService;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 13:59
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public abstract class KloudCheckerService implements CheckerService{

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
