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
 *  $RCSfile: IREMConstantBeanTypeProxy.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:22:23 $ 
 */


import org.eclipse.jem.internal.proxy.core.*;
import org.eclipse.jem.internal.proxy.common.remote.Commands;
/**
 * This interface is used for any BeanType that represents a constant type bean.
 * These are types that are immutable and a copy of the actual value is kept here
 * in the client and not stored as an object id.
 */

public interface IREMConstantBeanTypeProxy extends IBeanTypeProxy {
	
	/**
	 * Create a new bean proxy the ValueObject passed in.
	 */
	public IBeanProxy newBeanProxy(Commands.ValueObject value);
}