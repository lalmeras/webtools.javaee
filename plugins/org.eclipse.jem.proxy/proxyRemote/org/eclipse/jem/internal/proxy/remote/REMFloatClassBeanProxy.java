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
 *  $RCSfile: REMFloatClassBeanProxy.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:22:23 $ 
 */


import org.eclipse.jem.internal.proxy.core.*;
import org.eclipse.jem.internal.proxy.common.remote.Commands;
/**
 * Remote proxy implementation of INumberBeanProxy for Float.class
 */
class REMFloatClassBeanProxy extends REMNumberBeanProxy {

REMFloatClassBeanProxy(REMProxyFactoryRegistry aRegistry, Float aFloat) {
	super(aRegistry, aFloat);
}

/**
 * Get the beantype
 */
public IBeanTypeProxy getTypeProxy() {
	return ((REMStandardBeanTypeProxyFactory) fFactory.getBeanTypeProxyFactory()).floatClass;
}

/**
 * Render the bean into value object.
 */
public void renderBean(Commands.ValueObject value) {
	value.set((Float) numberValue());
}
}


