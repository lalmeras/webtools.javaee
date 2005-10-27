package org.eclipse.jst.j2ee.application.internal.operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsDataModelProvider;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;

public class AddComponentToEnterpriseApplicationDataModelProvider extends CreateReferenceComponentsDataModelProvider implements IAddComponentToEnterpriseApplicationDataModelProperties {

	public AddComponentToEnterpriseApplicationDataModelProvider() {
		super();
	}

	public Set getPropertyNames() {
		Set propertyNames = super.getPropertyNames();
		propertyNames.add(TARGET_COMPONENTS_TO_URI_MAP);
		return propertyNames;
	}

	public IDataModelOperation getDefaultOperation() {
		return new AddComponentToEnterpriseApplicationOp(model);
	}

	public Object getDefaultProperty(String propertyName) {
		if (TARGET_COMPONENTS_TO_URI_MAP.equals(propertyName)) {
			Map map = new HashMap();
			List components = (List) getProperty(TARGET_COMPONENT_LIST);
			for (int i = 0; i < components.size(); i++) {
				IVirtualComponent component = (IVirtualComponent) components.get(i);
				IProject project = component.getProject();
				String name = component.getName();

				if (J2EEProjectUtilities.isDynamicWebProject(project)) {
					name += ".war"; //$NON-NLS-1$
				} else if (J2EEProjectUtilities.isEJBProject(project)) {
					name += ".jar"; //$NON-NLS-1$
				} else if (J2EEProjectUtilities.isApplicationClientProject(project)) {
					name += ".jar"; //$NON-NLS-1$
				} else if (J2EEProjectUtilities.isJCAProject(project)) {
					name += ".rar"; //$NON-NLS-1$
				}
				map.put(component, name);
			}
			return map;
		}
		return super.getDefaultProperty(propertyName);
	}

}
