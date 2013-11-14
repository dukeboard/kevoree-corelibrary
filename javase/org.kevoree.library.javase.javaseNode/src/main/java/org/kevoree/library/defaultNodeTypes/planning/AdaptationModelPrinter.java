package org.kevoree.library.defaultNodeTypes.planning;

import org.kevoree.ComponentInstance;
import org.kevoree.DeployUnit;
import org.kevoree.MBinding;
import org.kevoree.NamedElement;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;
import org.kevoreeadaptation.ParallelStep;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/13
 * Time: 16:55
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class AdaptationModelPrinter {

    public void printStep(AdaptationModel kompareModel) {
        printStep(kompareModel.getOrderedPrimitiveSet(), 0);
    }

    public void printStep(ParallelStep step, int index) {
        if (step != null && step.getAdaptations().size() > 0) {
            System.out.println("Step n° " + index);
            for (AdaptationPrimitive adaptation : step.getAdaptations()) {
                printAdaptation(adaptation);
            }
            printStep(step.getNextStep(), index + 1);
        }
    }

    public void printAdaptations(AdaptationModel kompareModel) {
        for (AdaptationPrimitive adaptation : kompareModel.getAdaptations()) {
            printAdaptation(adaptation);
        }
    }

    public void printAdaptation(AdaptationPrimitive adaptation) {
        System.out.print(adaptation.getPrimitiveType().getName() + ": ");
        if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.AddBinding) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveBinding)) {
            MBinding binding = (MBinding) adaptation.getRef();
            System.out.print(binding.getHub().getName() + "<->" + ((ComponentInstance) binding.getPort().eContainer()).getName() + "." + binding.getPort().getPortTypeRef().getName());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.AddFragmentBinding) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveFragmentBinding)) {
            System.out.print(((NamedElement)adaptation.getRef()).getName() + "@" + adaptation.getTargetNodeName());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.AddInstance) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveInstance)
                || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.StartInstance) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.StopInstance)
                || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.AddType) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveType)) {
            System.out.print(((NamedElement) adaptation.getRef()).getName());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.AddDeployUnit) || adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.RemoveDeployUnit)) {
            System.out.print(((DeployUnit) adaptation.getRef()).getGroupName() + ":" + ((DeployUnit) adaptation.getRef()).getName() + ":" + ((DeployUnit) adaptation.getRef()).getVersion() + "-" + ((DeployUnit) adaptation.getRef()).getHashcode());
        } else if (adaptation.getPrimitiveType().getName().equals(JavaSePrimitive.UpdateDictionaryInstance)) {
            System.out.print(((NamedElement) adaptation.getRef()).getName());
        } else {
            if (adaptation.getRef() instanceof NamedElement) {
                System.out.print(((NamedElement) adaptation.getRef()).getName());
            }
        }
        System.out.println();
    }
}
