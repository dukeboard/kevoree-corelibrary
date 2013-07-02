package org.kevoree.library.sky.provider;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sky.provider.api.IaaSManagerService;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.kevoree.log.Log;
import scala.Option;

import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/01/12
 * Time: 10:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentType
@Provides({
        @ProvidedPort(name = "submit", type = PortType.SERVICE, className = IaaSManagerService.class)
})
public class IaaSKloudManager extends AbstractComponentType implements ModelListener, IaaSManagerService {

    @Start
    public void start() {
        this.getModelService().registerModelListener(this);
    }

    @Stop
    public void stop() {
        this.getModelService().unregisterModelListener(this);
    }

    @Update
    public void update() {
        stop();
        start();
    }


    @Override
    public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
        return true;
    }

    @Override
    public void modelUpdated() {
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.configureChildNodes(getModelService().getLastModel(), kengine) || IaaSKloudReasoner.configureIsolatedNodes(getModelService().getLastModel(), kengine)) {
            this.getModelService().unregisterModelListener(this);
            try {
                updateIaaSConfiguration(kengine);
            } catch (SubmissionException e) {
                Log.error("Unable to update infrastructure", e);
            }
            this.getModelService().registerModelListener(this);
        }
    }

    @Override
    public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
    }

    @Override
    public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
    }

    private void updateIaaSConfiguration(KevScriptEngine kengine) throws SubmissionException {
        Boolean created = false;
        for (int i = 0; i < 20; i++) {
            try {
                Log.debug("try to update IaaS node...");
                kengine.atomicInterpretDeploy();
                created = true;
                break;
            } catch (Exception e) {
                Log.warn("Error while try to update the IaaS configuration due to {}, try number {}", e.getMessage(), i);
            }
        }
        if (!created) {
            Log.error("After 20 attempt, it was not able to update the IaaS configuration");
            throw new SubmissionException("Unable to apply the request");
        }
    }

    @Override
    @Port(name = "submit", method = "add")
    public void add(ContainerRoot model) throws SubmissionException {
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        Option<String> none = Option.apply(null);
        if (IaaSKloudReasoner.addNodes(model.getNodes(), none, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "addToNode")
    public void addToNode(ContainerRoot model, String nodeName) throws SubmissionException {
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        Option<String> some = Option.apply(nodeName);
        if (IaaSKloudReasoner.addNodes(model.getNodes(), some, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "remove")
    public void remove(ContainerRoot model) throws SubmissionException {
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.removeNodes(model.getNodes(), getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "merge")
    public void merge(ContainerRoot model) throws SubmissionException {
        Option<String> none = Option.apply(null);
        List<ContainerNode> nodesToAdd = PaaSKloudReasoner.getNodesToAdd(getModelService().getLastModel(), model);
        List<ContainerNode> nodesToRemove = PaaSKloudReasoner.getNodesToRemove(getModelService().getLastModel(), model);
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.removeNodes(nodesToRemove, getModelService().getLastModel(), kengine) || IaaSKloudReasoner.addNodes(nodesToAdd, none, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "mergeToNode")
    public void mergeToNode(ContainerRoot model, String nodeName) throws SubmissionException {
        Option<String> some = Option.apply(nodeName);
        List<ContainerNode> nodesToAdd = PaaSKloudReasoner.getNodesToAdd(getModelService().getLastModel(), model);
        List<ContainerNode> nodesToRemove = PaaSKloudReasoner.getNodesToRemove(getModelService().getLastModel(), model);
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.removeNodes(nodesToRemove, getModelService().getLastModel(), kengine) || IaaSKloudReasoner.addNodes(nodesToAdd, some, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "start")
    public void start(ContainerRoot model) throws SubmissionException {
        Option<String> none = Option.apply(null);
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.startNodes(model.getNodes(), none, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "startToNode")
    public void startToNode(ContainerRoot model, String nodeName) throws SubmissionException {
        Option<String> some = Option.apply(nodeName);
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.startNodes(model.getNodes(), some, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "stop")
    public void stop(ContainerRoot model) throws SubmissionException {
        Option<String> none = Option.apply(null);
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.stopNodes(model.getNodes(), none, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }

    @Override
    @Port(name = "submit", method = "stopToNode")
    public void stopToNode(ContainerRoot model, String nodeName) throws SubmissionException {
        Option<String> some = Option.apply(nodeName);
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        if (IaaSKloudReasoner.stopNodes(model.getNodes(), some, getModelService().getLastModel(), kengine)) {
            updateIaaSConfiguration(kengine);
        }
    }
}