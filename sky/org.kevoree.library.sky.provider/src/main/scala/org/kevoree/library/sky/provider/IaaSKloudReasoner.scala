package org.kevoree.library.sky.provider

import org.kevoree.library.sky.helper.{KloudNetworkHelper, KloudModelHelper}
import org.kevoree.{ContainerNode, NodeType, ContainerRoot}
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.framework.{KevoreePropertyHelper, Constants}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer
import java.util
import org.kevoree.log.Log

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/11/12
 * Time: 11:44
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object IaaSKloudReasoner extends KloudReasoner {

  def configureIsolatedNodes(iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    var doSomething = false
    var usedPorts = KloudNetworkHelper.lisAllPorts(iaasModel)
    iaasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(iaasModel, n) && iaasModel.getGroups.forall(g => !g.getSubNodes.contains(n))).foreach {
      node =>
        val parentNodeOption = node.getHost
        if (parentNodeOption != null) {
          val ips: util.List[String] = KevoreePropertyHelper.instance$.getNetworkProperties(iaasModel, node.getName, org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP)
          if (ips.size() > 0) {
            val portNumber = configureIsolatedNode(node, parentNodeOption.getName, ips, iaasModel, kengine, usedPorts)
            if (portNumber != 0) {
              doSomething = doSomething && true
              usedPorts += portNumber
            } else {
              doSomething = doSomething && false
            }
          }
        }
    }
    Log.debug("configure isolated nodes : {}", doSomething)
    doSomething
  }

  def configureChildNodes(iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    // count current child for each Parent nodes
    val parents = countChilds(iaasModel)
    var potentialParents = ListBuffer[String]()

    var doSomething = false
    var usedIps = KloudNetworkHelper.listAllIp(iaasModel)
    var usedPorts = KloudNetworkHelper.lisAllPorts(iaasModel)
    // filter nodes that are not IaaSNode and are not child of IaaSNode
    iaasModel.getNodes.filter(n => n.getHost == null && KloudModelHelper.isPaaSNode(iaasModel, n)).foreach {
      // select a host for each user node
      node => {
        Log.debug("try to select a parent for {}", node.getName)

        if (potentialParents.isEmpty) {
          potentialParents = lookAtPotentialParents(parents)
        }
        val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
        val parentName = potentialParents(index)
        potentialParents.remove(index)
        kengine.addVariable("nodeName", node.getName)
        kengine.addVariable("parentName", parentName)
        kengine append "addChild {nodeName}@{parentName}"

        // define IP using selected node to know what is the network used in this machine
        val ips = new util.ArrayList[String]()
        val ipOption = defineIP(node.getName, parentName, iaasModel, kengine, usedIps)
        if (ipOption.isDefined) {
          usedIps += ipOption.get
          ips.add(ipOption.get)
        }
        val portNumber = configureIsolatedNode(node, parentName, ips, iaasModel, kengine, usedPorts)
        if (portNumber != 0) {
          usedPorts += portNumber
        }

        // find corresponding Kloud group to update the user group configuration on the kloud
        iaasModel.getGroups.filter(g => KloudModelHelper.isPaaSKloudGroup(iaasModel, g) && g.findByPath("subNodes[" + node.getName + "]", classOf[ContainerNode]) != null).foreach {
          group =>
            kengine.addVariable("groupName", group.getName)
            kengine append "updateDictionary {groupName} {ip='{ip}'}@{nodeName}"
        }

        Log.debug("Add {} as child of {}", node.getName, parentName)
        doSomething = true
      }
    }
    Log.debug("configure child nodes : {}", doSomething)
    doSomething
  }

  def countChilds(kloudModel: ContainerRoot): util.List[(String, Int)] = {
    var counts = List[(String, Int)]()
    kloudModel.getNodes.filter {
      node =>
        val nodeType: NodeType = node.getTypeDefinition.asInstanceOf[NodeType]
        nodeType.getManagedPrimitiveTypes.filter(primitive => primitive.getName.toLowerCase == "addnode"
          || primitive.getName.toLowerCase == "removenode").size == 2
    }.foreach {
      node =>
        counts = counts ++ List[(String, Int)]((node.getName, node.getHosts.size))
    }
    counts
  }

  def addNodes(addedNodes: java.util.List[ContainerNode], parentNodeNameOption: Option[String], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    if (!addedNodes.isEmpty) {
      Log.debug("Try to add {} user nodes into the Kloud", addedNodes.size())

      // create new node using PJavaSENode as type for each user node
      var usedIps = KloudNetworkHelper.listAllIp(iaasModel)
      var usedPorts = KloudNetworkHelper.lisAllPorts(iaasModel)
      addedNodes.foreach {
        node =>
          kengine.addVariable("nodeName", node.getName)
          kengine.addVariable("nodeType", node.getTypeDefinition.getName)
          // TODO maybe we need to merge the deploy unit that offer this type if it is not one of our types
          // add node
          Log.debug("addNode {} : {}", Array[String](node.getName, node.getTypeDefinition.getName))
          kengine append "addNode {nodeName} : {nodeType}"
          // set dictionary attributes of node
          if (node.getDictionary != null) {
            val defaultAttributes = getDefaultNodeAttributes(iaasModel, node.getTypeDefinition.getName)
            node.getDictionary.getValues
              .filter(value => defaultAttributes.find(a => a.getName == value.getAttribute.getName) match {
              case Some(attribute) => true
              case None => false
            }).foreach {
              value =>
                kengine.addVariable("attributeName", value.getAttribute.getName)
                kengine.addVariable("attributeValue", value.getValue)
                kengine append "updateDictionary {nodeName} {{attributeName} = '{attributeValue}'}"
            }
          }
          Log.debug("{}", parentNodeNameOption)
          var parentName = ""
          if (parentNodeNameOption.isEmpty) {
            var potentialParents = ListBuffer[String]()
            if (potentialParents.isEmpty) {
              val parents = countChilds(iaasModel)
              Log.debug("parents: {}", parents.mkString(", "))
              potentialParents = lookAtPotentialParents(parents)
              Log.debug("Potential parents: {}", potentialParents.mkString(", "))
            }
            val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
            parentName = potentialParents(index)
            potentialParents.remove(index)
          } else {
            parentName = parentNodeNameOption.get
          }

          kengine.addVariable("parentName", parentName)
          kengine append "addChild {nodeName}@{parentName}"

          val ipOption = defineIP(node.getName, parentName, iaasModel, kengine, usedIps)
          val ips = new util.ArrayList[String]()
          if (ipOption.isDefined) {
            usedIps += ipOption.get
            ips.add(ipOption.get)
          }
          val portNumber = configureIsolatedNode(node, parentName, ips, iaasModel, kengine, usedPorts)
          if (portNumber != 0) {
            usedPorts += portNumber
          }
      }
      true
    } else {
      true
    }
  }

  def startNodes(startedNodes: java.util.List[ContainerNode], parentNodeNameOption: Option[String], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    if (!startedNodes.isEmpty) {
      Log.debug("Try to start {} user nodes into the Kloud", startedNodes.size())

      startedNodes.forall {
        node =>
          if (node.getHost == null || (parentNodeNameOption.isDefined && node.getHost != null && node.getHost.getName == parentNodeNameOption.get)) {
            kengine.addVariable("nodeName", node.getName)
            if (node.getHost != null) {
              kengine.addVariable("parentNodeName", node.getHost.getName)
              Log.debug("startInstance {} : {}", node.getName, node.getTypeDefinition.getName)
              kengine append "startInstance {nodeName} @ {parentNodeName}"
            } else {
              Log.debug("startInstance {}", node.getName)
              kengine append "startInstance {nodeName}"
            }

            true
          } else {
            Log.debug("Unable to start the node {} because its parent is not the specified one")
            false
          }
      }
    } else {
      true
    }
  }

  def stopNodes(stoppedNodes: java.util.List[ContainerNode], parentNodeNameOption: Option[String], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    if (!stoppedNodes.isEmpty) {
      Log.debug("Try to stop {} user nodes into the Kloud", stoppedNodes.size())

      stoppedNodes.forall {
        node =>
          Log.warn(node.getName)
          Log.warn(parentNodeNameOption.toString)
          Log.warn("" + node.getHost)
          if (node.getHost == null || (parentNodeNameOption.isDefined && node.getHost != null && node.getHost.getName == parentNodeNameOption.get)) {
            kengine.addVariable("nodeName", node.getName)
            if (node.getHost != null) {
              kengine.addVariable("parentNodeName", node.getHost.getName)
              Log.debug("stopInstance {} @ {}", node.getName, node.getTypeDefinition.getName)
              kengine append "stopInstance {nodeName} @ {parentNodeName}"
            } else {
              Log.debug("stopInstance {}", node.getName)
              kengine append "stopInstance {nodeName}"
            }
            true
          } else {
            Log.debug("Unable to start the node {} because its parent is not the specified one")
            false
          }
      }
    } else {
      true
    }
  }

  private def configureIsolatedNode(node: ContainerNode, parentNodeName: String, ips: util.List[String], model: ContainerRoot, kengine: KevScriptEngine, usedPorts: ListBuffer[Int]): Int = {
    // find all groups that are linked to the parent node
    val groups = model.getGroups.filter(g => g.findByPath("subNodes[" + parentNodeName + "]", classOf[ContainerNode]) != null)
    // look for a group that is linked to the node
    val groupOption = groups.find(g => g.findByPath("subNodes[" + node.getName + "]", classOf[ContainerNode]) != null)
    if (groupOption.isEmpty) {
      var portAttributeName = "port"
      var portNumber = 8000
      // get all fragment properties that exist for the group and that start or end with 'port', we try to specify a port for the node
      if (groups(0).getTypeDefinition.getDictionaryType != null) {
        groups(0).getTypeDefinition.getDictionaryType.getAttributes.filter(a => a.getFragmentDependant && (a.getName == "port" || a.getName.startsWith("port") || a.getName.endsWith("port"))).foreach {
          attribute => {
            var dv = 8000
            val dvOption = groups(0).getTypeDefinition.getDictionaryType.getDefaultValues.find(dv => dv.getAttribute.getName == attribute.getName)
            if (dvOption.isDefined) {
              try {
                dv = Integer.parseInt(dvOption.get.toString)
              } catch {
                case e: NumberFormatException =>
              }
            }
            /* Warning This method try severals Socket to determine available port */
            portNumber = KloudNetworkHelper.selectPortNumber(dv, ips, usedPorts)
            portAttributeName = attribute.getName
          }
        }
      }

      // add the new node on one of the group used by the host
      kengine addVariable("groupName", groups(0).getName)
      kengine append "addToGroup {groupName} {nodeName}"

      kengine addVariable("attributeName", portAttributeName)
      kengine.addVariable("port", portNumber.toString)
      kengine append "updateDictionary {groupName} { {attributeName} = '{port}' }@{nodeName}"
      portNumber
    } else {
      0
    }
  }

  private def defineIP(nodeName: String, parentName: String, model: ContainerRoot, kengine: KevScriptEngine, usedIps: ListBuffer[String]): Option[String] = {
    kengine.addVariable("nodeName", nodeName)
    kengine.addVariable("parentName", parentName)
    // define IP using selecting node to know what is the network used in this machine
    val ipOption = KloudNetworkHelper.selectIP(parentName, model, usedIps)
    if (ipOption.isDefined) {
      kengine.addVariable("ipKey", Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP)
      kengine.addVariable("ip", ipOption.get)
      kengine append "network {nodeName} {'{ipKey}' = '{ip}' }\n"
    } else {
      Log.debug("Unable to select an IP for {}", nodeName)
    }

    Log.debug("IP {} has been selected for the node {} on the host {}", ipOption, nodeName, parentName)
    ipOption
  }

  def lookAtPotentialParents(parents: util.List[(String, Int)]): ListBuffer[String] = {
    val potentialParents = ListBuffer[String]()
    var min = Int.MaxValue

    parents.foreach {
      parent =>
        if (parent._2 < min) {
          min = parent._2
          potentialParents.clear()
          potentialParents.add(parent._1)
        } else if (parent._2 == min) {
          potentialParents.add(parent._1)
        }
    }
    potentialParents
  }
}
