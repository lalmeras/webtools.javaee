/*******************************************************************************
 * Copyright (c) 2009 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.j2ee.internal.common.exportmodel;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.common.internal.modulecore.ISingleRootStatus;
import org.eclipse.jst.common.internal.modulecore.ReplaceManifestExportParticipant;
import org.eclipse.jst.common.internal.modulecore.SingleRootUtil;
import org.eclipse.jst.common.internal.modulecore.SingleRootExportParticipant.SingleRootParticipantCallback;
import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants.DependencyAttributeType;
import org.eclipse.jst.j2ee.internal.J2EEConstants;
import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyEnablement;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.wst.common.componentcore.internal.ComponentResource;
import org.eclipse.wst.common.componentcore.internal.flat.FilterResourceParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

public class JavaEESingleRootCallback implements SingleRootParticipantCallback {
	public static final int EAR_PROJECT_FOUND = 10100;
	public static final int ATLEAST_1_RESOURCE_MAP_MISSING = 10101;
	public static final int JAVA_OUTPUT_NOT_WEBINF_CLASSES = 10102;
	public static final int RUNTIME_PATH_NOT_ROOT_OR_WEBINF_CLASSES = 10103;
	public static final int ONLY_1_CONTENT_ROOT_ALLOWED = 10104;
	public static final int ONE_CONTENT_ROOT_REQUIRED = 10105;
	public static final int ATLEAST_1_JAVA_SOURCE_REQUIRED = 10106;
	public static final int CLASSPATH_DEPENDENCIES_FOUND = 10107;
	
	private static final int CANCEL = 0x0;
	private String[] filteredSuffixes = new String[]{}; 
	public JavaEESingleRootCallback() {
		// intentionally blank
	}
	
	public JavaEESingleRootCallback(String[] filtered) {
		this.filteredSuffixes = filtered;
	}
	
	public void setFilteredSuffixes(String[] filtered) {
		this.filteredSuffixes = filtered;
	}
	
	public boolean canValidate(IVirtualComponent vc) {
		return JavaEEProjectUtilities.usesJavaEEComponent(vc);
	}

	public void validate(SingleRootUtil util, IVirtualComponent vc, IProject project, List resourceMaps) {
		// Always return false for EARs so that members for EAR are always calculated and j2ee modules are filtered out
		if (JavaEEProjectUtilities.isEARProject(project)) { 
			util.reportStatus(EAR_PROJECT_FOUND);
			util.setValidateFlag(CANCEL);
			return;
		}
		
		if (resourceMaps.size() == 1) {
			ComponentResource mapping = (ComponentResource)resourceMaps.get(0); 
			if (util.isRootMapping(mapping)) {
				IResource sourceResource = project.findMember(mapping.getSourcePath());
				if (sourceResource != null && sourceResource.exists()) {
					if (sourceResource instanceof IContainer && !util.isSourceContainer((IContainer) sourceResource)) {
						util.reportStatus(ISingleRootStatus.SINGLE_ROOT_CONTAINER_FOUND, (IContainer) sourceResource);
						util.setValidateFlag(CANCEL);
						return;
					}
				}
			}
		}
		
		// If we have classpath dependencies we can't be single root
		if (hasClasspathDependencies(vc)) {
			util.reportStatus(CLASSPATH_DEPENDENCIES_FOUND);
			if (util.getValidateFlag() == CANCEL) 
				return;
		}
		
		//validate web projects for single root
		if (JavaEEProjectUtilities.isDynamicWebProject(project)) {
			validateWebProject(util, vc, resourceMaps);
			util.setValidateFlag(CANCEL);
		}

	}
	
	protected boolean hasClasspathDependencies(IVirtualComponent component) {
		try {
			final boolean isWebApp = JavaEEProjectUtilities.isDynamicWebComponent(component);
		    boolean webLibsOnly = false;
		    if (!ClasspathDependencyEnablement.isAllowClasspathComponentDependency() && isWebApp) {
		    	webLibsOnly = true;
			}
			final Map entriesToAttrib = ClasspathDependencyUtil.getRawComponentClasspathDependencies(
					JavaCoreLite.create(component.getProject()), 
					DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY, 
					webLibsOnly);
			return entriesToAttrib != null && entriesToAttrib.size() > 0;
		} catch( CoreException ce ) {}
		return false;
	}

	
	private void validateWebProject(SingleRootUtil util, IVirtualComponent vc, List resourceMaps) {
		// Ensure there are only basic component resource mappings -- one for the content folder 
		// and any for src folders mapped to WEB-INF/classes
		if (hasDefaultWebResourceMappings(util, resourceMaps)) {
			IContainer[] javaOutputFolders = util.getJavaOutputFolders();
			// Verify only one java output folder
			if (javaOutputFolders.length == 1) {
				// Verify the java output folder is to <content root>/WEB-INF/classes
				IPath javaOutputPath = util.getJavaOutputFolders()[0].getProjectRelativePath();
				IContainer rootContainer = vc.getRootFolder().getUnderlyingFolder();
				IPath compRootPath = rootContainer.getProjectRelativePath();
				if (compRootPath.append(J2EEConstants.WEB_INF_CLASSES).equals(javaOutputPath)) {
					util.reportStatus(ISingleRootStatus.SINGLE_ROOT_CONTAINER_FOUND, rootContainer);
					return;
				}
				util.reportStatus(JAVA_OUTPUT_NOT_WEBINF_CLASSES);
			}
			else {
				util.reportStatus(ISingleRootStatus.JAVA_OUTPUT_GREATER_THAN_1);
			}
		}
	}
	
	/**
	 * Ensure the default web setup is correct with one resource map and any number of java 
	 * resource maps to WEB-INF/classes
	 * 
	 * @param resourceMaps
	 * @return boolean
	 */
	private boolean hasDefaultWebResourceMappings(SingleRootUtil util, List resourceMaps) {
		int rootValidMaps = 0;
		
		IPath webInfClasses = new Path(J2EEConstants.WEB_INF_CLASSES).makeAbsolute();
		for (int i = 0; i < resourceMaps.size(); i++) {
			ComponentResource resourceMap = (ComponentResource) resourceMaps.get(i);
			IPath sourcePath = resourceMap.getSourcePath();
			IPath runtimePath = resourceMap.getRuntimePath();
			IResource sourceResource = util.getProject().findMember(sourcePath);
			
			// Verify if the map is for the content root
			if (util.isRootMapping(resourceMap)) {
				rootValidMaps++;
			} 
			// Verify if the map is for a java src folder and is mapped to "WEB-INF/classes"
			else if (runtimePath.equals(webInfClasses)) {
				if (sourceResource != null && sourceResource.exists()) {
					if (sourceResource instanceof IContainer && !util.isSourceContainer((IContainer) sourceResource)) {
						util.reportStatus(ISingleRootStatus.SOURCE_NOT_JAVA_CONTAINER, sourcePath);
					}
				}
				else {
					util.reportStatus(ISingleRootStatus.SOURCE_PATH_NOT_FOUND, sourcePath);
				}
			}
			else {
				util.reportStatus(RUNTIME_PATH_NOT_ROOT_OR_WEBINF_CLASSES, runtimePath);
			}
			
			if (util.getValidateFlag() == CANCEL) return false;
		}
		// Make sure only one of the maps is the content root, and that at least one is for the java folder
		if (rootValidMaps != 1) {
			if (rootValidMaps < 1) {
				util.reportStatus(ONE_CONTENT_ROOT_REQUIRED);
			}
			else if (rootValidMaps > 1) {
				util.reportStatus(ONLY_1_CONTENT_ROOT_ALLOWED);
			}
		}
		return util.getValidateFlag() == CANCEL ? false : true;
	}

	public IFlattenParticipant[] getDelegateParticipants() {
		return new IFlattenParticipant[] {
				new ReplaceManifestExportParticipant(new Path(J2EEConstants.MANIFEST_URI)),
				new JEEHeirarchyExportParticipant(),
				FilterResourceParticipant.createSuffixFilterParticipant(filteredSuffixes)
		};
	}
	

}
