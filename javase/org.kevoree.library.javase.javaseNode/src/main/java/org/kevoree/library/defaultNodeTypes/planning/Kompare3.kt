package org.kevoree.library.defaultNodeTypes.planning

import org.kevoree.ContainerRoot
import org.kevoreeadaptation.AdaptationModel
import org.kevoree.compare.DefaultModelCompare
import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory
import org.kevoree.modeling.api.trace.ModelAddTrace
import org.kevoree.modeling.api.trace.ModelSetTrace
import org.kevoree.modeling.api.trace.ModelRemoveTrace
import org.kevoree.Group
import org.kevoree.Channel
import org.kevoree.ContainerNode
import org.kevoree.ComponentInstance
import org.kevoree.modeling.api.trace.ModelAddAllTrace
import org.kevoree.modeling.api.trace.ModelRemoveAllTrace
import org.kevoree.Instance
import org.kevoree.TypeDefinition
import org.kevoree.MBinding
import java.util.ArrayList
import org.kevoree.DeployUnit
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.KMFContainer
import java.util.HashSet
import org.kevoree.GroupType
import org.kevoree.ComponentType
import org.kevoree.ChannelType
import org.kevoree.NodeType
import org.kevoree.log.Log

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/10/13
 * Time: 17:03
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public abstract class Kompare3(val registry: Map<String, Any>) {

    private val modelCompare = DefaultModelCompare()
    private val adaptationModelFactory = DefaultKevoreeAdaptationFactory()

    private val updatedInstances = ArrayList<String>()
    private val updatedDictionaryInstances = ArrayList<String>()
    private val updatedTypeDefinition = ArrayList<String>()
    private val alreadyManagedDeployUnit = ArrayList<String>()


    // TODO maybe we must compare the complete model and then filter traces instead of only compare node and doing extra stuff to manage deployUnits, TypeDefinitions, Channels and Groups
    // TODO maybe we need to generate Update
    open public fun compareModels(currentModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {
        updatedInstances.clear()
        updatedDictionaryInstances.clear()
        updatedTypeDefinition.clear()
        alreadyManagedDeployUnit.clear()

        val currentNode = currentModel.findNodesByID(nodeName)
        val targetNode = targetModel.findNodesByID(nodeName)

        val adaptationModel = adaptationModelFactory.createAdaptationModel()
        if (currentNode != null && targetNode != null) {
            val traces = modelCompare.diff(currentNode, targetNode)
            traces.traces.forEach {
                trace ->
                //                System.out.println(trace)
                if (trace is ModelAddTrace) {
                    manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelRemoveTrace) {
                    manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                } else if (trace is ModelSetTrace) {
                    manageModelSetTrace(trace, currentModel, targetModel, adaptationModel)
                } else if (trace is ModelAddAllTrace) {
                    //                    System.out.println(trace);
                } else if (trace is ModelRemoveAllTrace) {
                    //                    System.out.println(trace);
                }
            }
            //            var alwaysUsedDeployUnit = HashSet<String>()
            var alwaysUsedTypeDefinitions = HashSet<String>()
            var foundDeployUnitsToRemove = HashSet<String>()
            var foundTypeDefinitionsToRemove = HashSet<String>()
            var foundChannelsToRemove = HashSet<String>()
            var foundGroupsToRemove = HashSet<String>()
            var alwaysUsedChannels = HashSet<String>()
            var alwaysUsedGroups = HashSet<String>()
            currentNode.visit(object : ModelVisitor(){
                override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                    if(elem is DeployUnit){
                        foundDeployUnitsToRemove.add(elem.path()!!)
                    } else if (elem is TypeDefinition) {
                        foundTypeDefinitionsToRemove.add(elem.path()!!)
                    } else if (elem is Channel) {
                        foundChannelsToRemove.add(elem.path()!!)
                    }else if (elem is Group) {
                        foundGroupsToRemove.add(elem.path()!!)
                    }
                }

            }, true, true, true)
            targetNode.visit(object : ModelVisitor(){
                override fun visit(elem: KMFContainer, refNameInParent: String, parent: KMFContainer) {
                    if(elem is DeployUnit){
                        /*if (foundDeployUnitsToRemove.remove(elem.path()!!)) {
                            alwaysUsedDeployUnit.add(elem.path()!!)
                        }*/
                        foundDeployUnitsToRemove.remove(elem.path()!!)
                    } else if (elem is TypeDefinition) {
                        if (foundTypeDefinitionsToRemove.remove(elem.path()!!)) {
                            alwaysUsedTypeDefinitions.add(elem.path()!!)
                        }
                    } else if (elem is Channel) {
                        if (foundChannelsToRemove.remove(elem.path()!!)) {
                            alwaysUsedChannels.add(elem.path()!!)
                        }
                    } else if (elem is Group) {
                        if (foundGroupsToRemove.remove(elem.path()!!)) {
                            alwaysUsedGroups.add(elem.path()!!)
                        }
                    }
                }

            }, true, true, true)
            //            checkUpdateOfDeployUnit(alwaysUsedDeployUnit, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            checkUpdateOfTypeDefinitions(alwaysUsedTypeDefinitions, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            checkUpdateOfChannels(alwaysUsedChannels, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            checkUpdateOfGroups(alwaysUsedGroups, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            dropChannels(foundChannelsToRemove, currentModel, adaptationModel)
            dropGroups(foundGroupsToRemove, currentModel, adaptationModel)
            dropTypeDefinitions(foundTypeDefinitionsToRemove, currentModel, adaptationModel)
            dropDeployUnits(foundDeployUnitsToRemove, currentModel, adaptationModel)

        } else {
            // Because Kevoree core does not provide an updated model during bootstrap but only an empty model as the current one
            // should be better if the Kevoree core always give us a good current model !!
            Log.warn("One of the model doesn't have the local node: (currentNode = {}, targetNode = {})", currentNode, targetNode)
            if (currentNode == null && targetNode != null) {
                Log.info("Current node is null so we are bootstraping...")
                val traces = modelCompare.inter(targetNode, targetNode)
                traces.traces.forEach {
                    trace ->
                    System.out.println(trace)
                    if (trace is ModelAddTrace) {
                        manageModelAddTrace(trace, targetNode, targetModel, targetNode, targetModel, adaptationModel)
                    } else if (trace is ModelRemoveTrace) {
                        manageModelRemoveTrace(trace, targetNode, targetModel, targetNode, targetModel, adaptationModel)
                    } else if (trace is ModelSetTrace) {
                        if (!trace.srcPath.equals(targetNode.path()) && !trace.srcPath.startsWith(targetNode.path() + "/dictionary")) {
                            // we don't care about the local node because we are doing bootstrap and the node is already defined
                            manageModelSetTrace(trace, targetModel, targetModel, adaptationModel)
                        }
                    } else if (trace is ModelAddAllTrace) {
                        //                    System.out.println(trace);
                    } else if (trace is ModelRemoveAllTrace) {
                        //                    System.out.println(trace);
                    }
                }
            }
        }

        return adaptationModel
    }

    protected open fun manageModelAddTrace(trace: ModelAddTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        //        System.out.println(trace);
        if ((trace.typeName != null && (/*trace.typeName.equalsIgnoreCase(javaClass<Group>().getName())
        || trace.typeName.equals(javaClass<Channel>().getName())
        || */trace.typeName.equals(javaClass<ComponentInstance>().getName())
        || trace.typeName.equals(javaClass<ContainerNode>().getName())))) {
            // Add instance
            addInstance(targetModel.findByPath(trace.previousPath!!, javaClass<Instance>())!!, currentNode, currentModel, targetModel, adaptationModel)
        } else if (trace.refName.equalsIgnoreCase("bindings")) {
            // Add Binding
            addBinding(targetModel.findByPath(trace.previousPath!!, javaClass<MBinding>())!!, currentNode, currentModel, targetModel, adaptationModel)
        } else if (trace.srcPath.toLowerCase().startsWith("typedefinitions[") && trace.refName.equalsIgnoreCase("deployUnits")) {
            val deployUnit = targetModel.findByPath(trace.previousPath!!, javaClass<DeployUnit>())!!
            if (deployUnit.targetNodeType!!.path()!!.equals(targetNode.typeDefinition!!.path()!!)) {
                updateTypeDefinition(targetModel.findByPath(trace.srcPath, javaClass<TypeDefinition>())!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        } else if (trace.refName.equalsIgnoreCase("groups")) {
            val instance = targetModel.findByPath(trace.previousPath!!, javaClass<Instance>())!!
            // add the group instance
            addInstance(instance, currentNode, currentModel, targetModel, adaptationModel)
            if (instance.started!!) {
                // start the group instance if needed
                startInstance(instance, currentModel, adaptationModel)
            }
            // update the group instance to set the attributes
            updateDictionary(instance, currentModel, adaptationModel)
        }
    }

    protected open fun manageModelRemoveTrace(trace: ModelRemoveTrace, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        //                    System.out.println(trace);
        val element = currentModel.findByPath(trace.objPath)

        if (element != null && (element is Group || element is Channel || element is ComponentInstance || element is ContainerNode)) {
            removeInstance(element as Instance, currentModel, adaptationModel)
        }  else if (trace.refName.equals("bindings")) {
            removeBinding(element as MBinding, currentModel, adaptationModel)
        } else if (trace.srcPath.toLowerCase().startsWith("typedefinitions[") && trace.refName.equalsIgnoreCase("deployUnits")) {
            val deployUnit = currentModel.findByPath(trace.objPath, javaClass<DeployUnit>())!!
            if (deployUnit.targetNodeType!!.path()!!.equals(targetNode.typeDefinition!!.path()!!)) {
                updateTypeDefinition(currentModel.findByPath(trace.srcPath, javaClass<TypeDefinition>())!!, currentNode, currentModel, targetNode, targetModel, adaptationModel)
            }
        }
        // FIXME how to manage groups
    }

    protected open fun manageModelSetTrace(trace: ModelSetTrace, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        //                    System.out.println(trace);
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
        } else {
            //            System.out.println(trace)
        }
    }

    /*fun checkUpdateOfDeployUnit(alwaysUsedTypeDefinition: HashSet<String>, currentNode : ContainerNode, currentModel: ContainerRoot, targetNode : ContainerNode, targetModel: ContainerRoot, adaptationModel : AdaptationModel) {
        alwaysUsedDeployUnit.forEach {
            deployUnitPath ->
            val currentDeployUnit = currentModel.findByPath(deployUnitPath, javaClass<DeployUnit>())
            val targetDeployUnit = targetModel.findByPath(deployUnitPath, javaClass<DeployUnit>())

            if (currentDeployUnit != null && targetDeployUnit != null) {
                val traces = modelCompare.diff(currentDeployUnit, targetDeployUnit)
                traces.traces.forEach {
                    trace ->
                    //                    System.out.println(trace)
                    // there is never update of deployUnit because at least the hashcode changes and so the id of the deployUnit is always new
                }
            }
        }
    }*/

    protected open fun checkUpdateOfTypeDefinitions(alwaysUsedTypeDefinitions: HashSet<String>, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        alwaysUsedTypeDefinitions.forEach {
            typeDefinitionPath ->
            if (!updatedTypeDefinition.contains(typeDefinitionPath)) {
                val currentTypeDefinition = currentModel.findByPath(typeDefinitionPath, javaClass<TypeDefinition>())
                val targetTypeDefinition = targetModel.findByPath(typeDefinitionPath, javaClass<TypeDefinition>())

                if (currentTypeDefinition != null && targetTypeDefinition != null) {
                    val traces = modelCompare.diff(currentTypeDefinition, targetTypeDefinition)
                    traces.traces.forEach {
                        trace ->
                        //                        System.out.println(trace)
                        if (trace is ModelAddTrace) {
                            manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                        } else if (trace is ModelRemoveTrace) {
                            manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                        } else if (trace is ModelSetTrace) {
                            manageModelSetTrace(trace, currentModel, targetModel, adaptationModel)
                        } else if (trace is ModelAddAllTrace) {
                            //                    System.out.println(trace);
                        } else if (trace is ModelRemoveAllTrace) {
                            //                    System.out.println(trace);
                        }
                    }
                }
            }
        }
    }

    protected open fun checkUpdateOfChannels(alwaysUsedChannels: HashSet<String>, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        alwaysUsedChannels.forEach {
            channelPath ->
            if (!updatedInstances.contains(channelPath)) {
                val currentChannel = currentModel.findByPath(channelPath, javaClass<Channel>())
                val targetChannel = targetModel.findByPath(channelPath, javaClass<Channel>())

                if (currentChannel != null && targetChannel != null) {
                    val traces = modelCompare.diff(currentChannel, targetChannel)
                    traces.traces.forEach {
                        trace ->
                        //                        System.out.println(trace)
                        /*if (trace is ModelAddTrace) {
                            manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                        } else if (trace is ModelRemoveTrace) {
                            manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                        } else */if (trace is ModelSetTrace) {
                        manageModelSetTrace(trace, currentModel, targetModel, adaptationModel)
                    }/* else if (trace is ModelAddAllTrace) {
                            //                    System.out.println(trace);
                        } else if (trace is ModelRemoveAllTrace) {
                            //                    System.out.println(trace);
                        }*/
                    }
                }
            }
        }
    }

    protected open fun checkUpdateOfGroups(alwaysUsedGroups: HashSet<String>, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        alwaysUsedGroups.forEach {
            groupPath ->
            if (!updatedInstances.contains(groupPath)) {
                val currentGroup = currentModel.findByPath(groupPath, javaClass<Group>())
                val targetGroup = targetModel.findByPath(groupPath, javaClass<Group>())

                if (currentGroup != null && targetGroup != null) {
                    val traces = modelCompare.diff(currentGroup, targetGroup)
                    traces.traces.forEach {
                        trace ->
                        //                        System.out.println(trace)
                        /*if (trace is ModelAddTrace) {
                            manageModelAddTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                        } else if (trace is ModelRemoveTrace) {
                            manageModelRemoveTrace(trace, currentNode, currentModel, targetNode, targetModel, adaptationModel)
                        } else */if (trace is ModelSetTrace) {
                        manageModelSetTrace(trace, currentModel, targetModel, adaptationModel)
                    }/* else if (trace is ModelAddAllTrace) {
                            //                    System.out.println(trace);
                        } else if (trace is ModelRemoveAllTrace) {
                            //                    System.out.println(trace);
                        }*/
                    }
                }
            }
        }
    }

    protected open fun dropChannels(foundChannelsToRemove: HashSet<String>, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove channels that are not used anymore in the target model
        foundChannelsToRemove.forEach {
            channelPath ->
            val channel = currentModel.findByPath(channelPath, javaClass<Channel>())
            if (channel != null) {
                removeInstance(channel, currentModel, adaptationModel)
            }
        }
    }

    protected open fun dropGroups(foundGroupsToRemove: HashSet<String>, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove groups that are not used anymore in the target model
        foundGroupsToRemove.forEach {
            groupPath ->
            val group = currentModel.findByPath(groupPath, javaClass<Group>())
            if (group != null) {
                removeInstance(group, currentModel, adaptationModel)
            }
        }
    }

    protected open fun dropTypeDefinitions(foundTypeDefinitionsToRemove: HashSet<String>, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove TypeDefinitions that are not used anymore in the target model
        foundTypeDefinitionsToRemove.forEach {
            typeDefinitionPath ->
            val typeDefinition = currentModel.findByPath(typeDefinitionPath, javaClass<TypeDefinition>())
            if (typeDefinition != null) {
                removeTypeDefinition(typeDefinition, currentModel, adaptationModel)
            }
        }
    }

    protected open fun dropDeployUnits(foundDeployUnitsToRemove: HashSet<String>, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove DeployUnits that are not used anymore in the target model
        foundDeployUnitsToRemove.forEach {
            deployUnitPath ->
            val deployUnit = currentModel.findByPath(deployUnitPath, javaClass<DeployUnit>())
            if (deployUnit != null) {
                removeDeployUnit(deployUnit, currentModel, adaptationModel)
            }
        }
    }

    protected open fun addGroups(foundGroupsToAdd: HashSet<String>, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add groups that are new in the target model
        foundGroupsToAdd.forEach {
            groupPath ->
            val group = targetModel.findByPath(groupPath, javaClass<Group>())
            if (group != null) {
                addInstance(group, currentNode, currentModel, targetModel, adaptationModel)
                if (group.started!!) {
                    startInstance(group, currentModel, adaptationModel)
                }
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
            updatedInstances.add(instance.path()!!)
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

    protected open fun addBinding(element: MBinding, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add binding TODO must to check and maybe to complete according to FragmentBinding
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding)
        ccmd.ref = element
        adaptationModel.addAdaptations(ccmd)

        if (registry.get(element.hub!!.path()) == null) {
            addInstance(element.hub!!, currentNode, currentModel, targetModel, adaptationModel)
        }
    }

    protected open fun removeBinding(element: MBinding, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove binding TODO must to check and maybe to complete according to FragmentBinding
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding)
        ccmd.ref = element
        adaptationModel.addAdaptations(ccmd)

        // channel connected to this binding will be removed if it doesn't appear anymore in the channels connected to one of the components available on the currentNode (see dropChannelAndTypeDefinitionAndDeployUnit)
    }

    protected open fun addInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)

        updateDictionary(instance, currentModel, adaptationModel)

        if (!updatedTypeDefinition.contains(instance.typeDefinition!!.path()!!) && registry.get(targetModel.findTypeDefinitionsByID(instance.typeDefinition!!.path()!!)) == null) {
            // Add TypeDefinition
            addTypeDefinition(instance.typeDefinition!!, currentNode, currentModel, targetModel, adaptationModel)
        }
    }

    protected open fun updateInstance(instance: Instance, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!updatedInstances.contains(instance.path()!!)) {
            stopInstance(instance, currentModel, adaptationModel)
            startInstance(instance, currentModel, adaptationModel)
            removeInstance(instance, currentModel, adaptationModel)
            addInstance(instance, currentNode, currentModel, targetModel, adaptationModel)
            updateDictionary(instance, currentModel, adaptationModel)
        }
    }

    protected open fun removeInstance(instance: Instance, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Remove instance
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance)
        ccmd.ref = instance
        adaptationModel.addAdaptations(ccmd)

        if (instance.started!!) {
            // Stop instance
            stopInstance(instance, currentModel, adaptationModel)
        }

        // typeDefinition connected to this instance will be removed if it doesn't appear anymore in the typeDefinitions used by one of the components available on the currentNode (see dropChannelAndTypeDefinitionAndDeployUnit)
    }

    protected open fun addTypeDefinition(typeDefinition: TypeDefinition, currentNode: ContainerNode, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // Add TypeDefinition
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType)
        ccmd.ref = typeDefinition
        adaptationModel.addAdaptations(ccmd)

        // Add DeployUnit(s)
        val deployUnits = typeDefinition.deployUnits.filter {
            deployUnit ->
            deployUnit.targetNodeType!!.path()!!.equals(currentNode.typeDefinition!!.path()!!) && registry.get(deployUnit.path()!!) == null
        }
        deployUnits.forEach {
            deployUnit ->
            addDeployUnit(deployUnit, currentModel, targetModel, adaptationModel)
        }
    }

    protected open fun updateTypeDefinition(typeDefinition: TypeDefinition, currentNode: ContainerNode, currentModel: ContainerRoot, targetNode: ContainerNode, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!updatedTypeDefinition.contains(typeDefinition.path()!!)) {
            val typeDefinitionToRemove = currentModel.findByPath(typeDefinition.path()!!, javaClass<TypeDefinition>())!!
            val typeDefinitionToAdd = targetModel.findByPath(typeDefinition.path()!!, javaClass<TypeDefinition>())!!
            removeTypeDefinition(typeDefinitionToRemove, currentModel, adaptationModel)
            addTypeDefinition(typeDefinitionToAdd, currentNode, currentModel, targetModel, adaptationModel)
            updatedTypeDefinition.add(typeDefinition.path()!!)

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

    protected open fun removeTypeDefinition(typeDefinition: TypeDefinition, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        // remove TypeDefinition
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType)
        ccmd.ref = typeDefinition
        adaptationModel.addAdaptations(ccmd)

        // deployUnits connected to this typeDefinition will be removed if they don't appear anymore in the deployUnits used by one of the typeDefinition available on the currentNode (see dropTypeDefinitions)
    }

    protected open fun addDeployUnit(deployUnit: DeployUnit, currentModel: ContainerRoot, targetModel: ContainerRoot, adaptationModel: AdaptationModel) {
        if (!alreadyManagedDeployUnit.contains(deployUnit.path()!!)) {
            val ccmd = adaptationModelFactory.createAdaptationPrimitive()
            ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddDeployUnit)
            ccmd.ref = deployUnit
            adaptationModel.addAdaptations(ccmd)
            alreadyManagedDeployUnit.add(deployUnit.path()!!)

            deployUnit.requiredLibs.forEach {
                requiredLib ->
                if (registry.get(requiredLib) == null) {
                    addDeployUnit(requiredLib, currentModel, targetModel, adaptationModel)
                }
            }
        }
    }

    protected open fun removeDeployUnit(deployUnit: DeployUnit, currentModel: ContainerRoot, adaptationModel: AdaptationModel) {
        val ccmd = adaptationModelFactory.createAdaptationPrimitive()
        ccmd.primitiveType = currentModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveDeployUnit)
        ccmd.ref = deployUnit
        adaptationModel.addAdaptations(ccmd)

        // deployUnits connected to this deployUnit will be removed if they don't appear anymore in the deployUnits used by one of the typeDefinition available on the currentNode (see dropDeployUnits)
    }
}
