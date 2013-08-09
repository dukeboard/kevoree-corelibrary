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
package org.kevoree.library.sky.api.execution;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.library.sky.api.helper.ProcessStreamFileLogger;
import org.kevoree.log.Log;

import java.io.*;
import java.util.List;


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class KevoreeNodeRunner {



    private File outFile;

    private File errFile;

    private String nodeName;

    public abstract boolean addNode(ContainerRoot iaasModel, ContainerRoot childBootStrapModel);

    public abstract boolean startNode(ContainerRoot iaasModel, ContainerRoot childBootStrapModel);

    public abstract boolean stopNode();

    public abstract boolean removeNode();

    protected KevoreeNodeRunner(String nodeName) {
        this.nodeName = nodeName;
    }

    public File getOutFile() {
        return outFile;
    }

    public File getErrFile() {
        return errFile;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public void setErrFile(File errFile) {
        this.errFile = errFile;
    }

    public String getNodeName() {
        return nodeName;
    }

    /**
     * configure the ssh server
     *
     * @param path
     * @param ips
     */
    public void configureSSHServer(String path, List<String> ips) {
        if (ips.size() > 0) {
            Log.debug("configure ssh server ip");
            StringBuilder builder = new StringBuilder();
            for (String ip : ips) {
                builder.append("ListenAddress ").append(ip).append("\n");
            }
            try {
                replaceStringIntoFile("#ListenAddress 0.0.0.0", builder.toString(), path + File.separator + "etc" + File.separator + "ssh" + File.separator + "sshd_config");
            } catch (Exception e) {
                Log.debug("Unable to configure ssh server", e);
            }
        }
    }

    private boolean isASubType(TypeDefinition nodeType, String typeName) {
        for (TypeDefinition superType : nodeType.getSuperTypes()) {
            if (superType.getName().equals(typeName) || isASubType(superType, typeName)) {
                return true;
            }
        }
        return false;
    }

    //  @throws(classOf[Exception])
    private void copyStringToFile(String data, String outputFile) throws Exception {
        if (data != null && !data.equals("")) {
            if (new File(outputFile).exists()) {
                new File(outputFile).delete();
            }
            DataOutputStream writer = new DataOutputStream(new FileOutputStream(new File(outputFile)));
            writer.write(data.getBytes(), 0, data.getBytes().length);
            writer.flush();
            writer.close();
        }
    }

    public boolean copyFile(String inputFile, String outputFile) {
        if (new File(inputFile).exists()) {
            try {
                if (new File(outputFile).exists()) {
                    new File(outputFile).delete();
                }
                DataInputStream reader = new DataInputStream(new FileInputStream(new File(inputFile)));
                DataOutputStream writer = new DataOutputStream(new FileOutputStream(new File(outputFile)));

                byte[] bytes = new byte[2048];
                int length = reader.read(bytes);
                while (length != -1) {
                    writer.write(bytes, 0, length);
                    length = reader.read(bytes);

                }
                writer.flush();
                writer.close();
                reader.close();
                return true;
            } catch (Exception e) {
                Log.error("Unable to copy " + inputFile + " on " + outputFile, e);
                return false;
            }
        } else {
            Log.debug("Unable to find {}", inputFile);
            return false;
        }
    }

    //  @throws(classOf[java.lang.StringIndexOutOfBoundsException])
    private void replaceStringIntoFile(String dataToReplace, String newData, String file) throws Exception {
        if (dataToReplace != null && !dataToReplace.equals("") && newData != null && !newData.equals("")) {
            if (new File(file).exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                DataInputStream reader = new DataInputStream(new FileInputStream(new File(file)));
                ByteArrayOutputStream writer = new ByteArrayOutputStream();

                byte[] bytes = new byte[2048];
                int length = reader.read(bytes);
                while (length != -1) {
                    writer.write(bytes, 0, length);
                    length = reader.read(bytes);

                }
                writer.flush();
                writer.close();
                reader.close();
                stringBuilder.append(new String(writer.toByteArray()));
                if (stringBuilder.indexOf(dataToReplace) == -1) {
                    Log.debug("Unable to find {} on file {} so replacement cannot be done", dataToReplace, file);
                } else {
                    stringBuilder.replace(stringBuilder.indexOf(dataToReplace), stringBuilder.indexOf(dataToReplace) + dataToReplace.length(), newData);

                    copyStringToFile(stringBuilder.toString(), file);
                }
            } else {
                Log.debug("The file {} doesn't exist, nothing can be replace.", file);
            }
        }
    }


    public String findVersionForChildNode(String nodeName, ContainerRoot model, ContainerNode iaasNode) {
        DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
        Log.debug("looking for Kevoree version for node {}", nodeName);
        ContainerNode node = model.findNodesByID(nodeName);
        if (node != null) {
            Log.debug("looking for deploy unit");
            for (DeployUnit dp : node.getTypeDefinition().getDeployUnits()) {
                if (dp.getTargetNodeType().getName().equals(iaasNode.getTypeDefinition().getName()) || isASubType(iaasNode.getTypeDefinition(), dp.getTargetNodeType().getName())) {
                    Log.debug("looking for version of kevoree framework for the found deploy unit");
                    for (DeployUnit dep : dp.getRequiredLibs()) {
                        if (dp.getUnitName().equals("org.kevoree") && dep.getGroupName().equals("org.kevoree.framework")) {
                            dep.getVersion();
                        }
                    }
                }
            }
        }
        return factory.getVersion();
    }

    public void configureLogFile(AbstractNodeType iaasNode, Process process) {
        String logFolder = System.getProperty("java.io.tmpdir");
        if (iaasNode.getDictionary().get("log_folder") != null && new File(iaasNode.getDictionary().get("log_folder").toString()).exists()) {
            logFolder = iaasNode.getDictionary().get("log_folder").toString();
        }
        String logFile = logFolder + File.separator + nodeName + ".log";
        outFile = new File(logFile + ".out");
        outFile.deleteOnExit();
        Log.info("writing logs about {} on {}", nodeName, outFile.getAbsolutePath());
        new Thread(new ProcessStreamFileLogger(process.getInputStream(), outFile)).start();
        errFile = new File(logFile + ".err");
        errFile.deleteOnExit();
        Log.info("writing logs about {} on {}", nodeName, errFile.getAbsolutePath());
        new Thread(new ProcessStreamFileLogger(process.getErrorStream(), errFile)).start();
    }
}

