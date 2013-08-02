package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sky.helper.KloudModelHelper;
import org.kevoree.library.sky.provider.api.PaaSSlaveService;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/11/12
 * Time: 15:42
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@Requires({
		@RequiredPort(name = "slave", type = PortType.SERVICE, className = PaaSSlaveService.class)
})
@ComponentType
public class PaaSManagerSlave extends AbstractComponentType implements ModelListener {

	private boolean bootstrapping = true;

	@Start
	public void start () throws Exception {

	}

	@Stop
	public void stop () {

	}

	@Override
	public boolean preUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		return true;
	}

	@Override
	public boolean initUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public boolean afterLocalUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public void modelUpdated () {
		if (bootstrapping) {
			if (!KloudModelHelper.isPaaSModel(getModelService().getLastModel())) {
				try {
					ContainerRoot model = getPortByName("slave", PaaSSlaveService.class).getModel();
					if (KloudModelHelper.isPaaSModel(model)) {
						getModelService().atomicUpdateModel(model);
					}
					bootstrapping = false;
				} catch (SubmissionException e) {
                    Log.warn("The request to get the PaaS model from the master fails", e);
				}
			}
		}
	}

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}
}
