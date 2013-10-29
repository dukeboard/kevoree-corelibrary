package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.MBinding
import org.kevoree.api.PrimitiveCommand
import org.kevoree.ComponentInstance
import org.kevoree.framework.AbstractComponentType
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.message.PortUnbindMessage
import org.kevoree.framework.message.FragmentUnbindMessage
import org.kevoree.log.Log

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


class RemoveBindingCommand(val c: MBinding, val nodeName: String, val registry: MutableMap<String, Any>) : PrimitiveCommand {

    override fun undo() {
        AddBindingCommand(c, nodeName, registry).execute()
    }

    override fun execute(): Boolean {
        if(c == null){
            return false
        }else{

            val kevoreeChannelFound = registry.get(c.hub!!.path()!!)
            val kevoreeComponentFound = registry.get((c.port!!.eContainer() as ComponentInstance).path()!!)
            //            val kevoreeChannelFound = KevoreeDeployManager.getRef(c.hub.javaClass.getName()+"_wrapper", c.hub!!.name!!)
            //            val kevoreeComponentFound = KevoreeDeployManager.getRef((c.port!!.eContainer()as ComponentInstance).javaClass.getName(), (c.port!!.eContainer()as ComponentInstance).name!!)
            if(kevoreeChannelFound != null && kevoreeComponentFound != null && kevoreeComponentFound is AbstractComponentType && kevoreeChannelFound is KevoreeChannelFragment){
                //                val casted = kevoreeComponentFound as AbstractComponentType
                //                val channelCasted = kevoreeChannelFound as KevoreeChannelFragment
                val foundNeedPort = kevoreeComponentFound.getNeededPorts()!!.get(c.port!!.portTypeRef!!.name)
                val foundHostedPort = kevoreeComponentFound.getHostedPorts()!!.get(c.port!!.portTypeRef!!.name)

                if(foundNeedPort == null && foundHostedPort == null){
                    Log.info("Port instance not found in component")
                    Log.info("Look for " + c.port!!.portTypeRef!!.name);
                    Log.info(kevoreeComponentFound.getNeededPorts()!!.values().toString());
                    return false
                }
                if (foundNeedPort != null) {
                    /* Bind port to Channel */
                    val newbindmsg = FragmentUnbindMessage(c.hub!!.name!!, null)
                    return (foundNeedPort as KevoreePort).processAdminMsg(newbindmsg)
                }
                if(foundHostedPort != null){
                    val cname = (c.port!!.eContainer() as ComponentInstance).name!!
                    val bindmsg = PortUnbindMessage(nodeName, cname, (foundHostedPort as KevoreePort).getName()!!)
                    return (kevoreeChannelFound as KevoreeChannelFragment).processAdminMsg(bindmsg)
                }
                return false
            } else {
                return false
            }
        }
    }

    fun toString(): String {
        return "RemoveBindingCommand " + c.hub!!.name + "<->" + (c.port!!.eContainer() as ComponentInstance).name
    }

}
