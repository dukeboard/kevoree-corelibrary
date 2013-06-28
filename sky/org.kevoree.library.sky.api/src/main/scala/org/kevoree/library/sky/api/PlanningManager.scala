package org.kevoree.library.sky.api

import org.kevoree.library.sky.api.nodeType.CloudNode
import org.kevoreeadaptation.{KevoreeAdaptationFactory, AdaptationPrimitive, ParallelStep, AdaptationModel}
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree._
import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory
import scala.collection.JavaConversions._
import org.kevoree.kompare.{JavaSePrimitive, KevoreeKompareBean}
import org.kevoree.kompare.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoree.framework.AbstractNodeType
import scala.collection.mutable


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/12/11
 * Time: 09:19
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class PlanningManager(skyNode: AbstractNodeType) extends KevoreeKompareBean {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def compareModels(current: ContainerRoot, target: ContainerRoot, nodeName: String): AdaptationModel = {
    val factory = new DefaultKevoreeAdaptationFactory
    val adaptationModel: AdaptationModel = factory.createAdaptationModel
    //    var step: ParallelStep = factory.createParallelStep
    //    adaptationModel.setOrderedPrimitiveSet(step)
    //    if (skyNode.isHost) {
    var removeNodeType: AdaptationPrimitiveType = null
    var addNodeType: AdaptationPrimitiveType = null
    current.getAdaptationPrimitiveTypes.foreach {
      primitiveType =>
        if (primitiveType.getName == CloudNode.REMOVE_NODE) {
          removeNodeType = primitiveType
        } else if (primitiveType.getName == CloudNode.ADD_NODE) {
          addNodeType = primitiveType
        }
    }
    if (removeNodeType == null || addNodeType == null) {
      target.getAdaptationPrimitiveTypes.foreach {
        primitiveType =>
          if (primitiveType.getName == CloudNode.REMOVE_NODE) {
            removeNodeType = primitiveType
          }
          else if (primitiveType.getName == CloudNode.ADD_NODE) {
            addNodeType = primitiveType
          }
      }
    }
    if (removeNodeType == null) {
      logger.warn("there is no adaptation primitive for {}", CloudNode.REMOVE_NODE)
    }
    if (addNodeType == null) {
      logger.warn("there is no adaptation primitive for {}", CloudNode.ADD_NODE)
    }

    current.findByPath("nodes[" + skyNode.getNodeName + "]", classOf[ContainerNode]) match {
      case node: ContainerNode => {
        target.findByPath("nodes[" + skyNode.getNodeName + "]", classOf[ContainerNode]) match {
          case node1: ContainerNode => {
            node.getHosts.foreach {
              subNode =>
                node1.findByPath("hosts[" + subNode.getName + "]", classOf[ContainerNode]) match {
                  case null => {
                    logger.debug("add a {} adaptation primitive with {} as parameter", Array[String](CloudNode.REMOVE_NODE, subNode.getName))
                    val command: AdaptationPrimitive = factory.createAdaptationPrimitive
                    command.setPrimitiveType(removeNodeType)
                    command.setRef(subNode)
                    //                      val subStep: ParallelStep = factory.createParallelStep
                    //                      subStep.addAdaptations(command)
                    adaptationModel.addAdaptations(command)
                    //                      step.setNextStep(subStep)
                    //                      step = subStep
                    processStopInstance(subNode, adaptationModel, current, factory)
                  }
                  case subNode1: ContainerNode => {
                    if (subNode1.getStarted() != subNode.getStarted()) {
                      if (subNode.getStarted()) {
                        processStopInstance(subNode, adaptationModel, current, factory)
                      } else {
                        processStartInstance(subNode, adaptationModel, current, factory);
                      }
                    }
                  }
                }
            }
          }
          case null => {
            logger.debug("Unable to find the current node on the target model, We remove all the hosted nodes from the current model")
            node.getHosts.foreach {
              subNode =>
                logger.debug("add a {} adaptation primitive with {} as parameter", Array[String](CloudNode.REMOVE_NODE, subNode.getName))
                val command: AdaptationPrimitive = factory.createAdaptationPrimitive
                command.setPrimitiveType(removeNodeType)
                command.setRef(subNode)
                //                  val subStep: ParallelStep = factory.createParallelStep
                //                  subStep.addAdaptations(command)
                adaptationModel.addAdaptations(command)
                //                  step.setNextStep(subStep)
                //                  step = subStep
                processStopInstance(subNode, adaptationModel, current, factory)
            }
          }
        }
      }
      case null =>
    }

    target.findByPath("nodes[" + skyNode.getNodeName + "]", classOf[ContainerNode]) match {
      case node: ContainerNode => {
        current.findByPath("nodes[" + skyNode.getNodeName + "]", classOf[ContainerNode]) match {
          case node1: ContainerNode => {
            node.getHosts.foreach {
              subNode =>
                node1.findByPath("hosts[" + subNode.getName + "]", classOf[ContainerNode]) match {
                  case null => {
                    logger.debug("add a {} adaptation primitive with {} as parameter", Array[String](CloudNode.ADD_NODE, subNode.getName))
                    val command: AdaptationPrimitive = factory.createAdaptationPrimitive
                    command.setPrimitiveType(addNodeType)
                    command.setRef(subNode)
                    //                      val subStep: ParallelStep = factory.createParallelStep
                    //                      subStep.addAdaptations(command)
                    adaptationModel.addAdaptations(command)
                    //                      step.setNextStep(subStep)
                    //                      step = subStep
                    processStartInstance(subNode, adaptationModel, current, factory)
                  }
                  case subNode1: ContainerNode => {
                    if (subNode1.getStarted() != subNode.getStarted()) {
                      if (subNode.getStarted()) {
                        processStopInstance(subNode, adaptationModel, current, factory)
                      } else {
                        processStartInstance(subNode, adaptationModel, current, factory)
                      }
                    }
                  }
                }
            }
          }
          case null => {
            logger.debug("Unable to find the current node on the current model, We add all the hosted nodes from the target model")
            node.getHosts.foreach {
              subNode =>
                logger.debug("add a {} adaptation primitive with {} as parameter", Array[String](CloudNode.ADD_NODE, subNode.getName))
                val command: AdaptationPrimitive = factory.createAdaptationPrimitive
                command.setPrimitiveType(addNodeType)
                command.setRef(subNode)
                //                  val subStep: ParallelStep = factory.createParallelStep
                //                  subStep.addAdaptations(command)
                adaptationModel.addAdaptations(command)
                //                  step.setNextStep(subStep)
                //                  step = subStep
                processStartInstance(subNode, adaptationModel, current, factory)
            }
          }
        }
      }
      case null =>
    }
    //    }
    logger.debug("Adaptation model contain {} Host node primitives", adaptationModel.getAdaptations.size)
    //    val superModel: AdaptationModel = skyNode.superKompare(current, target)

    val superModel = super.compareModels(current, target, nodeName)

    /*if (!skyNode.isContainer && isContaining(superModel.getOrderedPrimitiveSet)) {
      throw new Exception("This node is not a container (see \"role\" attribute)")
    }*/
    adaptationModel.addAllAdaptations(superModel.getAdaptations)
    //    step.setNextStep(superModel.getOrderedPrimitiveSet)
    logger.debug("Adaptation model contain {} primitives", adaptationModel.getAdaptations.size)
    adaptationModel
  }


  private def processStopInstance(actualInstance: Instance, adaptationModel: AdaptationModel, actualRoot: ContainerRoot, adaptationModelFactory: KevoreeAdaptationFactory) {
    val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
    ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.instance$.getStopInstance()))
    ccmd2.setRef(actualInstance)
    adaptationModel.addAdaptations(ccmd2)
  }

  private def processStartInstance(updatedInstance: Instance, adaptationModel: AdaptationModel, updateRoot: ContainerRoot, adaptationModelFactory: KevoreeAdaptationFactory) {
    val ccmd2 = adaptationModelFactory.createAdaptationPrimitive()
    ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.instance$.getStartInstance()))
    ccmd2.setRef(updatedInstance)
    adaptationModel.addAdaptations(ccmd2)
  }


  override def plan(adaptationModel: AdaptationModel, p2: String): AdaptationModel = {
    if (!adaptationModel.getAdaptations.isEmpty) {

      val adaptationModelFactory = new org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory()
      val scheduling = new SchedulingWithTopologicalOrderAlgo()
      nextStep()
      adaptationModel.setOrderedPrimitiveSet(getCurrentStep)

      // STOP child nodes
      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getStopInstance && adapt.getRef.isInstanceOf[ContainerNode]
      })

      // REMOVE child nodes
      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == CloudNode.REMOVE_NODE
      })

      //PROCESS STOP
      scheduling.schedule(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getStopInstance && !adapt.getRef.isInstanceOf[ContainerNode]
      }, false).foreach {
        p =>
          getStep.addAdaptations(p)
          setStep(adaptationModelFactory.createParallelStep())
          getCurrentStep.setNextStep(getStep)
          setCurrentStep(getStep)
      }

      // REMOVE BINDINGS
      createNextStep(adaptationModel.getAdaptations.filter {
        adapt =>
          adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getRemoveBinding ||
            adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getRemoveFragmentBinding
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getRemoveInstance
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getRemoveType
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getRemoveDeployUnit
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getAddThirdParty
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getUpdateDeployUnit
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getAddDeployUnit
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getAddType
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getAddInstance
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt =>
          adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getAddBinding ||
            adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getAddFragmentBinding
      })

      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getUpdateDictionaryInstance
      })


      // ADD child nodes
      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == CloudNode.ADD_NODE
      })

      // START child nodes
      createNextStep(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getStartInstance && adapt.getRef.isInstanceOf[ContainerNode]
      })

      var oldStep = getCurrentStep
      //PROCESS START
      scheduling.schedule(adaptationModel.getAdaptations.filter {
        adapt => adapt.getPrimitiveType.getName == JavaSePrimitive.instance$.getStartInstance && !adapt.getRef.isInstanceOf[ContainerNode]
      }, true).foreach {
        p =>
          getStep.addAdaptations(p)
          setStep(adaptationModelFactory.createParallelStep())
          getCurrentStep.setNextStep(getStep)
          oldStep = getCurrentStep
          setCurrentStep(getStep)
      }
      if (getStep.getAdaptations.isEmpty) {
        oldStep.setNextStep(null)
      }
    } else {
      adaptationModel.setOrderedPrimitiveSet(null)
    }
    clearSteps()
    adaptationModel

  }

  private def isContaining(step: ParallelStep): Boolean = {
    if (step != null) {
      // TODO must be tested
      (step.getAdaptations.exists(adaptation =>
        (adaptation.getPrimitiveType.getName == "UpdateDictionaryInstance" && !adaptation.getRef.isInstanceOf[Group])
          || (adaptation.getPrimitiveType.getName == "AddInstance" && !adaptation.getRef.isInstanceOf[Group])
          || (adaptation.getPrimitiveType.getName == "UpdateInstance" && !adaptation.getRef.isInstanceOf[Group])
          || (adaptation.getPrimitiveType.getName == "RemoveInstance" && !adaptation.getRef.isInstanceOf[Group])
          || (adaptation.getPrimitiveType.getName == "StartInstance" && !adaptation.getRef.isInstanceOf[Group])
          || (adaptation.getPrimitiveType.getName == "StopInstance" && !adaptation.getRef.isInstanceOf[Group]))
        || isContaining(step.getNextStep))
    } else {
      false
    }
  }

  protected def createNextStep(commands: java.util.List[AdaptationPrimitive]) {
    getStep.addAllAdaptations(commands)

    nextStep()
  }
}