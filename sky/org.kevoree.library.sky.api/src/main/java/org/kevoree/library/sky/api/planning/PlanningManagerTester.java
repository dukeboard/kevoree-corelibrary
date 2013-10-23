package org.kevoree.library.sky.api.planning;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.MBinding;
import org.kevoree.NamedElement;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.defaultNodeTypes.planning.JavaSePrimitive;
import org.kevoree.library.sky.api.PJavaSENode;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;
import org.kevoreeadaptation.ParallelStep;

import java.util.HashMap;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/08/13
 * Time: 11:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class PlanningManagerTester {

    public static void main(String[] args) {
        PJavaSENode skyNode = new PJavaSENode();



        ContainerRoot model0 = KevoreeXmiHelper.instance$.load("/home/edaubert/emptyModel.kev");
        ContainerRoot model1 = KevoreeXmiHelper.instance$.load("/home/edaubert/firstModel.kev");

        PlanningManager planningManager = new PlanningManager(skyNode, new HashMap<String, Object>());
        AdaptationModel adaptationModel = planningManager.compareModels(model0, model1, "node0");

        adaptationModel = planningManager.plan(adaptationModel, "node0");

//    model0.findNodesByID("node0").getTypeDefinition()

//    printAdaptations(kompareModel);
        printStep(adaptationModel);
    }


    public static void printStep(AdaptationModel kompareModel) {
        printStep(kompareModel.getOrderedPrimitiveSet(), 0);
    }

    public static void  printStep(ParallelStep step, int index) {
        if (step != null && step.getAdaptations().size() > 0) {
            System.out.println("Step n° " + index);
            for (AdaptationPrimitive adaptation : step.getAdaptations()) {
                printAdaptation(adaptation);
            }
            printStep(step.getNextStep(), index+1);
        }
    }

    public static void  printAdaptations(AdaptationModel kompareModel) {
        for (AdaptationPrimitive adaptation : kompareModel.getAdaptations()) {
            printAdaptation(adaptation);
        }
    }

    public static void  printAdaptation (AdaptationPrimitive adaptation) {
        System.out.print(adaptation.getPrimitiveType().getName() + ": ");
        if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddBinding()) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveBinding())) {
            MBinding binding = (MBinding)adaptation.getRef();
            System.out.print(binding.getHub().getName() + "<->" + ((NamedElement)binding.getPort().eContainer()).getName() + "." + binding.getPort().getPortTypeRef().getName());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddInstance()) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveInstance())
                || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStartInstance()) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStopInstance())
                || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddType()) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveType())) {
            System.out.print(((NamedElement) adaptation.getRef()).getName());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddDeployUnit()) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveDeployUnit())) {
            System.out.print(((DeployUnit) adaptation.getRef()).getGroupName() + ":" + ((DeployUnit) adaptation.getRef()).getName() + ":" + ((DeployUnit) adaptation.getRef()).getVersion());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getUpdateDictionaryInstance())) {
            System.out.print(((NamedElement) adaptation.getRef()).getName());
        }
        System.out.println();
    }
}
