package org.kevoree.library.defaultNodeTypes.planning

import org.kevoree.log.Log
import org.kevoreeadaptation.AdaptationModel
import org.kevoree.ContainerRoot
import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory
import org.kevoree.compare.DefaultModelCompare
import org.kevoree.modeling.api.trace.ModelAddAllTrace
import org.kevoree.modeling.api.trace.ModelSetTrace
import org.kevoree.modeling.api.trace.ModelRemoveTrace
import org.kevoree.modeling.api.trace.ModelAddTrace
import org.kevoree.modeling.api.trace.ModelRemoveAllTrace
import org.kevoree.ContainerNode
import org.kevoree.Channel
import org.kevoree.Group
import org.kevoree.Instance
import java.util.ArrayList
import org.kevoree.ComponentInstance
import org.kevoree.DeployUnit
import org.kevoree.TypeDefinition
import org.kevoree.MBinding
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.GroupType
import org.kevoree.NodeType

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/11/13
 * Time: 12:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class Kompare3(val registry: Map<String, Any>) {

    private val modelCompare = DefaultModelCompare()
    private val adaptationModelFactory = DefaultKevoreeAdaptationFactory()

    private val updatedDictionaryInstances = ArrayList<String>()
    private val alreadyInstalledElement = ArrayList<String>()
    private val alreadyRemovedElement = ArrayList<String>()

    open public fun compareModels(currentModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {
        updatedDictionaryInstances.clear()
        alreadyInstalledElement.clear()
        alreadyRemovedElement.clear()

        val currentNode = currentModel.findNodesByID(nodeName)
        val targetNode = targetModel.findNodesByID(nodeName)

        val adaptationModel = adaptationModelFactory.createAdaptationModel()
        if (currentNode != null && targetNode != null) {
            val traces = modelCompare.diff(currentModel, targetModel)
            traces.traces.forEach {
                trace ->
                //                System.out.println(trace)
                if (trace is ModelAddTrace) {
                    manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelRemoveTrace) {
                    manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelSetTrace) {
                    manageModelSetTrace(trace, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelAddAllTrace) {
                    //                    System.out.println(trace)
                } else if (trace is ModelRemoveAllTrace) {
                    //                    System.out.println(trace)
                }
            }

            var foundDeployUnitsToRemove = ArrayList<String>()
            var foundTypeDefinitionsToRemove = ArrayList<String>()
            currentNode.visit(object : ModelVisitor(){
                override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                    if(elem is DeployUnit){
                        foundDeployUnitsToRemove.add(elem.path()!!)
                    } else if (elem is TypeDefinition) {
                        foundTypeDefinitionsToRemove.add(elem.path()!!)
                    }
                }

            }, true, true, true)
            targetNode.visit(object : ModelVisitor(){
                override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                    if(elem is DeployUnit){
                        foundDeployUnitsToRemove.remove(elem.path()!!)
                    } else if (elem is TypeDefinition) {
                        foundTypeDefinitionsToRemove.remove(elem.path()!!)
                    }
                }
            }, true, true, true)
            dropTypeDefinitions(foundTypeDefinitionsToRemove, currentModel, adaptationModel)
            dropDeployUnits(foundDeployUnitsToRemove, currentModel, adaptationModel)
        } else {
            Log.warn("One of the model doesn't contain the local node: (currentNode = {}, tagretNode = {}", currentNode, targetNode)
        }
        return adaptationModel
    }

    protected open fun manageModelAddTrace(trace: ModelAddTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (trace.srcPath.equals(currentNode.path()!!)) {
            manageModelAddTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        } else if (trace.srcPath.equals("")){
            val element = targetModel.findByPath(trace.previousPath!!)
            if (element is Channel) {
                if (element.bindings.find { binding -> binding.port!!.eContainer()!!.eContainer() == targetNode } != null) {
                    manageModelAddTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                }
            } else if (element is MBinding) {
                if (element.port!!.eContainer()!!.eContainer() == targetNode) {
                    manageModelAddTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                }
            }
        } else {
            val srcElement = targetModel.findByPath(trace.srcPath)
            val element = targetModel.findByPath(trace.previousPath!!)
            if (element is ContainerNode && element == targetNode) {
                manageModelAddTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            } else if (srcElement is TypeDefinition) {
                updateTypeDefinition(srcElement, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        }
    }

    protected open fun manageModelAddTraceForNode(trace: ModelAddTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (trace.typeName != null) {
            if (/*trace.typeName.equalsIgnoreCase(javaClass<Group>().getName())
            || */trace.typeName.equals(javaClass<Channel>().getName())
            || trace.typeName.equals(javaClass<ComponentInstance>().getName())
            || trace.typeName.equals(javaClass<ContainerNode>().getName())) {
                // Add instance
                addInstance(targetModel.findByPath(trace.previousPath!!, javaClass<Instance>())!!, currentNode, currentModel, targetModel, adaptationModel)
            } else if (trace.typeName.equalsIgnoreCase(javaClass<MBinding>().getName())) {
                // Add MBinding
                addBinding(targetModel.findByPath(trace.previousPath!!, javaClass<MBinding>())!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        } else {
            val srcElement = targetModel.findByPath(trace.srcPath)
            val srcGroup = targetModel.findByPath(trace.srcPath)
            val subNode = targetModel.findByPath(trace.previousPath!!)
            if (srcGroup != null && srcGroup is Group && subNode != null && subNode is ContainerNode && subNode == targetNode) {
                // Add Group
                addInstance(srcGroup, currentNode, currentModel, targetModel, adaptationModel)
            } else if (srcElement is TypeDefinition) {
                updateTypeDefinition(srcElement, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        }
    }

    protected open fun manageModelRemoveTrace(trace: ModelRemoveTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (trace.srcPath.equals(currentNode.path()!!)) {
            manageModelRemoveTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
        } else if (trace.srcPath.equals("")){
            val element = targetModel.findByPath(trace.objPath)
            if (element is Channel) {
                if (element.bindings.find { binding -> binding.port!!.eContainer()!!.eContainer() == targetNode } != null) {
                    manageModelRemoveTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                }
            } else if (element is MBinding) {
                if (element.port!!.eContainer()!!.eContainer() == targetNode) {
                    manageModelRemoveTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                }
            }
        } else {
            val element = targetModel.findByPath(trace.objPath)
            if (element is ContainerNode && element == targetNode) {
                manageModelRemoveTraceForNode(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        }
    }

    protected open fun manageModelRemoveTraceForNode(trace: ModelRemoveTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val element = currentModel.findByPath(trace.objPath)
        if (/*element is Group || */element is Channel || element is ComponentInstance || element is ContainerNode) {
            removeInstance(element as Instance, currentModel, adaptationModel)
        }  else if (element is MBinding) {
            if ((element.port!!.eContainer()!!.eContainer() as ContainerNode) == currentNode) {
                // Remove Binding
                removeBinding(element, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }

        }else {
            val srcGroup = targetModel.findByPath(trace.srcPath)
            val subNode = targetModel.findByPath(trace.objPath!!)
            if (srcGroup != null && srcGroup is Group && subNode != null && subNode is ContainerNode && subNode == targetNode) {
                // remove Group
                removeInstance(srcGroup, currentModel, adaptationModel)
            }
        }
    }

    protected open fun manageModelSetTrace(trace: ModelSetTrace, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        //        val currentElement = currentModel.findByPath(trace.srcPath)
        val targetElement = targetModel.findByPath(trace.srcPath)

        if (/*currentElement is Channel || */targetElement is Channel) {
            if ((targetElement as Channel).bindings.find { binding -> binding.port!!.eContainer()!!.eContainer() == targetNode } != null) {
                manageModelSetTraceForNode(trace, currentModel, targetModel, adaptationModel)
            }
        } else if (/*currentElement is Group || */targetElement is Group) {
            if ((targetElement as Group).subNodes.contains(targetNode)) {
                manageModelSetTraceForNode(trace, currentModel, targetModel, adaptationModel)
            }
        } else if (/*currentElement is ComponentInstance || */targetElement is ComponentInstance) {
            if ((targetElement as ComponentInstance).eContainer() == targetNode) {
                manageModelSetTraceForNode(trace, currentModel, targetModel, adaptationModel)
            }
        } else if (/*currentElement is ContainerNode || */targetElement is ContainerNode) {
            if ((targetElement as ContainerNode).host == targetNode) {
                manageModelSetTraceForNode(trace, currentModel, targetModel, adaptationModel)
            }
        }
    }

    protected open fun manageModelSetTraceForNode(trace: ModelSetTrace, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (trace.refName.equals("started")) {
            if (trace.content.equals("true")) {
                startInstance(targetModel.findByPath(trace.srcPath, javaClass<Instance>())!!, currentModel, adaptationModel)
            } else {
                var instance: Instance?
                if (currentModel.findByPath(trace.srcPath) != null) {
                    instance = currentModel.findByPath(trace.srcPath, javaClass<Instance>())
                } else {
                    instance = targetModel.findByPath(trace.srcPath, javaClass<Instance>())
                }
                stopInstance(instance!!, currentModel, adaptationModel)
            }
        } else if (trace.refName.equals("value")) {
            updateDictionary(targetModel.findByPath(trace.srcPath)!!.eContainer()!!.eContainer() as Instance, currentModel, adaptationModel)
        }
    }

    protected open fun dropTypeDefinitions(foundTypeDefinitionsToRemove: ArrayList<String>, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove TypeDefinitions that are not used anymore in the target model
        foundTypeDefinitionsToRemove.forEach {
            typeDefinitionPath ->
            removeTypeDefinition(currentModel.findByPath(typeDefinitionPath, javaClass<TypeDefinition>())!!, currentModel, adaptationModel)
        }
    }

    protected open fun dropDeployUnits(foundDeployUnitsToRemove: ArrayList<String>, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove DeployUnits that are not used anymore in the target model
        foundDeployUnitsToRemove.forEach {
            deployUnitPath ->
            removeDeployUnit(currentModel.findByPath(deployUnitPath, javaClass<DeployUnit>())!!, currentModel, adaptationModel)
        }
    }

    protected open fun updateDictionary(instance: Instance, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!updatedDictionaryInstances.contains(instance.path()!!)) {
            // Update dictionary
            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
            ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance)
            ccmd.ref = instance
            adaptationModel.addAdaptations(ccmd)
            updatedDictionaryInstances.add(instance.path()!!)
        }

    }

    protected open fun startInstance(instance: Instance, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Start instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)
    }

    protected open fun stopInstance(instance: Instance, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Stop instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)
    }

    protected open fun addInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)

        updateDictionary(instance, currentModel, adaptationModel)

        if (registry.get(targetModel.findTypeDefinitionsByID(instance.typeDefinition!!.path()!!)) == null) {
            // Add TypeDefinition
            addTypeDefinition(instance.typeDefinition!!, currentNode, currentModel, targetModel, adaptationModel)
        }
    }

    protected open fun addTypeDefinition(typeDefinition: TypeDefinition, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!alreadyInstalledElement.contains(typeDefinition.path()!!)) {
            // Add TypeDefinition
            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
            ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType)
            ccmd.ref = typeDefinition
            adaptationModel.addAdaptations(ccmd)
            alreadyInstalledElement.add(typeDefinition.path()!!)

            // Add DeployUnit(s)
            val deployUnits = typeDefinition.deployUnits.filter {
                deployUnit ->
                inheritedFrom(deployUnit.targetNodeType!!, currentNode.typeDefinition!!) && registry.get(deployUnit.path()!!) == null
            }
            deployUnits.forEach {
                deployUnit ->
                addDeployUnit(deployUnit, currentModel, targetModel, adaptationModel)
            }
        }
    }

    private fun inheritedFrom(targetNodeType: TypeDefinition, curentTargetNodeType: TypeDefinition): Boolean {
        return targetNodeType.path()!!.equals(curentTargetNodeType.path()!!) || curentTargetNodeType.superTypes.find { superType -> inheritedFrom(targetNodeType, superType) } != null
    }

    protected open fun addDeployUnit(deployUnit: DeployUnit, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!alreadyInstalledElement.contains(deployUnit.path()!!)) {
            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
            ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddDeployUnit)
            ccmd.ref = deployUnit
            adaptationModel.addAdaptations(ccmd)
            alreadyInstalledElement.add(deployUnit.path()!!)

            deployUnit.requiredLibs.forEach {
                requiredLib ->
                if (registry.get(requiredLib) == null) {
                    addDeployUnit(requiredLib, currentModel, targetModel, adaptationModel)
                }
            }
        }
    }

    protected open fun addBinding(binding: MBinding, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding)
        ccmd.ref = binding
        adaptationModel.addAdaptations(ccmd)

        val remoteBindings = binding.hub!!.bindings.filter { remoteBinding -> remoteBinding.port!!.eContainer()!!.eContainer() != targetNode }
        if (remoteBindings.size > 0) {
            remoteBindings.forEach { remoteBinding -> addFragmentBinding(binding.hub!!, (remoteBinding.port!!.eContainer()!!.eContainer()!! as ContainerNode).name!!, currentModel, adaptationModel) }
        }
    }

    protected open fun addFragmentBinding(channel: Channel, remoteNodeName: String, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!alreadyInstalledElement.contains(channel.path()!! + "@" + remoteNodeName)) {
            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
            ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding)
            ccmd.ref = channel
            ccmd.targetNodeName = remoteNodeName
            adaptationModel.addAdaptations(ccmd)
            alreadyInstalledElement.add(channel.path()!! + "@" + remoteNodeName)
        }
    }

    protected open fun removeInstance(instance: Instance, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)

        // stop instance if needed
        if (instance.started!!) {
            stopInstance(instance, currentModel, adaptationModel)
        }

    }

    protected open fun removeBinding(binding: MBinding, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding)
        ccmd.ref = binding
        adaptationModel.addAdaptations(ccmd)

        val currentRemoteBindings = binding.hub!!.bindings.filter { remoteBinding -> remoteBinding.port!!.eContainer()!!.eContainer() != currentNode }
        val targetChannel = targetModel.findByPath(binding.hub!!.path()!!, javaClass<Channel>())
        if (targetChannel == null) {
            currentRemoteBindings.forEach { remoteBinding -> removeFragmentBinding(binding.hub!!, (remoteBinding.port!!.eContainer()!!.eContainer()!! as ContainerNode).name!!, currentModel, adaptationModel) }
            removeInstance(binding.hub!!, currentModel, adaptationModel)
        } else {
            val localBinding = targetChannel.bindings.find { localBinding -> localBinding.port!!.eContainer()!!.eContainer() == targetNode }
            if (localBinding == null) {
                currentRemoteBindings.forEach { remoteBinding -> removeFragmentBinding(binding.hub!!, (remoteBinding.port!!.eContainer()!!.eContainer()!! as ContainerNode).name!!, currentModel, adaptationModel) }
                removeInstance(binding.hub!!, currentModel, adaptationModel)
            }
        }
    }

    protected open fun removeFragmentBinding(channel: Channel, remoteNodeName: String, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding)
        ccmd.ref = channel
        ccmd.targetNodeName = remoteNodeName
        adaptationModel.addAdaptations(ccmd)
    }

    protected open fun removeTypeDefinition(typeDefinition: TypeDefinition, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // remove TypeDefinition
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType)
        ccmd.ref = typeDefinition
        adaptationModel.addAdaptations(ccmd)

        // deployUnits connected to this typeDefinition will be removed if they don't appear anymore in the deployUnits used by one of the typeDefinition available on the currentNode (see dropTypeDefinitions)
    }

    protected open fun removeDeployUnit(deployUnit: DeployUnit, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveDeployUnit)
        ccmd.ref = deployUnit
        adaptationModel.addAdaptations(ccmd)

        // deployUnits connected to this deployUnit will be removed if they don't appear anymore in the deployUnits used by one of the typeDefinition available on the currentNode (see dropDeployUnits)
    }

    protected open fun updateTypeDefinition(typeDefinition: TypeDefinition, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!alreadyInstalledElement.contains(typeDefinition.path()!!) || !alreadyRemovedElement.contains(typeDefinition.path()!!)) {
            val typeDefinitionToRemove = currentModel.findByPath(typeDefinition.path()!!, javaClass<TypeDefinition>())!!
            val typeDefinitionToAdd = targetModel.findByPath(typeDefinition.path()!!, javaClass<TypeDefinition>())!!
            if (!alreadyInstalledElement.contains(typeDefinition.path()!!)) {
                addTypeDefinition(typeDefinitionToAdd, currentNode, currentModel, targetModel, adaptationModel)
                alreadyInstalledElement.add(typeDefinition.path()!!)
            }
            if (!alreadyRemovedElement.contains(typeDefinition.path()!!)) {
                removeTypeDefinition(typeDefinitionToRemove, currentModel, adaptationModel)
                alreadyRemovedElement.add(typeDefinition.path()!!)
            }

            // check all instances to update them
            if (typeDefinition is ChannelType) {
                targetNode.visit(object : ModelVisitor() {
                    override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                        if (elem is Channel) {
                            updateInstance(elem, currentNode, currentModel, targetModel, adaptationModel)
                        }
                    }
                }, true, false, true)
            } else if (typeDefinition is ComponentType) {
                targetNode.visit(object : ModelVisitor() {
                    override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                        if (elem is ComponentInstance) {
                            updateInstance(elem, currentNode, currentModel, targetModel, adaptationModel)
                        }
                    }
                }, false, true, false)
            } else if (typeDefinition is GroupType) {
                targetNode.visit(object : ModelVisitor() {
                    override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                        if (elem is Group) {
                            updateInstance(elem, currentNode, currentModel, targetModel, adaptationModel)
                        }
                    }
                }, true, false, true)
            } else if (typeDefinition is NodeType) {
                targetNode.visit(object : ModelVisitor() {
                    override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                        if (elem is ContainerNode) {
                            updateInstance(elem, currentNode, currentModel, targetModel, adaptationModel)
                        }
                    }
                }, false, false, true)
            }
        }
    }

    protected open fun updateInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        stopInstance(instance, currentModel, adaptationModel)
        startInstance(instance, currentModel, adaptationModel)
        removeInstance(instance, currentModel, adaptationModel)
        addInstance(instance, currentNode, currentModel, targetModel, adaptationModel)
        updateDictionary(instance, currentModel, adaptationModel)
    }
}