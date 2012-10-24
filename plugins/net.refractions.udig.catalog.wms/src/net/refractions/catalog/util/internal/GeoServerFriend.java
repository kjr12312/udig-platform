/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2012, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.refractions.catalog.util.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.internal.wms.WMSGeoResourceImpl;
import net.refractions.udig.catalog.util.IFriend;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Used to handle URL recognisable as coming from geoserver.
 * 
 * @author Jody
 */
public class GeoServerFriend extends IFriend {
	public List<IResolve> friendly( IResolve handle, IProgressMonitor monitor ){
        if( !(handle instanceof IGeoResource) ) {
            return Collections.emptyList();
        }
        
		WMSGeoResourceImpl layer = (WMSGeoResourceImpl) handle;
		URL url = layer.getIdentifier();
		
		String uri = url.toString();
		String file = url.getFile();
		String host = url.getHost();
		int port = url.getPort();
		String ref = url.getRef();
		String protocol = url.getProtocol();
		
		if( !uri.contains("geoserver")) { //$NON-NLS-1$
			return Collections.emptyList();
		}
		String associate;
		associate = file.replace("wms", "wfs"); //$NON-NLS-1$ //$NON-NLS-2$
		int split = associate.indexOf('?');
		if( split != -1 ) associate = associate.substring(0,split+1);
		associate += "service=WFS&request=GetCapabilities&VERSION=1.0.0"; //$NON-NLS-1$
		associate += "#"; //$NON-NLS-1$
		associate += ref;
		
		URL target;
		try {
			target = new URL( protocol, host, port, associate);
		} catch (MalformedURLException e) {
			return Collections.emptyList();			
		}		
		ICatalog local = CatalogPlugin.getDefault().getLocalCatalog();
		
		// look up frendly wfs entry from local catalog (if present)
		IGeoResource friend = local.getById( IGeoResource.class, new ID(target), monitor );
		if( friend == null ) {
			return Collections.emptyList();
		}
		else {
			return new ArrayList<IResolve>( Collections.singletonList( friend ) );
		}		
	}
}
