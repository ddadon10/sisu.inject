/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.osgi;

import javax.inject.Singleton;

import org.eclipse.sisu.inject.BindingSubscriber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;

/**
 * Service {@link Binding} backed by an OSGi {@link ServiceReference}.
 */
final class ServiceBinding<T>
    implements Binding<T>, Provider<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<T> clazz;

    private final Key<T> key;

    private final T instance;

    private final int rank;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    ServiceBinding( final BundleContext context, final String clazzName, final int maxRank,
                    final ServiceReference<T> reference )
        throws ClassNotFoundException
    {
        clazz = (Class<T>) reference.getBundle().loadClass( clazzName );

        final Object name = reference.getProperty( "name" );
        if ( name instanceof String && ( (String) name ).length() > 0 )
        {
            key = Key.get( clazz, Names.named( (String) name ) );
        }
        else
        {
            key = Key.get( clazz );
        }

        instance = context.getService( reference );

        // limit the exposed rank to the given maximum
        final int serviceRank = ( (Number) reference.getProperty( Constants.SERVICE_RANKING ) ).intValue();
        rank = serviceRank < maxRank ? serviceRank : maxRank;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public T get()
    {
        return instance;
    }

    public Key<T> getKey()
    {
        return key;
    }

    public Provider<T> getProvider()
    {
        return this;
    }

    public Object getSource()
    {
        return null;
    }

    public void applyTo( final Binder binder )
    {
        // no-op
    }

    public <V> V acceptVisitor( final ElementVisitor<V> visitor )
    {
        return visitor.visit( this );
    }

    public <V> V acceptTargetVisitor( final BindingTargetVisitor<? super T, V> visitor )
    {
        return null;
    }

    public <V> V acceptScopingVisitor( final BindingScopingVisitor<V> visitor )
    {
        return visitor.visitScopeAnnotation( Singleton.class );
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    boolean isCompatibleWith( final BindingSubscriber<T> subscriber )
    {
        return clazz.equals( subscriber.type().getRawType() );
    }

    int rank()
    {
        return rank;
    }
}
