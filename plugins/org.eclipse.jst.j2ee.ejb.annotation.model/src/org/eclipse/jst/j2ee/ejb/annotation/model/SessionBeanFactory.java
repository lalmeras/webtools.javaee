/***************************************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 **************************************************************************************************/

package org.eclipse.jst.j2ee.ejb.annotation.model;

import org.eclipse.jst.j2ee.ejb.Session;
import org.eclipse.jst.j2ee.ejb.annotation.internal.model.SessionBeanDelegate;
import org.eclipse.jst.j2ee.ejb.annotations.ISessionBeanDelegate;


public class SessionBeanFactory {

	 public static ISessionBeanDelegate getDelegate(Session  session, SessionBeanDataModel beanDataModel)
	 {
	 	SessionBeanDelegate beanDelegate = new SessionBeanDelegate();
	 	beanDelegate.setEjb(session);
	 	beanDelegate.setEnterpriseBeanDataModel(beanDataModel);
	 	
	 	return beanDelegate;
	 }
}
