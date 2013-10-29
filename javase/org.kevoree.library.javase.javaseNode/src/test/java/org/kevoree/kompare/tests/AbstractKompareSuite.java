package org.kevoree.kompare.tests;

import org.junit.Assert;
import org.junit.Before;
import org.kevoree.*;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.defaultNodeTypes.planning.KevoreeKompareBean;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.util.ModelVisitor;
import org.kevoreeadaptation.AdaptationModel;
import org.kevoreeadaptation.AdaptationPrimitive;

import java.util.HashMap;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 16:59
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class AbstractKompareSuite {

    protected KevoreeKompareBean component = null;

    @Before
    public void initialize() {
        component = new KevoreeKompareBean(null);
    }

    public void populateRegistry(ContainerRoot model, String nodeName) {
        ContainerNode currentNode = model.findNodesByID(nodeName);
        final HashMap<String, Object> registry = new HashMap<String, Object>();
        currentNode.visit(new ModelVisitor() {
            public void visit(KMFContainer elem, String refNameInParent, KMFContainer parent) {
                registry.put(elem.path(), elem.path());
            }
        }, true, true, true);

//        component.setRegistry(registry);
    }

    /* UTILITY METHOD */
    public ContainerRoot model(String url) {
        if (this.getClass().getClassLoader().getResource(url) == null) {
            System.out.println("Warning File not found for test !!!");
        }
        return KevoreeXmiHelper.instance$.load(this.getClass().getClassLoader().getResource(url).getPath());
    }


    protected class RichAdaptationModel {

        private AdaptationModel self;

        public RichAdaptationModel(AdaptationModel self) {
            this.self = self;
        }

        public void verifySize(int size) {
            Assert.assertEquals("Size not equals found=" + self.getAdaptations().size() + ", must be =" + size, size, self.getAdaptations().size());
        }

        public void shouldContain(String c, String refName) {
            boolean found = false;
            for (AdaptationPrimitive adaptation : self.getAdaptations()) {
                if (adaptation.getRef() instanceof Instance && adaptation.getPrimitiveType().getName().contains(c) && ((Instance) adaptation.getRef()).getName().equals(refName)) {
                    found = true;
                    break;
                } else if (adaptation.getPrimitiveType().getName().contains(c) && ((TypeDefinition) adaptation.getRef()).getName().equals(refName)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(c + "should contains " + refName + " but it is not the case", found);
        }

        public void shouldContainSize(String c, int nb) {
            int found = 0;
            for (AdaptationPrimitive adaptation : self.getAdaptations()) {
                if (adaptation.getRef() instanceof Instance && adaptation.getPrimitiveType().getName().contains(c)) {
                    found++;
                }
            }
            Assert.assertEquals("There must be " + nb + " " + c + " elements but there is only " + found, nb, found);
        }


        public void shouldNotContain(String c) {
            boolean found = false;
            for (AdaptationPrimitive adaptation : self.getAdaptations()) {
                if (adaptation.getRef() instanceof Instance && adaptation.getPrimitiveType().getName().contains(c)) {
                    found = true;
                    break;
                }
            }
            Assert.assertFalse(" The adaptation model should not contains " + c + " but it is not the case", found);
        }

        public void print() {

            System.out.println("Adaptations " + self.getAdaptations().size());
            for (AdaptationPrimitive adapt : self.getAdaptations()) {
                System.out.println(adapt.getPrimitiveType().getName());
                if (adapt.getRef() instanceof DeployUnit) {
                    System.out.println("=>" + ((DeployUnit) adapt.getRef()).getName());
                } else if (adapt.getRef() instanceof TypeDefinition) {
                    System.out.println("=>" + ((TypeDefinition) adapt.getRef()).getName());
                } else if (adapt.getRef() instanceof Instance) {
                    System.out.println("=>" + ((Instance) adapt.getRef()).getName());
                } else if (adapt.getRef() instanceof MBinding) {
                    MBinding i = (MBinding) adapt.getRef();
                    System.out.println("=>" + i.getHub().getName() + "->" + i.getPort().getPortTypeRef().getName() + "-" + ((NamedElement) i.getPort().eContainer()).getName());
                } else {
                    System.out.println(">" + adapt.getRef().getClass().getName());
                }

            }
        }
    }


    protected class RichContainerRoot {

        private ContainerRoot self;

        public RichContainerRoot(ContainerRoot self) {
            this.self = self;
        }

        public ContainerRoot setLowerHashCode() {
            for (DeployUnit du : self.getDeployUnits()) {
                du.setHashcode(0 + "");
            }
            return self;
        }
    }
}
