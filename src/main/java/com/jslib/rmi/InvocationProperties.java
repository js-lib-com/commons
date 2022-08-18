package com.jslib.rmi;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface InvocationProperties
{
  void forEach(BiConsumer<String, Object> consumer);
}
