package org.kevoree.library.defaultNodeTypes.planning

import org.kevoree.*
import org.kevoreeadaptation.*
import org.kevoree.modeling.api.KMFContainer

open class KevoreeKompareBean(registry: Map<String, Any>) : Kompare4(registry), KevoreeScheduler {
    override var previousStep: ParallelStep? = null
    override var currentSteps: ParallelStep? = null

    override var adaptationModelFactory: KevoreeAdaptationFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()

    fun plan(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {
        var adaptationModel = compareModels(actualModel, targetModel, nodeName)
        val afterPlan = schedule(adaptationModel, nodeName)
        return afterPlan
    }

    private fun transformPrimitives(adaptationModel: AdaptationModel, actualModel: ContainerRoot): AdaptationModel {
        //TRANSFORME UPDATE
        for(adaptation in adaptationModel.adaptations){
            when(adaptation.primitiveType!!.name) {
                JavaPrimitive.UpdateBinding.name() -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.RemoveBinding.name())
                    rcmd.ref = adaptation.ref!!
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.AddBinding.name())
                    acmd.ref = adaptation.ref!!
                    adaptationModel.addAdaptations(acmd)
                }
                JavaPrimitive.UpdateFragmentBinding.name() -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.RemoveFragmentBinding.name())
                    rcmd.ref = adaptation.ref!!
                    rcmd.targetNodeName = adaptation.targetNodeName
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.AddFragmentBinding.name())
                    acmd.ref = adaptation.ref!!
                    acmd.targetNodeName = adaptation.targetNodeName
                    adaptationModel.addAdaptations(acmd)
                }

                JavaPrimitive.UpdateInstance.name() -> {
                    val stopcmd = adaptationModelFactory.createAdaptationPrimitive()
                    stopcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.StopInstance.name())
                    stopcmd.ref = (adaptation.ref as Array<Any>).get(0)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(stopcmd)

                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.RemoveInstance.name())
                    rcmd.ref = (adaptation.ref as Array<Any>).get(0)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.AddInstance.name())
                    acmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(acmd)

                    val uDiccmd = adaptationModelFactory.createAdaptationPrimitive()
                    uDiccmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.UpdateDictionaryInstance.name())
                    uDiccmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(uDiccmd)

                    val startcmd = adaptationModelFactory.createAdaptationPrimitive()
                    startcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaPrimitive.StartInstance.name())
                    startcmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(startcmd)
                }
                else -> {
                }
            }
        }
        return adaptationModel;
    }

}
