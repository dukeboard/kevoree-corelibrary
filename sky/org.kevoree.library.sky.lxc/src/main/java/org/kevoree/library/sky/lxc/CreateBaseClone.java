package org.kevoree.library.sky.lxc;

import org.kevoree.ContainerRoot;

import org.kevoree.api.service.core.handler.ModelHandlerLockCallBack;
import org.kevoree.log.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 08/07/13
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
public class CreateBaseClone implements Runnable {

    private LxcHostNode lxcHostNode;
    private LxcManager lxcManager;

    public CreateBaseClone(LxcHostNode lxcHostNode, LxcManager lxcManager) {
        this.lxcHostNode = lxcHostNode;
        this.lxcManager = lxcManager;
    }

    @Override
    public void run() {

        lxcHostNode.getModelService().acquireLock(new ModelHandlerLockCallBack() {
            @Override
            public void lockTimeout() {
                Log.error("Time out clone creation");
            }

            @Override
            public void lockRejected() {
                Log.error("lockRejected clone creation");
            }

            @Override
            public void lockAcquired(UUID uuid) {
                Log.debug("lockAcquired lxc clone creation");
                // install scripts
                try {
                    lxcManager.install();
                    lxcManager.createClone();
                } catch (Exception e) {
                    Log.error("The creation of the base rootfs has failed");
                } finally {
                    lxcHostNode.getModelService().releaseLock(uuid);
                }
            }
        },LxcHostNode.CREATE_CLONE_TIMEOUT);

    }
}
