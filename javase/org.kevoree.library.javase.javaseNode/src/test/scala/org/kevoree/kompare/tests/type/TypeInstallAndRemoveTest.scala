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

package org.kevoree.kompare.tests.`type`

import org.kevoree.kompare.tests.KompareSuite
import org.junit._
import org.kevoree.library.defaultNodeTypes.planning.KevoreeKompareBean
import org.kevoreeadaptation._
import org.kevoree.kompare.{JavaSePrimitive, KevoreeKompareBean}
import org.kevoree.{DeployUnit, NamedElement}

class TypeInstallAndRemoveTest extends KompareSuite {

  var component : KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def testNoTypeInstall(){
    val kompareModel = component.kompare(emptyModel, model("test_type/noTypeInstall.art2"), "duke")
    kompareModel.print
    //kompareModel verifySize 2
    kompareModel verifySize 0 //now don't use kompare for bootstrap
  }

  @Test def testOnlyOneDeployUnitInstall_INITNODE(){
    val kompareModel = component.kompare(emptyModel, model("test_type/onlyOneDeployUnitInstall.art2"), "duke")
    kompareModel.print
    kompareModel shouldContainSize(JavaSePrimitive.AddDeployUnit,1) //don't add java node now
  }

  @Test def testOnlyOneDeployUnitInstall_UPDATENODE(){
    val kompareModel = component.kompare(model("test_type/noTypeInstall.art2"), model("test_type/onlyOneDeployUnitInstall.art2"), "duke")
    kompareModel shouldContainSize(JavaSePrimitive.AddDeployUnit,1)
    kompareModel shouldContain(JavaSePrimitive.AddType,"ComponentPrimitiveTypeService")
    kompareModel shouldContain(JavaSePrimitive.AddType,"ComponentB")
    kompareModel shouldContain(JavaSePrimitive.AddInstance,"ComponentB--10313997")
    kompareModel shouldContain(JavaSePrimitive.AddInstance,"ComponentPrimitiveTypeService--791402174")
  }

  @Test def testnoTypeInstall_UPDATENODE(){
    val kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"), model("test_type/onlyOneDeployUnitInstall.art2"), "duke")
    kompareModel verifySize 0
  }

  @Test def testnoTypeDeployUnitUninstall_UPDATENODE(){
    val kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"), model("test_type/noTypeDeployUnitUninstall.art2"), "duke")
    kompareModel shouldContainSize(JavaSePrimitive.RemoveDeployUnit,0)
  }


  @Test def testOnlyOneDeployUnitUninstall_UPDATENODE(){
    val kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"), model("test_type/onlyOneDeployUnitUninstall.art2"), "duke")
    kompareModel shouldContainSize(JavaSePrimitive.RemoveDeployUnit,1)
    kompareModel shouldContain(JavaSePrimitive.RemoveType,"ComponentPrimitiveTypeService")
    kompareModel shouldContain(JavaSePrimitive.RemoveType,"ComponentB")
    kompareModel shouldContain(JavaSePrimitive.RemoveInstance,"ComponentB--10313997")
    kompareModel shouldContain(JavaSePrimitive.RemoveInstance,"ComponentPrimitiveTypeService--791402174")
  }

  @Test def testOnlyOneDeployUnitUninstall_STOPNODE(){
    val kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"),emptyModel, "duke")
    kompareModel.print
    kompareModel shouldContainSize(JavaSePrimitive.RemoveDeployUnit(),1)//don't remove javaNode DU now
  }

  @Test def testuninstall_STOPNODE(){
    val kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"),emptyModel, "duke")
    kompareModel.print
    kompareModel shouldContainSize(JavaSePrimitive.RemoveDeployUnit(),1) //don't uninstall node instance
    kompareModel shouldContain(JavaSePrimitive.RemoveType(),"ComponentPrimitiveTypeService")
    kompareModel shouldContain(JavaSePrimitive.RemoveType(),"ComponentB")
    kompareModel shouldContain(JavaSePrimitive.RemoveInstance(),"ComponentB--10313997")
    kompareModel shouldContain(JavaSePrimitive.RemoveInstance(),"ComponentPrimitiveTypeService--791402174")
  }

}

