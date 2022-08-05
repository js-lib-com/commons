package com.jslib.converter;

import java.util.Map;

/**
 * Converter provider is used by converter registry to register converters declared by third party libraries. Converter provider
 * implementation should be deployed as Java service in order to be discovered and loaded at run-time.
 * 
 * @author Iulian Rotaru
 */
public interface ConverterProvider {
	/**
	 * Get converters implemented by this converter provider.
	 * 
	 * @return provided converters.
	 */
	Map<Class<?>, Class<? extends Converter>> getConverters();
}
