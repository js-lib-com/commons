package com.jslib.rmi;

import java.util.function.BiConsumer;

/**
 * Provider for extra properties injected into remote method invocation. For HTTP protocols, these extra properties are
 * usually injected into HTTP headers but there is no formal constraint to do so.
 * 
 * Properties provider implementation is supplied via remote factory to every proxy created for remote invocation.
 * Implementation should return dynamically created properties, specific to an invocation; empty map is allowed.
 * 
 * @author Iulian Rotaru
 */
@FunctionalInterface
public interface InvocationPropertiesProvider
{

  InvocationProperties get();

  static final InvocationPropertiesProvider EMPTY = new InvocationPropertiesProvider()
  {
    @Override
    public InvocationProperties get()
    {
      return new InvocationProperties()
      {
        @Override
        public void forEach(BiConsumer<String, Object> consumer)
        {
        }
      };
    }
  };

}
