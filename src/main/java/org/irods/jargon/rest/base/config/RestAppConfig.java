package org.irods.jargon.rest.base.config;
/**
 * 
 */

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.pool.conncache.CachedIrodsProtocolManager;
import org.irods.jargon.pool.conncache.JargonConnectionCache;
import org.irods.jargon.pool.conncache.JargonKeyedPoolConfig;
import org.irods.jargon.pool.conncache.JargonPooledObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Base bean definitions and iRODS/Jargon components
 * @author conwaymc
 *
 */
@Configuration
public class RestAppConfig {
	
	@Autowired
	private BaseRestConfig baseRestConfig;
	
	@Bean
	public IRODSSession irodsSession(JargonProperties jargonProperties) {
		IRODSSession irodsSession = new IRODSSession();
		irodsSession.setJargonProperties(jargonProperties);
		return irodsSession;
	}
	
	@Bean
	public JargonProperties jargonProperties(BaseRestConfig baseRestConfig) throws JargonException {
		SettableJargonProperties jargonProperties = new SettableJargonProperties();
		jargonProperties.setNegotiationPolicy(SslNegotiationPolicy.valueOf(baseRestConfig.getSslNegotiationPolicy()));
		return jargonProperties;	
	}
	
	@Bean
	public IRODSSimpleProtocolManager irodsSimpleProtocolManager() {
		return IRODSSimpleProtocolManager.instance();
	}
	
	@Bean
	public JargonKeyedPoolConfig jargonKeyedPoolConfig() {
		JargonKeyedPoolConfig config = new JargonKeyedPoolConfig();
		config.setJmxNameBase("irods-rest-base-pool");
		config.setJmxNamePrefix("base-rest");
		//config.setJmxEnabled(false);
		return config;
	}
	
	@Bean
	public JargonPooledObjectFactory jargonPooledObjectFactory(IRODSSession irodsSession, IRODSSimpleProtocolManager irodsSimpleProtocolManager) {
		JargonPooledObjectFactory jargonPooledObjectFactory = new JargonPooledObjectFactory();
		jargonPooledObjectFactory.setIrodsSession(irodsSession);
		jargonPooledObjectFactory.setIrodsSimpleProtocolManager(irodsSimpleProtocolManager);
		return jargonPooledObjectFactory;
	}
	
	@Bean
	public JargonConnectionCache jargonConnectionCache(JargonPooledObjectFactory jargonPooledObjectFactory, JargonKeyedPoolConfig jargonKeyedPoolConfig) {
		JargonConnectionCache cache = new JargonConnectionCache(jargonPooledObjectFactory, jargonKeyedPoolConfig);
		return cache;
	}
	
	@Bean 
	public CachedIrodsProtocolManager cachedIrodsProtocolManager(JargonConnectionCache jargonConnectionCache) {
		CachedIrodsProtocolManager cachedIrodsProtocolManager = new CachedIrodsProtocolManager();
		cachedIrodsProtocolManager.setJargonConnectionCache(jargonConnectionCache);
		return cachedIrodsProtocolManager;
	}
	
	@Bean
	public IRODSAccessObjectFactory irodsAccessObjectFactory(IRODSSession irodsSession, CachedIrodsProtocolManager cachedIrodsProtocolManager) {
		return new IRODSAccessObjectFactoryImpl(irodsSession);
	}
 	
	public BaseRestConfig getBaseRestConfig() {
		return baseRestConfig;
	}

	public void setBaseRestConfig(BaseRestConfig baseRestConfig) {
		this.baseRestConfig = baseRestConfig;
	}
}
