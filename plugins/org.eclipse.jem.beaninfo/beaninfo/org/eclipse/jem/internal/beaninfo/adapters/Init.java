package org.eclipse.jem.internal.beaninfo.adapters;
/*******************************************************************************
 * Copyright (c)  2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 *  $RCSfile: Init.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:17:59 $ 
 */
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.jem.internal.java.beaninfo.IIntrospectionAdapter;
import org.eclipse.jem.internal.proxy.core.ProxyFactoryRegistry;

/**
 * Static initializer class to initialize the beaninfo stuff.
 * It is disconnected from the desktop and so doesn't require it.
 */

public class Init {
	
	// So it can't be instantiated.
	private Init() {
	};

	/**
	 * Initialize the context with an IBeaninfoSupplier, used to set up the
	 * introspection process. 
	 *
	 * The beaninfo supplier is responsible for setting up the bean info search path,
	 * including removing the sun beaninfos, if desired.
	 */
	public static void initialize(ResourceSet rset, final IBeaninfoSupplier supplier) {
		rset.getAdapterFactories().add(new BeaninfoAdapterFactory(supplier));
	}

	/**
	 * Initialize the registry now that it is available.
	 */
	public static void initialize(ProxyFactoryRegistry registry) {
		// Remove the "sun.beans.info" from the beaninfo search path because
		// we completely override the sun bean infos.
		Utilities.removeBeanInfoPath(registry, "sun.beans.infos"); //$NON-NLS-1$
	}

	/**
	 * Cleanup from the context because we are being removed.
	 * If clearResults is true, then the factory should clear the results of introspection
	 * from the everything because the context will still be around.
	 */
	public static void cleanup(ResourceSet rset, boolean clearResults) {
		BeaninfoAdapterFactory factory =
			(BeaninfoAdapterFactory) EcoreUtil.getAdapterFactory(rset.getAdapterFactories(), IIntrospectionAdapter.ADAPTER_KEY);
		rset.getAdapterFactories().remove(factory);
		if (factory != null)
			factory.closeAll(clearResults);
	}

}