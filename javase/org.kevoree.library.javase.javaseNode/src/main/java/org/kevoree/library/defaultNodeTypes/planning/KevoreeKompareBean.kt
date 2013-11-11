package org.kevoree.library.defaultNodeTypes.planning

import org.kevoree.*
import org.kevoreeadaptation.*

open class KevoreeKompareBean(registry: Map<String, Any>) : Kompare3(registry), KevoreeScheduler {
    override var previousStep: ParallelStep? = null
    override var currentSteps: ParallelStep? = null

    override var adaptationModelFactory: KevoreeAdaptationFactory = org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()

    fun plan(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel {

        var adaptationModel = compareModels(actualModel, targetModel, nodeName)

//        AdaptationModelPrinter().printAdaptations(adaptationModel)

        //logger.debug("after Hara Kiri detect")
        val afterPlan = schedule(adaptationModel, nodeName)
        return afterPlan
    }

    private fun transformPrimitives(adaptationModel: AdaptationModel, actualModel: ContainerRoot): AdaptationModel {
        //TRANSFORME UPDATE
        for(adaptation in adaptationModel.adaptations){
            when(adaptation.primitiveType!!.name) {
                JavaSePrimitive.UpdateType -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveType)
                    rcmd.ref = adaptation.ref!!
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)
                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddType)
                    acmd.ref = adaptation.ref!!
                    adaptationModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveBinding)
                    rcmd.ref = adaptation.ref!!
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddBinding)
                    acmd.ref = adaptation.ref!!
                    adaptationModel.addAdaptations(acmd)
                }
                JavaSePrimitive.UpdateFragmentBinding -> {
                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveFragmentBinding)
                    rcmd.ref = adaptation.ref!!
                    rcmd.targetNodeName = adaptation.targetNodeName
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddFragmentBinding)
                    acmd.ref = adaptation.ref!!
                    acmd.targetNodeName = adaptation.targetNodeName
                    adaptationModel.addAdaptations(acmd)
                }

                JavaSePrimitive.UpdateInstance -> {
                    val stopcmd = adaptationModelFactory.createAdaptationPrimitive()
                    stopcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StopInstance)
                    stopcmd.ref = (adaptation.ref as Array<Any>).get(0)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(stopcmd)

                    val rcmd = adaptationModelFactory.createAdaptationPrimitive()
                    rcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.RemoveInstance)
                    rcmd.ref = (adaptation.ref as Array<Any>).get(0)
                    adaptationModel.removeAdaptations(adaptation)
                    adaptationModel.addAdaptations(rcmd)

                    val acmd = adaptationModelFactory.createAdaptationPrimitive()
                    acmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.AddInstance)
                    acmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(acmd)

                    val uDiccmd = adaptationModelFactory.createAdaptationPrimitive()
                    uDiccmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.UpdateDictionaryInstance)
                    uDiccmd.ref = (adaptation.ref as Array<Any>).get(1)
                    adaptationModel.addAdaptations(uDiccmd)

                    val startcmd = adaptationModelFactory.createAdaptationPrimitive()
                    startcmd.primitiveType = actualModel.findAdaptationPrimitiveTypesByID(JavaSePrimitive.StartInstance)
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
