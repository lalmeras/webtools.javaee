package org.eclipse.jem.internal.proxy.remote;
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
 *  $RCSfile: REMBeanProxy.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:22:23 $ 
 */


import org.eclipse.jem.internal.proxy.core.*;
/**
 * Standard implementation of IREMBeanProxy
 */

public class REMBeanProxy extends REMAbstractBeanProxy {
	private IBeanTypeProxy fType;

	protected REMBeanProxy(REMProxyFactoryRegistry aRegistry, Integer anID, IBeanTypeProxy aType){
		super(aRegistry, anID);
		fType = aType;
	}
	
	public IBeanTypeProxy getTypeProxy() {
		return fType;
	}
	
	public void release() {
		fType = null;
		super.release();
	}
}