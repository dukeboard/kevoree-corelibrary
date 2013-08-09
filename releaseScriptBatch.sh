#
# Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.gnu.org/licenses/lgpl-3.0.txt
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Authors:
# Fouquet Francois
# Nain Gregory
#

export MAVEN_OPTS="-Xms2048m -Xmx2048m -XX:PermSize=512m -XX:MaxPermSize=1024m"
export JAVA_HOME=$(/usr/libexec/java_home)
mvn -B release:clean
mvn -B release:prepare
mvn -B release:perform