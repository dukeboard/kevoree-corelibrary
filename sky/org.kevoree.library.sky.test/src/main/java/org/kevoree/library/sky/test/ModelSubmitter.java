package org.kevoree.library.sky.test;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/11/12
 * Time: 15:51
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "Test")
@DictionaryType({
        @DictionaryAttribute(name = "model", optional = true, defaultValue = "/home/edaubert/Documents/svns/kevoree_gforge/erwan_thesis/valid/thesis_validation4000.kev")
})
@ComponentType
public class ModelSubmitter extends AbstractComponentType {
    private Thread t;

    @Start
    public void start() throws KevScriptEngineException {
        if (t == null) {
            t = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    KevoreeXmiHelper.instance$.save(ModelSubmitter.this.getDictionary().get("model").toString() + "-base.kev", getModelService().getLastModel());
                    ContainerRoot model = KevoreeXmiHelper.instance$.load(ModelSubmitter.this.getDictionary().get("model").toString());
                    try {
                        Log.warn("[TIME] ModelSubmitter submit model: {}", System.currentTimeMillis());
//					ModelSubmitter.this.getPortByName("deploy", PaaSManagerService.class).initialize("edaubert", model);
                        updateAndCheckIaaSConfiguration(model);
                    } catch (SubmissionException e) {
                        Log.error("Unable to initialize model on Cloud", e);
                    }
                }
            };
            t.start();
        }
    }

    @Stop
    public void stop() throws KevScriptEngineException {
    }

    @Update
    public void update() throws KevScriptEngineException {
    }

    private void updateAndCheckIaaSConfiguration(ContainerRoot model) throws SubmissionException {
        Boolean created = false;
        for (int i = 0; i < 20; i++) {
            try {
                Log.debug("try to update IaaS node...");
                // TODO measure time
                Log.warn("[TIME] ModelSubmitter submit new model: {}", System.currentTimeMillis());
//				kengine.atomicInterpretDeploy();
                getModelService().atomicUpdateModel(model);

                created = true;
                // check all nodes are available
                ContainerRoot currentModel = getModelService().getLastModel();
                for (ContainerNode node : model.getNodes()) {
                    ContainerNode addedNode = currentModel.findByPath(node.path(), ContainerNode.class);
                    if (addedNode == null) {
                        Log.error("Node {} has not been added on the current model", node.getName());
                        created = false;
                    }
                }
                break;
            } catch (Exception e) {
                Log.warn("Error while try to update the IaaS configuration due to {}, try number {}", e.getMessage(), i);
//                KevoreeXmiHelper.instance$.save(ModelSubmitter.this.getDictionary().get("model").toString() + "-try" + i + ".kev", getModelService().getLastModel());
            }
        }
        if (!created) {
            Log.error("After 20 attempt, it was not able to update the IaaS configuration");
            throw new SubmissionException("Unable to apply the request");
        }
    }
}
