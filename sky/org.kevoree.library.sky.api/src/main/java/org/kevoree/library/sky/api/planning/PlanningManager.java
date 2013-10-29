package org.kevoree.library.sky.api.planning;


import org.kevoree.AdaptationPrimitiveType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.library.defaultNodeTypes.planning.JavaSePrimitive;
import org.kevoree.library.defaultNodeTypes.planning.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.planning.scheduling.SchedulingWithTopologicalOrderAlgo;
import org.kevoree.library.sky.api.CloudNode;
import org.kevoree.log.Log;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;
import org.kevoreeadaptation.KevoreeAdaptationFactory;
import org.kevoreeadaptation.ParallelStep;
import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory;

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

    public AdaptationModel compareModels(ContainerRoot current, ContainerRoot target, String nodeName) {
        DefaultKevoreeAdaptationFactory factory = new DefaultKevoreeAdaptationFactory();
        AdaptationModel adaptationModel = factory.createAdaptationModel();
        AdaptationPrimitiveType removeNodeType = null;
        AdaptationPrimitiveType addNodeType = null;
        for (AdaptationPrimitiveType primitiveType : current.getAdaptationPrimitiveTypes()) {
            if (primitiveType.getName().equals(CloudNode.REMOVE_NODE)) {
                removeNodeType = primitiveType;
            } else if (primitiveType.getName().equals(CloudNode.ADD_NODE)) {
                addNodeType = primitiveType;
            }
        }
        if (removeNodeType == null || addNodeType == null) {
            for (AdaptationPrimitiveType primitiveType : target.getAdaptationPrimitiveTypes()) {
                if (primitiveType.getName().equals(CloudNode.REMOVE_NODE)) {
                    removeNodeType = primitiveType;
                } else if (primitiveType.getName().equals(CloudNode.ADD_NODE)) {
                    addNodeType = primitiveType;
                }
            }
        }
        if (removeNodeType == null) {
            Log.warn("there is no adaptation primitive for {}", CloudNode.REMOVE_NODE);
        }
        if (addNodeType == null) {
            Log.warn("there is no adaptation primitive for {}", CloudNode.ADD_NODE);
        }

        ContainerNode currentNode = current.findNodesByID(skyNode.getName());
        ContainerNode targetNode = target.findNodesByID(skyNode.getName());
        if (currentNode != null) {

            if (targetNode != null) {
                for (ContainerNode subNode : currentNode.getHosts()) {
                    ContainerNode subNode1 = targetNode.findHostsByID(subNode.getName());
                    if (subNode1 == null) {
                        Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.REMOVE_NODE, subNode.getName());
                        AdaptationPrimitive command = factory.createAdaptationPrimitive();
                        command.setPrimitiveType(removeNodeType);
                        command.setRef(subNode);
                        adaptationModel.addAdaptations(command);
                        processStopInstance(subNode, adaptationModel, current, factory);
                    }

                }

            } else {
                // TODO IF HARAKIRI is refactoring, maybe this block must be refactoring too or even removed
                Log.debug("Unable to find the current node on the target model, We remove all the hosted nodes from the current model");
                for (ContainerNode subNode : currentNode.getHosts()) {
                    Log.debug("add a {} adaptation primitive with {} as parameter", JavaSePrimitive.StopInstance, subNode.getName());
                    AdaptationPrimitive command = factory.createAdaptationPrimitive();
                    command.setPrimitiveType(current.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance));
                    command.setRef(subNode);
                    adaptationModel.addAdaptations(command);
                    processStopInstance(subNode, adaptationModel, current, factory);
                }
            }
        }

        if (targetNode != null) {
            if (currentNode != null) {
                for (ContainerNode subNode : targetNode.getHosts()) {
                    ContainerNode subNode1 = currentNode.findHostsByID(subNode.getName());
                    if (subNode1 == null) {
                        Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.ADD_NODE, subNode.getName());
                        AdaptationPrimitive command = factory.createAdaptationPrimitive();
                        command.setPrimitiveType(addNodeType);
                        command.setRef(subNode);
                        adaptationModel.addAdaptations(command);
                        processStartInstance(subNode, adaptationModel, target, factory);
                    } else {
                        if (subNode1.getStarted() != subNode.getStarted()) {
                            if (subNode1.getStarted()) {
                                processStopInstance(subNode, adaptationModel, current, factory);
                            } else {
                                processStartInstance(subNode, adaptationModel, target, factory);
                            }
                        }
                    }
                }
            } else {
                Log.debug("Unable to find the current node on the current model, We add all the hosted nodes from the target model");
                for (ContainerNode subNode : targetNode.getHosts()) {
                    Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.ADD_NODE, subNode.getName());
                    AdaptationPrimitive command = factory.createAdaptationPrimitive();
                    command.setPrimitiveType(addNodeType);
                    command.setRef(subNode);
                    adaptationModel.addAdaptations(command);
                    processStartInstance(subNode, adaptationModel, target, factory);
                }
            }
        }

        Log.debug("Adaptation model contain {} Host node primitives", adaptationModel.getAdaptations().size());

        AdaptationModel superModel = super.compareModels(current, target, nodeName);

        adaptationModel.addAllAdaptations(superModel.getAdaptations());
        Log.debug("Adaptation model contain {} primitives", adaptationModel.getAdaptations().size());
        return adaptationModel;
    }

    private void processStopInstance(Instance actualInstance, AdaptationModel adaptationModel, ContainerRoot actualRoot, KevoreeAdaptationFactory adaptationModelFactory) {
        Log.debug("Process StopInstance on {}", actualInstance.getName());
        AdaptationPrimitive ccmd2 = adaptationModelFactory.createAdaptationPrimitive();
        ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance));
        ccmd2.setRef(actualInstance);
        adaptationModel.addAdaptations(ccmd2);
    }

    private void processStartInstance(Instance updatedInstance, AdaptationModel adaptationModel, ContainerRoot updateRoot, KevoreeAdaptationFactory adaptationModelFactory) {
        Log.debug("Process StartInstance on {}", updatedInstance.getName());
        AdaptationPrimitive ccmd2 = adaptationModelFactory.createAdaptationPrimitive();
        ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance));
        ccmd2.setRef(updatedInstance);
        adaptationModel.addAdaptations(ccmd2);
    }

    @Override
    public AdaptationModel plan(AdaptationModel adaptationModel, String nodeName) {
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
            /*primitives = scheduling.schedule(primitives, false);
            for (AdaptationPrimitive primitive : primitives) {
                List<AdaptationPrimitive> primitiveList = new ArrayList<AdaptationPrimitive>(1);
                primitiveList.add(primitive);
                createNextStep(JavaSePrimitive.instance$.getStopInstance(), primitiveList);
            }*/

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
//            ParallelStep oldStep = getCurrentSteps();
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
            /*primitives = scheduling.schedule(primitives, true);
            for (AdaptationPrimitive primitive : primitives) {
                List<AdaptationPrimitive> primitiveList = new ArrayList<AdaptationPrimitive>(1);
                primitiveList.add(primitive);
                oldStep = getCurrentStep();
                createNextStep(JavaSePrimitive.instance$.getStartInstance(), primitiveList);
            }*/
            // remove empty step at the end
            /*if (getStep() != null && getStep().getAdaptations().isEmpty()) {
                oldStep.setNextStep(null);
            }*/
        } else {
            adaptationModel.setOrderedPrimitiveSet(null);
        }
        clearSteps();
        return adaptationModel;

    }
}