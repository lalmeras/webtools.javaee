/***************************************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 **************************************************************************************************/
package org.eclipse.jst.j2ee.ejb.annotations.internal.xdoclet;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jst.common.launcher.ant.AntLauncher;
import org.eclipse.jst.j2ee.ejb.annotations.xdoclet.Logger;

public class XDocletBuilder extends IncrementalProjectBuilder implements
		IExecutableExtension {

	private static final boolean performValidateEdit = false;

	private static boolean isGloballyEnabled = true;

	private static final String OFF = "off"; //$NON-NLS-1$

	protected static class ProjectChangeListener implements
			IResourceChangeListener, IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (delta.getResource() != null) {
				int resourceType = delta.getResource().getType();
				if (resourceType == IResource.PROJECT
						|| resourceType == IResource.ROOT) {
					try {
						delta.accept(this);
					} catch (CoreException e) {
						Logger.logException(
								"Exception managing buildspec list", e); //$NON-NLS-1$
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource != null) {
				if (resource.getType() == IResource.ROOT)
					return true;
				else if (resource.getType() == IResource.PROJECT) {
					if (delta.getKind() == IResourceDelta.ADDED) {
						add(new NullProgressMonitor(), (IProject) resource,
								null);
					}
					return false;
				}
			}
			return false;
		}
	}

	/**
	 * Add the StructuredBuilder to the build spec of a single IProject
	 * 
	 * @param project -
	 *            the IProject to add to, when needed
	 */
	public static void add(IProgressMonitor monitor, IProject project,
			Object validateEditContext) {
		if (project == null || !project.isAccessible()) {
			return;
		}
		boolean isBuilderPresent = false;
		try {
			IFile descriptionFile = project
					.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
			if (descriptionFile.exists() && descriptionFile.isAccessible()) {
				IProjectDescription description = project.getDescription();
				ICommand[] commands = description.getBuildSpec();
				if (commands != null) {
					for (int i = 0; i < commands.length; i++) {
						String builderName = commands[i].getBuilderName();
						// builder name will be null if it has not been set
						if (builderName != null
								&& builderName.equals(getBuilderId())) {
							isBuilderPresent = true;
							break;
						}
					}
				}
				if (!isBuilderPresent && !monitor.isCanceled()) {
					// validate for edit
					IStatus status = null;
					if (performValidateEdit) {
						ISchedulingRule validateEditRule = null;
						try {

							IFile[] validateFiles = new IFile[] { descriptionFile };
							IWorkspace workspace = descriptionFile
									.getWorkspace();
							validateEditRule = workspace.getRuleFactory()
									.validateEditRule(validateFiles);
							Platform.getJobManager().beginRule(
									validateEditRule, monitor);
							status = workspace
									.validateEdit(validateFiles, null);
						} finally {
							if (validateEditRule != null) {
								Platform.getJobManager().endRule(
										validateEditRule);
							}
						}
					}
					if (status == null || status.isOK()) {
						// add the builder
						ICommand newCommand = description.newCommand();
						newCommand.setBuilderName(getBuilderId());
						ICommand[] newCommands = null;
						if (commands != null) {
							newCommands = new ICommand[commands.length + 1];
							System.arraycopy(commands, 0, newCommands, 0,
									commands.length);
							newCommands[commands.length] = newCommand;
						} else {
							newCommands = new ICommand[1];
							newCommands[0] = newCommand;
						}
						description.setBuildSpec(newCommands);
						/*
						 * This 'refresh' was added since unit tests were
						 * throwing exceptions about being out of sync. That may
						 * indicate a "deeper" problem such as needing to use
						 * scheduling rules, (although there don't appear to be
						 * examples of that) or something similar.
						 */
						// project.refreshLocal(IResource.DEPTH_ZERO,
						// subMonitorFor(monitor, 1,
						// IProgressMonitor.UNKNOWN));
						try {
							project.setDescription(description, monitor);
						} catch (CoreException e) {
							if (performValidateEdit) {
								Logger
										.log(
												Logger.WARNING,
												"Description for project \"" + project.getName() + "\" could not be updated despite successful build"); //$NON-NLS-2$//$NON-NLS-1$					
							} else {
								Logger
										.log(
												Logger.WARNING,
												"Description for project \"" + project.getName() + "\" could not be updated"); //$NON-NLS-2$//$NON-NLS-1$					
							}
						}
					}
				}
			} else {
				Logger
						.log(
								Logger.WARNING,
								"Description for project \"" + project.getName() + "\" could not be updated"); //$NON-NLS-2$//$NON-NLS-1$
			}
		} catch (Exception e) {
			// if we can't read the information, the project isn't open,
			// so it can't run auto-validate
			Logger
					.logException(
							"Exception caught when adding Model Builder", e); //$NON-NLS-1$
		}
	}

	/**
	 * Adds the Builder to every project in the Workspace
	 * 
	 * @param root
	 */
	public synchronized static void add(IProgressMonitor monitor,
			IWorkspaceRoot root, Object validateEditContext) {
		if (!isGloballyEnabled) {
			return;
		}
		IProject[] allProjects = root.getProjects();
		IProgressMonitor localMonitor = monitor;
		localMonitor.beginTask(
				"Starting to add builder to projects with EJB modules", 1); //$NON-NLS-1$
		for (int i = 0; i < allProjects.length && !monitor.isCanceled(); i++) {
			add(localMonitor, allProjects[i], validateEditContext);
			localMonitor.worked(1);
		}
		localMonitor.done();
	}

	private static String getBuilderId() {
		return "org.eclipse.jst.j2ee.ejb.annotations.xdocletbuilder"; //$NON-NLS-1$
	}

	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}

	protected List fActiveDelegates = null;

	private String fName = "XDoclet Builder"; //$NON-NLS-1$

	private long time0;

	private IResourceChangeListener changeListener;

	/**
	 * 
	 */
	public XDocletBuilder() {
		super();
		if (isGloballyEnabled) {
			fActiveDelegates = new ArrayList();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		IProject currentProject = getProject();
		// Currently, just use the Task Tags preference
		boolean locallyEnabled = isGloballyEnabled;
		if (!locallyEnabled || currentProject == null
				|| !currentProject.isAccessible()) {
			return new IProject[] { currentProject };
		}

		IResourceDelta delta = getDelta(currentProject);
		IProgressMonitor localMonitor = monitor;
		localMonitor.beginTask(getDisplayName(), 1);

		if (!localMonitor.isCanceled()) {
			// check the kind of delta if one was given
			if (kind == FULL_BUILD || kind == CLEAN_BUILD || delta == null) {
				doFullBuild(kind, args, localMonitor, getProject());
			} else {
				doIncrementalBuild(kind, args, localMonitor);
			}
		}
		localMonitor.worked(1);
		localMonitor.done();
		return new IProject[] { getProject() };
	}

	void build(int kind, Map args, IResource resource, IContentType[] types,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && resource.getType() == IResource.FILE) {
			URL url = Platform.getBundle("org.eclipse.jst.j2ee.ejb.annotations.xdoclet").getEntry(
					"/templates/builder/xdoclet.xml");
			IJavaProject javaProject = JavaCore.create(resource.getProject());
			ICompilationUnit compilationUnit = JavaCore
					.createCompilationUnitFrom((IFile) resource);
			try {

				Properties properties = new Properties();
				properties.put("ejb", resource.getProject().getName());
				properties.put("ejb.project.dir", resource.getProject()
						.getLocation().toString());
				properties.put("ejb.module.src", this.getPackageFragmentRoot(compilationUnit).getResource().getProjectRelativePath().toString());
				properties.put("ejb.module.gen", this.getPackageFragmentRoot(compilationUnit).getResource().getProjectRelativePath().toString());
				properties.put("ejb.bin.dir", this.getJavaProjectOutputContainer(javaProject).toString());
				properties.put("xdoclet.home", "C:/downloads/xdoclet-1.2.1");
				properties
						.put("ant.home",
								"C:/nmd/dev/java/ep31m4/eclipse/plugins/org.apache.ant_1.6.2");
				properties.put("ejb.spec.version", "2.0");
				properties.put("java.class.path", "");
				properties.put("project.class.path", "");
				properties.put("project.path", "");
				properties.put("ejb.bean.path", resource
						.getProjectRelativePath().toString());

				AntLauncher antLauncher = new AntLauncher(url, resource
						.getParent().getLocation(), properties, new HashMap());
				antLauncher.launch("ejbdoclet", monitor);
			} catch (Exception e) {
			}

		}
	}

	private  PackageFragmentRoot getPackageFragmentRoot(ICompilationUnit res) {
		IJavaElement current = res;
		do {
			if (current instanceof PackageFragmentRoot) return (PackageFragmentRoot)current;
			current = current.getParent();
		} while(current != null);
		return null;
	}
	private  IPath getJavaProjectOutputContainer(IJavaProject proj) throws JavaModelException {
		IPath path = proj.getOutputLocation();
		if (path == null)
			return null;
		if (path.segmentCount() == 1)
			return path;
		return ((IContainer)proj.getProject()).getFolder(path.removeFirstSegments(1)).getProjectRelativePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		IProject currentProject = getProject();
		if (!isGloballyEnabled || currentProject == null
				|| !currentProject.isAccessible()) {
			return;
		}
		doFullBuild(IncrementalProjectBuilder.CLEAN_BUILD, new HashMap(0),
				monitor, getProject());
	}

	boolean isXDocletAnnotatedResource(IResource resource) {
		IContentType[] types = null;
		if (resource.getType() == IResource.FILE && resource.isAccessible()) {
			IContentDescription d = null;
			try {
				// optimized description lookup, might not succeed
				d = ((IFile) resource).getContentDescription();
				if (d != null
						&& "org.eclipse.jdt.core.javaSource".equals(d
								.getContentType().getId())) {
					String contents = JavaCore.createCompilationUnitFrom(
							(IFile) resource).getSource();
					// System.out.println(contents);
					return contents.indexOf("@ejb.bean") >= 0;
				}
			} catch (CoreException e) {
				// should not be possible given the accessible and file type
				// check above
			}
			if (types == null) {
				types = Platform.getContentTypeManager().findContentTypesFor(
						resource.getName());
				for (int i = 0; i < types.length; i++) {
					IContentType type = types[i];
					if ("org.eclipse.jdt.core.javaSource".equals(type.getId())) {
						String contents = "";
						try {
							contents = JavaCore.createCompilationUnitFrom(
									(IFile) resource).getSource();
						} catch (JavaModelException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return contents.indexOf("@ejb.bean") >= 0;
					}
				}
			}
			return false;
		}
		return false;
	}

	/**
	 * Iterate through the list of resources and build each one
	 * 
	 * @param monitor
	 * @param resources
	 */
	protected void doFullBuild(int kind, Map args, IProgressMonitor monitor,
			IProject project) {

		final IProgressMonitor subMonitor = monitor;
		final int localKind = kind;
		final Map localArgs = args;

		final IProgressMonitor visitorMonitor = monitor;
		IResourceVisitor internalBuilder = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.getType() == IResource.FILE) {
					// for any supported file type, record the resource
					if (isXDocletAnnotatedResource(resource)) {
						build(localKind, localArgs, resource, null, subMonitor);
						visitorMonitor.worked(1);
					}
					return false;
				} else {
					return true;
				}
			}

		};
		try {
			project.accept(internalBuilder);
		} catch (CoreException e) {
			Logger.logException(e);
		}
	}

	/**
	 * 
	 */
	protected void doIncrementalBuild(int kind, Map args,
			IProgressMonitor monitor) {
		IResourceDelta projectDelta = getDelta(getProject());
		if (projectDelta == null) {
			throw new IllegalArgumentException(
					"delta is null, should do a full build"); //$NON-NLS-1$
		}

		final Map localArgs = args;
		final int localKind = kind;
		final IProgressMonitor localMonitor = monitor;
		IResourceDeltaVisitor participantVisitor = new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) throws CoreException {
				if (!localMonitor.isCanceled()
						&& delta.getResource().getType() == IResource.FILE) {
					if (isXDocletAnnotatedResource(delta.getResource()))
						build(localKind, localArgs, delta.getResource(), null,
								localMonitor);
				}
				return delta.getAffectedChildren().length > 0;
			}
		};
		try {
			projectDelta.accept(participantVisitor);
		} catch (CoreException e) {
			Logger.logException(e);
		}
		monitor.worked(1);
	}

	private String getDisplayName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		if (config != null) {
			fName = config.getDeclaringExtension().getLabel();
		}
	}

	public static void shutdown() {
	}

	public static void startup() {
	}
}
