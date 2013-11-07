package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.Instance
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.api.PrimitiveCommand
import org.kevoree.ContainerRoot
import org.kevoree.framework.kaspects.TypeDefinitionAspect
import org.kevoree.NodeType
import org.kevoree.framework.AbstractChannelFragment
import org.kevoree.framework.AbstractComponentType
import org.kevoree.framework.AbstractGroupType
import org.kevoree.ComponentInstance
import org.kevoree.framework.KInstance
import org.kevoree.Group
import org.kevoree.Channel
import org.kevoree.DeployUnit
import org.kevoree.log.Log
import org.kevoree.framework.AbstractNodeType
import org.kevoree.library.defaultNodeTypes.wrapper.KevoreeComponent
import org.kevoree.library.defaultNodeTypes.wrapper.KevoreeGroup
import org.kevoree.library.defaultNodeTypes.wrapper.ChannelTypeFragmentThread

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 17:53
 */

class AddInstance(val c: Instance, val nodeName: String, val modelservice: KevoreeModelHandlerService, val kscript: KevScriptEngineFactory, val bs: org.kevoree.api.Bootstraper, val nt : AbstractNodeType, val registry:MutableMap<String, Any>): PrimitiveCommand, Runnable {

    private val typeDefinitionAspect = TypeDefinitionAspect()
    var deployUnit : DeployUnit? = null
    var nodeTypeName : String? = null
    var tg : ThreadGroup? = null

    var resultSub = false

    override fun execute(): Boolean {
        val model = c.typeDefinition!!.eContainer() as ContainerRoot
        val node = model.findNodesByID(nodeName)
        deployUnit = typeDefinitionAspect.foundRelevantDeployUnit(c.typeDefinition!!, node!!)!!
        val nodeType = node.typeDefinition
        nodeTypeName = typeDefinitionAspect.foundRelevantHostNodeType(nodeType as NodeType, c.typeDefinition!!)!!.name!!
        var subThread : Thread? = null
        try {
            tg = ThreadGroup("kev/"+c.path()!!)
            subThread = Thread(tg,this)
            subThread!!.start()
            subThread!!.join()
            return resultSub
        } catch(e: Throwable) {
            if(subThread != null){
                try {
                    subThread!!.stop() //kill sub thread
                } catch(t : Throwable){
                    //ignore killing thread
                }
            }
            val message = "Could not add the instance " + c.name!! + ":" + c.typeDefinition!!.name!!
            Log.error(message, e)
            return false
        }
    }

    override fun undo() {
        RemoveInstance(c, nodeName, modelservice, kscript, bs,nt, registry).execute()
    }

    public override fun run() {
        try {
            val beanClazz = bs.getKevoreeClassLoaderHandler().getKevoreeClassLoader(deployUnit)!!.loadClass(c.typeDefinition!!.bean)
            val newBeanInstance = beanClazz!!.newInstance()
            var newBeanKInstanceWrapper :KInstance? = null
            if(c is ComponentInstance){
                newBeanKInstanceWrapper = KevoreeComponent(newBeanInstance as AbstractComponentType,nodeName,c.name!!,modelservice,bs,kscript,nt.getDataSpaceService(),tg!!)
                (newBeanKInstanceWrapper as KevoreeComponent).initPorts(nodeTypeName!!,c)
            }
            if(c is Group){
                newBeanKInstanceWrapper = KevoreeGroup(newBeanInstance as AbstractGroupType,nodeName,c.name!!,modelservice,bs,kscript,nt.getDataSpaceService(),tg!!)
            }
            if(c is Channel){
                newBeanKInstanceWrapper = ChannelTypeFragmentThread(newBeanInstance as AbstractChannelFragment,nodeName,c.name!!,modelservice,bs,kscript,nt.getDataSpaceService(),tg!!)
                (newBeanKInstanceWrapper as ChannelTypeFragmentThread).initChannel()
            }

            registry.put(c.path()!!, newBeanKInstanceWrapper!!)
            resultSub = true
        } catch(e : Throwable){
            Log.error("Error while adding instance {}",e,c.name)
            resultSub = false
        }


    }


    public override fun toString(): String? {
        return "AddInstance " + c.name
    }
}