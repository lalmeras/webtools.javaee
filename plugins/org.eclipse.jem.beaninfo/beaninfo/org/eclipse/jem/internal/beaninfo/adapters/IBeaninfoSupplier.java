package org.eclipse.jem.internal.beaninfo.adapters;
/*******************************************************************************
 * Copyright (c)  2001, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 *  $RCSfile: IBeaninfoSupplier.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:17:59 $ 
 */


import org.eclipse.jem.internal.proxy.core.ProxyFactoryRegistry;
/**
 * Interface to supply information for beaninfo to decouple
 * the introspection from the desktop.
 */

public interface IBeaninfoSupplier {
	
	/**
	 * @return Return the registry to use. Initialize it if not already initialized.
	 */
	public ProxyFactoryRegistry getRegistry();
	
	/**
	 * Used to know if we currently have a registry created in the supplier.
	 * 
	 * @return true if there is a registry currently in the supplier.
	 */
	public boolean isRegistryCreated();
	
	/**
	 * Close the registry. This tells the registry to close. This is necessary
	 * at times because of changes to classes require the registry to be
	 * reconstructed.
	 */
	public void closeRegistry();

}