package org.eclipse.jem.internal.java.impl;
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
 *  $RCSfile: JavaFactoryImpl.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:12:30 $ 
 */
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.jem.internal.java.JavaDataType;
import org.eclipse.jem.internal.java.instantiation.IInstantiationHandler;
import org.eclipse.jem.internal.java.instantiation.IInstantiationHandlerFactoryAdapter;

/**
 * The factory to use JavaRef packages. It will use the
 * IInstantiationHandler, if there is one, to do the
 * factory requests. Else it will let the superclass handle it.
 */
public class JavaFactoryImpl extends EFactoryImpl {
	
	private IInstantiationHandler instantiationHandler;
	private boolean retrievedHandler;

	/**
	 * Constructor for JavaFactoryImpl.
	 */
	public JavaFactoryImpl() {
		super();
	}

	protected IInstantiationHandler getInstantiationHandler() {
		if (!retrievedHandler) {
			// Need to retrieve handler lazily because when factory is created it does not yet know what ResourceSet it is in.
			// Can't know that until the first time we need a handler.
			ResourceSet rset = getEPackage().eResource().getResourceSet();
			if (rset != null) {
				retrievedHandler = true;
				IInstantiationHandlerFactoryAdapter factory = (IInstantiationHandlerFactoryAdapter) EcoreUtil.getExistingAdapter(rset, IInstantiationHandlerFactoryAdapter.ADAPTER_KEY);
				if (factory != null)
					instantiationHandler = factory.getInstantiationHandler(this);
			}
		}
		return instantiationHandler;
	}
	
	/**
	 * @see org.eclipse.emf.ecore.EFactory#create(EClass)
	 */
	public EObject create(EClass eClass) {
		IInstantiationHandler ia = getInstantiationHandler();
		if (ia == null || !ia.handlesClass(eClass))
			return super.create(eClass);
		else
			return ia.create(eClass);
	}

	/**
	 * This is just a helper method to easily create JavaDataTypes. Since
	 * the initialization string is supposed to be immutable for JavaDataTypes,
	 * the actual init string feature is not exposed.
	 */
	public Object createFromString(JavaDataType eDataType, String literalValue) {
		IInstantiationHandler ia = getInstantiationHandler();
		if (ia == null || !ia.handlesDataType(eDataType))
			return null;
		else
			return ia.createFromString(eDataType, literalValue);
	}

}
