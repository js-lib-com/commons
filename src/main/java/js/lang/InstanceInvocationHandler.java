package js.lang;

import java.lang.reflect.InvocationHandler;

/**
 * Extension of Java Proxy invocation handler specialized on wrapping instances. An instance invocation handler always
 * has an underlying object instance and route Proxy invocations to it. Wrapped instance can be created by proxy handler
 * itself or externally and injected via constructor or setter.
 * <p>
 * This interface provides a single method, see {@link #getWrappedInstance()}, that facilitate access to underlying
 * object instance.
 * 
 * @author Iulian Rotaru
 * @param <T> wrapped instance type.
 * @version final
 */
public interface InstanceInvocationHandler<T> extends InvocationHandler
{
  /**
   * Get managed instance wrapped by this invocation handler.
   * 
   * @return wrapped managed instance.
   */
  T getWrappedInstance();
}
