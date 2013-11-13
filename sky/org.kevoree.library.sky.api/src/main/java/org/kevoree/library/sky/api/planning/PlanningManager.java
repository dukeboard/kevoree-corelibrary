package org.kevoree.library.sky.api.planning;


import jet.runtime.typeinfo.JetValueParameter;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.library.defaultNodeTypes.planning.JavaSePrimitive;
import org.kevoree.library.defaultNodeTypes.planning.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.planning.scheduling.SchedulingWithTopologicalOrderAlgo;
import org.kevoree.library.sky.api.CloudNode;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.trace.ModelAddTrace;
import org.kevoree.modeling.api.trace.ModelRemoveTrace;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;
import org.kevoreeadaptation.ParallelStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/12/11
 * Time: 09:19
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public class PlanningManager extends KevoreeKompareBean {
    private AbstractNodeType skyNode;

    public PlanningManager(AbstractNodeType skyNode, Map<String, Object> registry) {
        super(registry);
        this.skyNode = skyNode;
    }

    @Override
    protected void manageModelAddTrace(@JetValueParameter(name = "trace") ModelAddTrace trace, @JetValueParameter(name = "currentNode") ContainerNode currentNode, @JetValueParameter(name = "currentModel") ContainerRoot currentModel, @JetValueParameter(name = "targetNode") ContainerNode targetNode, @JetValueParameter(name = "targetModel") ContainerRoot targetModel, @JetValueParameter(name = "adaptationModel") AdaptationModel adaptationModel) {
        if (trace.getSrcPath().equals(currentNode.path()) && trace.getRefName().equals("hosts")) {
            ContainerNode node = targetModel.findByPath(trace.getPreviousPath(), ContainerNode.class);
            addNodeInstance(node, currentModel, targetModel, adaptationModel);
        } else {
            super.manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel);
        }
    }

    @Override
    protected void manageModelRemoveTrace(@JetValueParameter(name = "trace") ModelRemoveTrace trace, @JetValueParameter(name = "currentNode") ContainerNode currentNode, @JetValueParameter(name = "currentModel") ContainerRoot currentModel, @JetValueParameter(name = "targetNode") ContainerNode targetNode, @JetValueParameter(name = "targetModel") ContainerRoot targetModel, @JetValueParameter(name = "adaptationModel") AdaptationModel adaptationModel) {
        if (trace.getSrcPath().equals(currentNode.path()) &&  trace.getRefName().equals("hosts")) {
            ContainerNode node = currentModel.findByPath(trace.getObjPath(), ContainerNode.class);
            removeNodeInstance(node, currentModel, adaptationModel);
        } else {
            super.manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel);
        }
    }

    private void addNodeInstance(ContainerNode subNode, ContainerRoot currentModel, ContainerRoot targetModel, AdaptationModel adaptationModel) {
        Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.ADD_NODE, subNode.getName());
        AdaptationPrimitive command = getAdaptationModelFactory().createAdaptationPrimitive();
        command.setPrimitiveType(currentModel.findAdaptationPrimitiveTypesByID(CloudNode.ADD_NODE));
        command.setRef(subNode);
        adaptationModel.addAdaptations(command);
        processStartInstance(subNode, adaptationModel, targetModel);
    }

    private void removeNodeInstance(ContainerNode subNode, ContainerRoot currentModel, AdaptationModel adaptationModel) {
        Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.REMOVE_NODE, subNode.getName());
        AdaptationPrimitive command = getAdaptationModelFactory().createAdaptationPrimitive();
        command.setPrimitiveType(currentModel.findAdaptationPrimitiveTypesByID(CloudNode.REMOVE_NODE));
        command.setRef(subNode);
        adaptationModel.addAdaptations(command);
        processStopInstance(subNode, adaptationModel, currentModel);
    }

    private void processStopInstance(Instance actualInstance, AdaptationModel adaptationModel, ContainerRoot actualRoot) {
        Log.debug("Process StopInstance on {}", actualInstance.getName());
        AdaptationPrimitive ccmd2 = getAdaptationModelFactory().createAdaptationPrimitive();
        ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance));
        ccmd2.setRef(actualInstance);
        adaptationModel.addAdaptations(ccmd2);
    }

    private void processStartInstance(Instance updatedInstance, AdaptationModel adaptationModel, ContainerRoot updateRoot) {
        Log.debug("Process StartInstance on {}", updatedInstance.getName());
        AdaptationPrimitive ccmd2 = getAdaptationModelFactory().createAdaptationPrimitive();
        ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance));
        ccmd2.setRef(updatedInstance);
        adaptationModel.addAdaptations(ccmd2);
    }

    @Override
    public AdaptationModel schedule(AdaptationModel adaptationModel, String nodeName) {
        if (!adaptationModel.getAdaptations().isEmpty()) {

            Log.debug("Planning adaptation and defining steps...");
            SchedulingWithTopologicalOrderAlgo scheduling = new SchedulingWithTopologicalOrderAlgo();
            nextStep();
            adaptationModel.setOrderedPrimitiveSet(getCurrentSteps());

            // STOP child nodes
            List<AdaptationPrimitive> primitives = new ArrayList<AdaptationPrimitive>();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.StopInstance) && primitive.getRef() instanceof ContainerNode) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.StopInstance, primitives);

            // REMOVE child nodes
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(CloudNode.REMOVE_NODE)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(CloudNode.REMOVE_NODE, primitives);

            //STOP INSTANCEs (except child node)
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.StopInstance) && !(primitive.getRef() instanceof ContainerNode)) {
                    primitives.add(primitive);
                }
            }
            ParallelStep stepToInsert = scheduling.schedule(primitives, false);
            if (stepToInsert != null && !stepToInsert.getAdaptations().isEmpty()) {
                insertStep(stepToInsert);
            }

            // REMOVE BINDINGS
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveBinding) ||
                        primitive.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveFragmentBinding)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.RemoveBinding, primitives);

            // REMOVE INSTANCE
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveInstance)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.RemoveInstance, primitives);

            // REMOVE TYPE
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveType)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.RemoveType, primitives);

            // REMOVE TYPE
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveDeployUnit)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.RemoveDeployUnit, primitives);

            // REMOVE THIRD PARTY
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.AddThirdParty)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.AddThirdParty, primitives);

            // UPDATE DEPLOYUNITs
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.UpdateDeployUnit)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.UpdateDeployUnit, primitives);

            // ADD DEPLOYUNITs
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.AddDeployUnit)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.AddDeployUnit, primitives);

            // ADD TYPEs
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.AddType)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.AddType, primitives);

            // ADD INSTANCEs
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.AddInstance)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.AddInstance, primitives);

            // ADD BINDINGs
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.AddBinding) ||
                        primitive.getPrimitiveType().getName().equals(JavaSePrimitive.AddFragmentBinding)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.AddBinding, primitives);

            // UPDATE DICTIONARY
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.UpdateDictionaryInstance)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.UpdateDictionaryInstance, primitives);


            // ADD child nodes
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(CloudNode.ADD_NODE)) {
                    primitives.add(primitive);
                }
            }
            createNextStep(CloudNode.ADD_NODE, primitives);

            // START child nodes
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.StartInstance) && primitive.getRef() instanceof ContainerNode) {
                    primitives.add(primitive);
                }
            }
            createNextStep(JavaSePrimitive.StartInstance, primitives);

            // START INSTANCEs (except child nodes)
            primitives.clear();
            for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.StartInstance) && !(primitive.getRef() instanceof ContainerNode)) {
                    primitives.add(primitive);
                }
            }
            stepToInsert = scheduling.schedule(primitives, true);
            if (stepToInsert != null && !stepToInsert.getAdaptations().isEmpty()) {
                insertStep(stepToInsert);
            }
        } else {
            adaptationModel.setOrderedPrimitiveSet(null);
        }
        clearSteps();
        return adaptationModel;

    }
}