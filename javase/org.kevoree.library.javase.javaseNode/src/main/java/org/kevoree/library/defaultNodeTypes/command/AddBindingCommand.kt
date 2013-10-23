package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.MBinding
import org.kevoree.api.PrimitiveCommand
import org.kevoree.ComponentInstance
import org.kevoree.framework.AbstractComponentType
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.framework.message.PortBindMessage
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



class AddBindingCommand(val c: MBinding, val nodeName: String, val registry: MutableMap<String, Any>) : PrimitiveCommand {

    override fun undo() {
        RemoveBindingCommand(c, nodeName, registry).execute()
    }
    override fun execute(): Boolean {
        val kevoreeChannelFound = registry.get(c.hub!!.path()!!)
        val kevoreeComponentFound = registry.get((c.port!!.eContainer() as ComponentInstance).path()!!)
        if(kevoreeChannelFound != null && kevoreeComponentFound != null && kevoreeComponentFound is AbstractComponentType && kevoreeChannelFound is KevoreeChannelFragment){
            val portName = c.port!!.portTypeRef!!.name!!
            val foundNeedPort = kevoreeComponentFound.getNeededPorts()!!.get(portName)
            val foundHostedPort = kevoreeComponentFound.getHostedPorts()!!.get(portName)
            if(foundNeedPort == null && foundHostedPort == null){
                Log.info("Port instance not found in component")
                Log.info("Look for " + portName);
                Log.info("" + kevoreeComponentFound.getNeededPorts()!!.containsKey(portName));
                Log.info("" + kevoreeComponentFound.getHostedPorts()!!.containsKey(portName));
                return false
            }

            if (foundNeedPort != null) {
                /* Bind port to Channel */
                val newbindmsg = FragmentBindMessage(kevoreeChannelFound, c.hub!!.name!!, nodeName)
                return (foundNeedPort as KevoreePort).processAdminMsg(newbindmsg)
            }
            if(foundHostedPort != null){
                val compoName = (c.port!!.eContainer() as ComponentInstance).name!!
                val bindmsg = PortBindMessage(foundHostedPort as KevoreePort, nodeName, compoName, (foundHostedPort as KevoreePort).getName()!!)
                return kevoreeChannelFound.processAdminMsg(bindmsg)
            }
            return false
        } else {
            Log.error("Error while apply binding , channelFound=" + kevoreeChannelFound + ",componentFound=" + kevoreeComponentFound)
            return false
        }
    }

    fun toString(): String {
        return "AddBindingCommand " + c.hub!!.name + "<->" + (c.port!!.eContainer() as ComponentInstance).name;
    }
}
