package org.eclipse.jem.internal.proxy.common.remote;
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
 *  $RCSfile: UnexpectedCommandException.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:22:23 $ 
 */

import org.eclipse.jem.internal.proxy.common.CommandException;
/**
 * This is an exception that is thrown when the processing
 * of data was invalid for the command. 
 * There will be a flag indicting error type, and a flag
 * indicating whether the connection is recoverable or not.
 * If not recoverable, then the connection needs to be closed.
 *
 */
public class UnexpectedCommandException extends CommandException {
	private final Object fErrorType;
	private final boolean fRecoverable;
	
	public UnexpectedCommandException(Object errorType, boolean recoverable) {
		fErrorType = errorType;
		fRecoverable = recoverable;
	}		
	
	public UnexpectedCommandException(Object errorType, boolean recoverable, Object data) {
		super(data);
		fErrorType = errorType;
		fRecoverable = recoverable;
	}
	
	public UnexpectedCommandException(Object errorType, boolean recoverable, String msg, Object data) {
		super(msg, data);
		fErrorType = errorType;
		fRecoverable = recoverable;
	}

	public Object getErrorType() {
		return fErrorType;
	}
	
	public boolean isRecoverable() {
		return fRecoverable;
	}
	
	public String getMessage() {
		String m = super.getMessage();
		if (fErrorType != null) {
			String dataMsg = getExceptionData() != null ? " Errordata="+getExceptionData().toString() : "";	//$NON-NLS-1$ //$NON-NLS-2$
			if (m == null)
				return "Errortype="+fErrorType.toString() + dataMsg; //$NON-NLS-1$
			else
				return m + ':' + "Errortype="+fErrorType.toString() + dataMsg; //$NON-NLS-1$
		} else
			return m;
	}
}