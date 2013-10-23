package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.Channel
import org.kevoree.api.PrimitiveCommand
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.KevoreeChannelFragment

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
class AddFragmentBindingCommand(val c: Channel, val remoteNodeName: String, val nodeName: String, val registry:MutableMap<String, Any>): PrimitiveCommand {

    override fun execute(): Boolean {

        val kevoreeChannelFound = registry.get(c.path()!!) as KevoreeChannelFragment?
        if(kevoreeChannelFound != null){
            val bindmsg = FragmentBindMessage(kevoreeChannelFound,c.name!!,remoteNodeName)
            return (kevoreeChannelFound.processAdminMsg(bindmsg))
        } else {
            return false
        }
    }

    override fun undo() {
        RemoveFragmentBindingCommand(c, remoteNodeName, nodeName, registry).execute()
    }

    public fun toString() : String {
        return "AddFragmentBindingCommand " + c.name
    }

}
