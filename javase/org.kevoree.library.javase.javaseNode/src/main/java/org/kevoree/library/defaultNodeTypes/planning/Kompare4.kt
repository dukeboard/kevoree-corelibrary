package org.kevoree.library.defaultNodeTypes.planning

import org.kevoree.log.Log
import org.kevoreeadaptation.AdaptationModel
import org.kevoree.ContainerRoot
import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory
import org.kevoree.compare.DefaultModelCompare
import org.kevoree.modeling.api.trace.ModelSetTrace
import org.kevoree.modeling.api.trace.ModelRemoveTrace
import org.kevoree.modeling.api.trace.ModelAddTrace
import org.kevoree.Instance
import java.util.ArrayList
import org.kevoree.DeployUnit
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.KMFContainer
import org.kevoreeadaptation.AdaptationPrimitive
import java.util.HashSet
import org.kevoree.modeling.api.trace.TraceSequence

public abstract class Kompare4(val registry: Map<String, Any>) {

    private val modelCompare = DefaultModelCompare()
    private val adaptationModelFactory = DefaultKevoreeAdaptationFactory()

    /* Helper to create command */
    private fun adapt(primitive: JavaPrimitive, elem: Any?, model: ContainerRoot): AdaptationPrimitive {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = model.findAdaptationPrimitiveTypesByID(primitive.name())
        ccmd.ref = elem
        return ccmd
    }

    open public fun compareModels(currentModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {
        val adaptationModel = adaptationModelFactory.createAdaptationModel()
        val dictionaryAlreadyUpdate = HashSet<Instance>()
        val currentNode = currentModel.findNodesByID(nodeName)
        val targetNode = targetModel.findNodesByID(nodeName)
        var traces: TraceSequence? = null
        if (currentNode != null && targetNode != null) {
            traces = modelCompare.diff(currentModel, targetModel)
            for(g in targetNode.groups){
                val previousGroup = currentModel.findByPath(g.path()!!)
                if(previousGroup != null){
                    traces!!.append(modelCompare.diff(previousGroup, g))
                }
            }
        } else {
            if(targetNode != null){
                traces = modelCompare.inter(targetNode, targetNode)
                //append traces from groupes, only attributes are interesting
                for(g in targetNode.groups){
                    traces!!.populate(g.toTraces(true, false))
                }
            }
        }
        if (traces != null) {
            for(trace in traces!!.traces) {
                //println(trace)
                when(trace) {
                    is ModelAddTrace -> {
                        //only consider if Add has as target myself
                        if(trace.srcPath == targetNode!!.path()){
                            when(trace.refName) {
                                "components" -> {
                                    val elemToAdd = targetModel.findByPath(trace.previousPath!!)
                                    adaptationModel.addAdaptations(adapt(JavaPrimitive.AddInstance, elemToAdd, targetModel))
                                }
                                "groups" -> {
                                    val elemToAdd = targetModel.findByPath(trace.previousPath!!)
                                    adaptationModel.addAdaptations(adapt(JavaPrimitive.AddInstance, elemToAdd, targetModel))
                                }
                                else -> {
                                }
                            }
                        } else {
                            //eventually ADD typeDefinition could appear for continuous design
                            //TODO
                        }
                    }
                    is ModelRemoveTrace -> {
                        //Still need test for this part !
                        if(trace.srcPath == targetNode!!.path()){
                            when(trace.refName) {
                                "components" -> {
                                    val elemToAdd = targetModel.findByPath(trace.objPath)
                                    adaptationModel.addAdaptations(adapt(JavaPrimitive.RemoveInstance, elemToAdd, targetModel))
                                }
                                "groups" -> {
                                    val elemToAdd = targetModel.findByPath(trace.objPath)
                                    adaptationModel.addAdaptations(adapt(JavaPrimitive.RemoveInstance, elemToAdd, targetModel))
                                }
                                else -> {
                                }
                            }
                        }
                    }
                    is ModelSetTrace -> {
                        val modelElement = targetModel.findByPath(trace.srcPath)
                        if(modelElement is Instance){
                            when(trace.refName) {
                                "started" -> {
                                    if(trace.srcPath == targetNode!!.path()){
                                        //HaraKiri case
                                    } else {
                                        if (trace.content?.toLowerCase() == "true") {
                                            adaptationModel.addAdaptations(adapt(JavaPrimitive.StartInstance, modelElement, targetModel))
                                        } else {
                                            adaptationModel.addAdaptations(adapt(JavaPrimitive.StopInstance, modelElement, targetModel))
                                        }
                                    }
                                }
                                "typeDefinition" -> {
                                    //TODO continuous design
                                }
                                "value" -> {
                                    if(!dictionaryAlreadyUpdate.contains(modelElement)){
                                        adaptationModel.addAdaptations(adapt(JavaPrimitive.UpdateDictionaryInstance, modelElement, targetModel))
                                        dictionaryAlreadyUpdate.add(modelElement)
                                    }

                                }
                                else -> {

                                }
                            }
                        }
                    }
                    else -> {
                        //noop
                    }
                }
            }
            var foundDeployUnitsToRemove = ArrayList<String>()
            currentNode?.visit(object : ModelVisitor(){
                override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                    if(elem is DeployUnit){
                        foundDeployUnitsToRemove.add(elem.path()!!)
                    }
                }

            }, true, true, true)
            targetNode?.visit(object : ModelVisitor(){
                override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                    if(elem is DeployUnit){
                        foundDeployUnitsToRemove.remove(elem.path()!!)
                    }
                }
            }, true, true, true)
            for(pathDeployUnitToDrop in foundDeployUnitsToRemove){
                adaptationModel.addAdaptations(adapt(JavaPrimitive.RemoveDeployUnit, currentModel.findByPath(pathDeployUnitToDrop), targetModel))
            }
        } else {
            Log.warn("One of the model doesn't contain the local node: (currentNode = {}, tagretNode = {}", currentNode, targetNode)
        }
        return adaptationModel
    }

    /*
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

    protected open fun addInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)
        updateDictionary(instance, currentModel, adaptationModel)
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

    protected open fun updateInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        stopInstance(instance, currentModel, adaptationModel)
        startInstance(instance, currentModel, adaptationModel)
        removeInstance(instance, currentModel, adaptationModel)
        addInstance(instance, currentNode, currentModel, targetModel, adaptationModel)
        updateDictionary(instance, currentModel, adaptationModel)
    }

    */

}