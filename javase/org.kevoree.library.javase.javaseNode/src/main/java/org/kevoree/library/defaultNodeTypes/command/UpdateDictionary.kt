package org.kevoree.library.defaultNodeTypes.command

import java.util.HashMap
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.api.PrimitiveCommand
import org.kevoree.framework.KInstance

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class UpdateDictionary(val c: Instance, val nodeName: String, val registry: MutableMap<String, Any>) : PrimitiveCommand {

    private var lastDictioanry: Map<String, Any>? = null


    override fun execute(): Boolean {
        //BUILD MAP
        val dictionary = HashMap<String, Any>()
        if (c.typeDefinition!!.dictionaryType != null) {
            for(dv in c.typeDefinition!!.dictionaryType!!.defaultValues) {
                dictionary.put(dv.attribute!!.name!!, dv.value!!)
            }
        }

        if (c.dictionary != null) {
            for(v in c.dictionary!!.values){
                if (v.attribute!!.fragmentDependant!!) {
                    val tn = v.targetNode
                    if (tn != null) {
                        if (tn.name == nodeName) {
                            dictionary.put(v.attribute!!.name!!, v.value!!)
                        }
                    }
                } else {
                    dictionary.put(v.attribute!!.name!!, v.value!!)
                }
            }
        }

        val reffound = registry.get(c.path()!!)
        if(reffound != null && reffound is KInstance){
            val iact = reffound as KInstance
            val previousCL = Thread.currentThread().getContextClassLoader()
            Thread.currentThread().setContextClassLoader(iact.javaClass.getClassLoader())
            lastDictioanry = iact.kUpdateDictionary(dictionary, c.typeDefinition!!.eContainer() as ContainerRoot)
            Thread.currentThread().setContextClassLoader(previousCL)
            return lastDictioanry != null
        } else {
            return false
        }

    }

    override fun undo() {
        val mapFound = registry.get(c.path()!!)
        val tempHash = HashMap<String, Any>()
        if (lastDictioanry != null) {
            tempHash.putAll(lastDictioanry!!);
        }
        if(mapFound != null && mapFound is KInstance){
            val iact = mapFound as KInstance
            val previousCL = Thread.currentThread().getContextClassLoader()
            Thread.currentThread().setContextClassLoader(iact.javaClass.getClassLoader())
            lastDictioanry = iact.kUpdateDictionary(tempHash, c.typeDefinition!!.eContainer() as ContainerRoot)
            Thread.currentThread().setContextClassLoader(previousCL)
        }
    }

    fun toString(): String {
        return "UpdateDictionary " + c.name
    }

}
