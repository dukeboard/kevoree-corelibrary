package org.kevoree.kompare.tests.model.builder.type;

import org.kevoree.DeployUnit;
import org.kevoree.DictionaryType;
import org.kevoree.KevoreeFactory;
import org.kevoree.TypeDefinition;
import org.kevoree.kompare.tests.model.builder.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/10/13
 * Time: 17:39
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class TypeDefinitionBuilder extends Builder<TypeDefinition> {

    protected TypeDefinition typeDefinition;

    private boolean isAbstract = false;
    private String bean;
    private DictionaryType dictionaryType;
    private String factoryBean;

    private List<DeployUnit> deployUnits = new ArrayList<DeployUnit>();

    protected TypeDefinitionBuilder(KevoreeFactory factory) {
        super(factory);
    }

    public TypeDefinitionBuilder setTypeDefinition(TypeDefinition typeDefinition) {
        this.typeDefinition = typeDefinition;
        return this;
    }

    public TypeDefinitionBuilder setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
        return this;
    }

    public TypeDefinitionBuilder setBean(String bean) {
        this.bean = bean;
        return this;
    }

    public TypeDefinitionBuilder setDictionaryType(DictionaryType dictionaryType) {
        this.dictionaryType = dictionaryType;
        return this;
    }

    public TypeDefinitionBuilder setFactoryBean(String factoryBean) {
        this.factoryBean = factoryBean;
        return this;
    }

    public TypeDefinitionBuilder addDeployUnit(DeployUnit deployUnit) {
        deployUnits.add(deployUnit);
        return this;
    }

    public TypeDefinitionBuilder removeDeployUnit(DeployUnit deployUnit) {
        deployUnits.remove(deployUnit);
        return this;
    }

    @Override
    protected TypeDefinition build() throws Exception {
        typeDefinition.setAbstract(isAbstract);

        if (bean != null) {
            typeDefinition.setBean(bean);
        } else {
            throw new Exception("Bean must be specified");
        }
        if (dictionaryType != null) {
            typeDefinition.setDictionaryType(dictionaryType);
        } else {
            throw new Exception("DictionaryType must be specified");
        }
        if (factoryBean != null) {
            typeDefinition.setFactoryBean(factoryBean);
        } else {
            throw new Exception("FactoryBean must be specified");
        }
        if (name != null) {
            typeDefinition.setName(name);
        } else {
            throw new Exception("Name must be specified");
        }
        typeDefinition.setDeployUnits(deployUnits);
        return typeDefinition;
    }
}
