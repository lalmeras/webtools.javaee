package org.eclipse.jem.internal.proxy.common;
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
 *  $RCSfile: CommandException.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:22:23 $ 
 */
/**
 * An error occurred during command processing.
 *
 */

public class CommandException extends Exception {
	protected final Object fExceptionData;
	
	public CommandException() {
		fExceptionData = null;
	}
	
	public CommandException(Object data) {
		fExceptionData = data;
	}
	
	public CommandException(String msg, Object data) {
		super(msg);
		fExceptionData = data;
	}	
	
	public Object getExceptionData() {
		return fExceptionData;
	}
	
	public boolean isRecoverable() {
		return false;	// By default Command Exceptions are not recoverable.
	}	
}