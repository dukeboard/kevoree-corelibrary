package org.kevoree.thesis;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper;
import org.kevoree.tools.marShell.KevScriptOfflineEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/11/12
 * Time: 18:22
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "Test")
@ComponentType
public class ModelGenerator {
    static DefaultKevoreeFactory defaultKevoreeFactory = new DefaultKevoreeFactory();

    public static void main(String[] args) {
        String folderPath = null;
        String defaultScriptPath = null;
        String defaultNodeName = null;
        String hostNodeTypes = null;
        int nbNodes = -1;
        int nbComponents = -1;
        String[] componentTypes = null;
        boolean defineHosting = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-script")) {
                defaultScriptPath = args[i+1];
            }
            if (args[i].equalsIgnoreCase("-targetFolder")) {
                folderPath = args[i+1];
            }
            if (args[i].equalsIgnoreCase("-defaultNode")) {
                defaultNodeName = args[i+1];
            }
            if (args[i].equalsIgnoreCase("-hostNodeTypes")) {
                hostNodeTypes = args[i+1];
            }
            if (args[i].equalsIgnoreCase("-nbNodes")) {
                nbNodes = Integer.parseInt(args[i+1]);
            }
            if (args[i].equalsIgnoreCase("-defineHosting")) {
                defineHosting = args[i+1].equalsIgnoreCase("true");
            }
            i++;
        }

        if (folderPath == null || defaultScriptPath == null || defaultNodeName == null || hostNodeTypes == null || nbNodes == -1) {
            System.out.println("Missing arguments.\nYou must use something like that: -script <myscript> -targetFolder <myFolder> -defaultNode <nodeName> -hostNodeTypes <myNodeType1,myNodeType2> -nbNodes <number of nodes to create> [-defineHosting <true|false>]");
            System.exit(-1);
        }

        generateKevScript(folderPath + "/kloud.kevs", nbNodes, defineHosting, defaultScriptPath, defaultNodeName, hostNodeTypes);
        generateModel(folderPath + "/kloud.kevs", folderPath + "kloud.kev");

        System.out.println("Now you need to start a runtime with this model saved on " + folderPath + "kloud.kev");
        System.out.println("If you prefer KevScript, please use " + folderPath + "kloud.kevs");
    }

    private static void generateModel(String kevScript, String storageModel) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BufferedReader fileStream = new BufferedReader(new FileReader(new File(kevScript)));

            KevScriptEngine kengine = new KevScriptOfflineEngine(defaultKevoreeFactory.createContainerRoot(), new NodeTypeBootstrapHelper());
            String line = fileStream.readLine();
            while (line != null) {
                kengine.append(line).append("\n");
                line = fileStream.readLine();
            }

            System.out.println("starting to generate model: " + System.currentTimeMillis());
            KevoreeXmiHelper.instance$.save(storageModel, kengine.interpret());
            System.out.println("finishing to generate model: " + System.currentTimeMillis());
        } catch (KevScriptEngineException e) {
            System.err.println("Unable to save the generated model");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String loadDefaultScript(String defaultScriptPath) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(new File(defaultScriptPath)));
            String line = reader.readLine();
            while (line != null) {
                builder.append(line).append("\n");
                line = reader.readLine();
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            System.out.println("Unable to load the default script");
            System.exit(-2);
        } catch (IOException e) {
            System.out.println("Unable to load the default script");
            System.exit(-3);
        }
        return null;
    }

    private static List<String> findHostingNode(String script, String hostNodeTypes) {
        List<String> hostNodeNames = new ArrayList<String>();

        for (String line : script.split("\n")) {
            if (line.trim().startsWith("addNode")) {
                if (hostNodeTypes.contains(line.split(":")[1].split("\\{")[0].trim())) {
                    hostNodeNames.add(line.trim().substring("addNode".length(), line.trim().indexOf(":")).trim());
                }
            }
        }
        return hostNodeNames;
    }

    private static void generateKevScript(String storageModel, int nbNode, boolean defineParentNode, String defaultScriptPath,
                                          String defaultNodeName, String hostNodeTypes) {
        System.out.println("Building model...");

        KevScriptEngine kengine = new KevScriptOfflineEngine(defaultKevoreeFactory.createContainerRoot(), new NodeTypeBootstrapHelper());

        kengine.addVariable("defaultNodeName", defaultNodeName);
        kengine.addVariable("kevoree.version", defaultKevoreeFactory.getVersion());

        String defaultScript = loadDefaultScript(defaultScriptPath);
        kengine.append(defaultScript);

        List<String> hostNodeNames = findHostingNode(defaultScript, hostNodeTypes);

        kengine.append("merge 'mvn:org.kevoree.thesis/org.kevoree.erwan.thesis/1.0-SNAPSHOT'");
        kengine.append("addComponent modelSubmitter@{defaultNodeName} :ThesisModelSubmitter {model = '{test.model}'}");

        for (int i = 0; i < nbNode; i++) {
            kengine.addVariable("childName", "childNode" + i);
            kengine.addVariable("parentNodeName", hostNodeNames.get(i % hostNodeNames.size()));
            kengine.append("addNode {childName} : PJavaSENode");
            if (defineParentNode) {
                kengine.append("addChild {childName} @ {parentNodeName}");
            }
        }

        try {
            byte[] bytes = kengine.getScript().getBytes("UTF-8");
            File f = new File(storageModel);

            FileOutputStream outputStream = new FileOutputStream(f);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            System.out.println(kengine.getScript());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
