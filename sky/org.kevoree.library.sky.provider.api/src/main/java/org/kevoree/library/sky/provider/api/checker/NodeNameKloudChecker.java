package org.kevoree.library.sky.provider.api.checker;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.Instance;
import org.kevoree.api.service.core.checker.CheckerViolation;
import org.kevoree.library.sky.helper.KloudModelHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 13:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public class NodeNameKloudChecker extends  KloudCheckerService {

  public List<CheckerViolation> check (ContainerRoot model){
      List<CheckerViolation> violations = new ArrayList<CheckerViolation>();
      for (Group group : model.getGroups()) {
          if (group.getName().equals(KloudModelHelper.getPaaSKloudGroup(model))) {
              CheckerViolation violation = violate(group);
              if (violation != null){
                  violations.add(violation);
              }
          }
      }
      for (ContainerNode node : model.getNodes()) {
          if (KloudModelHelper.isPaaSNode(model, node)) {
              CheckerViolation violation = violate(node);
              if (violation != null){
               violations.add(violation);
              }
          }
      }
    return violations;
  }

    private CheckerViolation violate(Instance instance) {
        if (!instance.getName().startsWith(getId())) {
            CheckerViolation concreteViolation  = new CheckerViolation();
            concreteViolation.setMessage(instance.getName() + " is not a valid name. It must start with your login.");
            List<Object> targets = new ArrayList<Object>();
            targets.add(instance);
            concreteViolation.setTargetObjects(targets);
            return concreteViolation;
        }
        return null;
    }
}
