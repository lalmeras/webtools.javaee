package org.eclipse.jst.javaee.core;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;

public class JEESAXXMLHandler extends SAXXMIHandler {

	public JEESAXXMLHandler(XMLResource xmiResource, XMLHelper helper,
			Map<?, ?> options) {
		super(xmiResource, helper, options);
		// TODO Auto-generated constructor stub
	}

	protected EPackage getPackageForURI(String uriString) {
		// Grab the schema location because all JEE DD files share a common namespace
		URI uri = urisToLocations.get(uriString);
		String locString = (uri == null) ? uriString : uri.toString();
		EPackage ePackage = packageRegistry.getEPackage(locString);
		if (ePackage == null)
			return super.getPackageForURI(locString);
		else return ePackage;
		
	}

}
