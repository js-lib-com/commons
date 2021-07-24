package js.util;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import js.converter.Converter;
import js.converter.ConverterException;
import js.converter.ConverterRegistry;
import js.lang.BugError;
import js.lang.InstanceInvocationHandler;
import js.lang.InvocationException;
import js.lang.NoProviderException;
import js.lang.NoSuchBeingException;
import js.lang.VarArgs;
import js.log.Log;
import js.log.LogFactory;

/**
 * Handy methods, mostly reflexive, related to class and class loader. This utility class allows for sub-classing. See
 * {@link js.util} for utility sub-classing description.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class Classes
{
  /** Class logger. */
  private static Log log = LogFactory.getLog(Classes.class);

  /** Prevent default constructor synthesis but allow sub-classing. */
  protected Classes()
  {
  }

  /**
   * Get an object instance identity, guaranteed to be unique on JVM instance. Mostly for debug, it can be used to
   * discriminate multiple instances of the same class.
   * 
   * @param instance object instance.
   * @return object instance identity.
   */
  public static String instance(Object instance)
  {
    return Integer.toHexString(System.identityHashCode(instance));
  }

  /**
   * Load named class using current thread context class loader. Uses current thread context class loader to locate and
   * load requested class. If current thread context class loader is null or fails to find requested class try with this
   * utility class class loader.
   * <p>
   * This logic is designed for Tomcat class loading algorithm. Libraries are loaded using a separated class loader and
   * every application has its own class loader. This method algorithm allows for a class used by an application to be
   * found either by current thread or by library class loader.
   * <p>
   * Considering above, note that there is a subtle difference compared with standard {@link Class#forName(String)}
   * counterpart: this method uses <code>current thread context loader</code> whereas Java standard uses
   * <code>current loader</code>. Maybe not obvious, this 'semantic' difference could lead to class not found on Java
   * standard while this utility method find the class. For example, a class defined by an web application could not be
   * found by Java <code>Class.forName</code> method running inside a class defined by library.
   * 
   * @param className qualified class name, not null.
   * @param <T> class to auto-cast named class.
   * @return class identified by name.
   * @throws NoSuchBeingException if class not found.
   * @throws ClassCastException if found class cannot cast to requested auto-cast type.
   * @throws NullPointerException if <code>className</code> argument is null.
   */
  public static <T> Class<T> forName(String className) throws NoSuchBeingException, ClassCastException, NullPointerException
  {
    if(className == null) {
      throw new NullPointerException("Null class name.");
    }
    Class<T> clazz = forOptionalName(className);
    if(clazz == null) {
      throw new NoSuchBeingException("Class not found: " + className);
    }
    return clazz;
  }

  /**
   * Get optional class by name or null if class not found. Tries to load the class with given class name returning null
   * if not found. This method does not throw exceptions nor log missing class exception. It is supposed caller will
   * test for null returned value and take appropriate action.
   * <p>
   * Uses current thread context class loader to locate and load requested class. If current thread context class loader
   * is null or fails to find requested class try with this utility class class loader.
   * <p>
   * This logic is designed for Tomcat class loading algorithm. Libraries are loaded using a separated class loader and
   * every application has its own class loader. This method algorithm allows for a class used by an application to be
   * found either by current thread or by library class loader.
   * <p>
   * Considering above, note that there is a subtle difference compared with standard {@link Class#forName(String)}
   * counterpart: this method uses <code>current thread context loader</code> whereas Java standard uses
   * <code>current loader</code>. Maybe not obvious, this 'semantic' difference could lead to class not found on Java
   * standard while this utility method find the class. For example, a class defined by an web application could not be
   * found by Java <code>Class.forName</code> method running inside a class defined by library.
   * 
   * @param className requested class name, null tolerated.
   * @param <T> class to auto-cast named class.
   * @return class identified by name or null if not found or given class name argument is null.
   * @throws ClassCastException if found class cannot cast to requested auto-cast type.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> forOptionalName(String className)
  {
    if(className == null) {
      return null;
    }
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if(loader != null) {
      try {
        return (Class<T>)Class.forName(className, true, loader);
      }
      catch(ClassNotFoundException expected) {}
    }

    // try this utility class class loader only if not set as current thread context class loader

    if(loader == null || !loader.equals(Classes.class.getClassLoader())) {
      try {
        return (Class<T>)Class.forName(className, true, Classes.class.getClassLoader());
      }
      catch(ClassNotFoundException unused) {}
    }

    return null;
  }

  /**
   * Get class by name with checked exception. The same as {@link #forName(String)} but throws checked exception in the
   * event requested class is not found.
   * 
   * @param className requested class name, not null.
   * @param <T> class to auto-cast named class.
   * @return class identified by name.
   * @throws ClassNotFoundException if class not found.
   * @throws ClassCastException if found class cannot cast to requested auto-cast type.
   * @throws NullPointerException if <code>className</code> argument is null.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> forNameEx(String className) throws ClassNotFoundException, ClassCastException, NullPointerException
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    ClassNotFoundException classNotFoundException = null;
    if(loader != null) {
      try {
        return (Class<T>)Class.forName(className, true, loader);
      }
      catch(ClassNotFoundException expected) {
        classNotFoundException = expected;
      }
    }

    // try library class loader only if not the same as current thread context class loader, already tried
    if(classNotFoundException != null && loader.equals(Classes.class.getClassLoader())) {
      throw classNotFoundException;
    }
    return (Class<T>)Class.forName(className, true, Classes.class.getClassLoader());
  }

  /**
   * Load service of requested interface throwing exception if provider not found. It is a convenient variant of
   * {@link #loadService(Class)} usable when a missing service implementation is a run-time stopper.
   * 
   * @param serviceInterface service interface.
   * @param <S> service type
   * @return service instance.
   * @throws NoProviderException if service provider not found on run-time.
   */
  public static <S> S loadService(Class<S> serviceInterface)
  {
    S service = loadOptionalService(serviceInterface);
    if(service == null) {
      throw new NoProviderException(serviceInterface);
    }
    return service;
  }

  /**
   * Load service of requested interface using given class loader. Throws exception if service implementation not found
   * on run-time.
   * 
   * @param serviceInterface service interface,
   * @param classLoader class loader.
   * @param <S> service type.
   * @return service instance.
   * @throws NoProviderException if service provider not found on run-time.
   */
  public static <S> S loadService(Class<S> serviceInterface, ClassLoader classLoader)
  {
    Iterator<S> services = ServiceLoader.load(serviceInterface, classLoader).iterator();
    if(services.hasNext()) {
      return services.next();
    }

    // although not official, Android does not support ServiceLoader
    // there is no way to insert service descriptor files into apk META-INF
    // as an workaround uses a hard coded 'services' package to store service descriptors

    try {
      String serviceDescriptor = getResourceAsString("/services/" + serviceInterface.getName());
      return newInstance(serviceDescriptor);
    }
    catch(Throwable e) {
      // log.dump("Service not found", e);
    }
    return null;
  }

  /**
   * Load service of requested interface returning null if service provider not found. Caller should test returned value
   * and take appropriate actions.
   * 
   * @param serviceInterface service interface.
   * @param <S> service type.
   * @return service instance or null.
   */
  public static <S> S loadOptionalService(Class<S> serviceInterface)
  {
    S service = null;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if(classLoader != null) {
      service = loadService(serviceInterface, classLoader);
    }
    if(service == null) {
      service = loadService(serviceInterface, Classes.class.getClassLoader());
    }
    return service;
  }

  /**
   * Test if class is not an interface or abstract and have default constructor. If this method returns true
   * {@link Class#newInstance()} has no reason to fail.
   * 
   * @param clazz class to test if instantiable.
   * @return true if requested class is instantiable.
   */
  public static boolean isInstantiable(Class<?> clazz)
  {
    if(clazz.isInterface()) {
      return false;
    }
    int m = clazz.getModifiers();
    if(Modifier.isAbstract(m)) {
      return false;
    }
    try {
      clazz.getDeclaredConstructor();
    }
    catch(NoSuchMethodException unused) {
      return false;
    }
    return true;
  }

  /**
   * Get method formal parameter types inferred from actual invocation arguments. This utility is a helper for method
   * discovery when have access to the actual invocation argument, but not the formal parameter types list declared by
   * method signature.
   * 
   * @param arguments variable number of method arguments.
   * @return parameter types.
   */
  public static Class<?>[] getParameterTypes(Object... arguments)
  {
    Class<?>[] types = new Class<?>[arguments.length];
    for(int i = 0; i < arguments.length; i++) {
      Object argument = arguments[i];
      if(argument == null) {
        types[i] = Object.class;
        continue;
      }
      if(!(argument instanceof VarArgs)) {
        types[i] = argument.getClass();
        if(types[i].isAnonymousClass()) {
          Class<?>[] interfaces = types[i].getInterfaces();
          Class<?> superclass = interfaces.length > 0 ? interfaces[0] : null;
          if(superclass == null) {
            superclass = types[i].getSuperclass();
          }
          types[i] = superclass;
        }
        continue;
      }

      // here we have VarArgs that must be the last in arguments list
      if(i != arguments.length - 1) {
        throw new BugError("Variable arguments must be the last in arguments list.");
      }
      @SuppressWarnings("unchecked")
      VarArgs<Object> varArgs = (VarArgs<Object>)argument;
      int index = arguments.length - 1;
      types[index] = varArgs.getType();
      arguments[index] = varArgs.getArguments();
    }
    return types;
  }

  /**
   * Invoke instance or class method with arguments. If this method <code>object</code> argument is a {@link Class}
   * delegate {@link #invoke(Object, Class, String, Object...)} with first argument set to null; otherwise
   * <code>object</code> is passed as first argument and its class the second.
   * 
   * @param object object instance or class,
   * @param methodName method name,
   * @param arguments variable number of arguments.
   * @param <T> returned value type.
   * @return value returned by method or null.
   * @throws NoSuchBeingException if method is not found.
   * @throws Exception if invocation fail for whatever reason including method internals.
   */
  public static <T> T invoke(Object object, String methodName, Object... arguments) throws Exception
  {
    Params.notNull(object, "Object");
    Params.notNullOrEmpty(methodName, "Method name");
    if(object instanceof Class<?>) {
      return invoke(null, (Class<?>)object, methodName, arguments);
    }
    else {
      return invoke(object, object.getClass(), methodName, arguments);
    }
  }

  /**
   * Reflexively executes a method on an object. Locate the method on given class, that is not necessarily object class,
   * e.g. it can be a superclass, and execute it. Given arguments are used for both method discovery and invocation.
   * <p>
   * Implementation note: this method is a convenient way to invoke a method when one knows actual parameters but not
   * strictly formal parameters types. When formal parameters include interfaces or abstract classes or an actual
   * parameter is null there is no way to infer formal parameter type from actual parameter instance. The only option
   * left is to locate method by name and if overloads found uses best effort to determine the right parameter list. For
   * this reason, on limit is possible to invoke the wrong method. Anyway, <b>this method is designed for tests
   * logic</b> and best effort is good enough. The same is true for {@link #invoke(Object, String, Object...)}.
   * 
   * @param object object instance,
   * @param clazz object class one of its superclass,
   * @param methodName method name,
   * @param arguments variable number of arguments.
   * @param <T> returned value type.
   * @return value returned by method or null.
   * @throws NoSuchBeingException if method is not found.
   * @throws Exception if invocation fail for whatever reason including method internals.
   */
  @SuppressWarnings("unchecked")
  public static <T> T invoke(Object object, Class<?> clazz, String methodName, Object... arguments) throws Exception
  {
    Params.notNull(clazz, "Class");
    Params.notNullOrEmpty(methodName, "Method name");
    Class<?>[] parameterTypes = getParameterTypes(arguments);
    try {
      Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
      return (T)invoke(object, method, arguments);
    }
    catch(NoSuchMethodException e) {
      // optimistic attempt to locate the method has failed
      // maybe because method parameters list includes interfaces, primitives or null
      // there is no other option but to search through all object methods

      methodsLoop: for(Method method : clazz.getDeclaredMethods()) {
        Class<?>[] methodParameters = method.getParameterTypes();
        if(!method.getName().equals(methodName)) {
          continue;
        }
        if(methodParameters.length != arguments.length) {
          continue;
        }
        // test if concrete arguments list match method formal parameters; if not continue methods loop
        // null is accepted as any type
        for(int i = 0; i < arguments.length; i++) {
          if(arguments[i] != null && !Types.isInstanceOf(arguments[i], methodParameters[i])) {
            continue methodsLoop;
          }
        }
        return (T)invoke(object, method, arguments);
      }
      throw new NoSuchBeingException("Method %s(%s) not found.", methodName, parameterTypes);
    }
  }

  /**
   * Invoke setter method on given object instance. Setter name has format as accepted by
   * {@link Strings#getMethodAccessor(String, String)} and value string is converted to method parameter type using
   * {@link Converter} facility. For this reason set parameter type should have a converter registered.
   * <p>
   * This method has no means to determine method using {@link Class#getMethod(String, Class...)} because parameter
   * value is always string and setter parameter type is unknown. For this reason this method uses
   * {@link #findMethod(Class, String)}. Note that find method searches for named method on object super hierarchy too.
   * 
   * @param object object instance,
   * @param name setter name, method name only without <code>set</code> prefix, dashed case accepted,
   * @param value value to set, string value that is converted to setter method parameter type.
   * @throws NoSuchMethodException if setter not found.
   * @throws Exception if invocation fail for whatever reason including method logic.
   */
  public static void invokeSetter(Object object, String name, String value) throws NoSuchMethodException, Exception
  {
    String setterName = Strings.getMethodAccessor("set", name);
    Class<?> clazz = object.getClass();

    Method method = findMethod(clazz, setterName);
    Class<?>[] parameterTypes = method.getParameterTypes();
    if(parameterTypes.length != 1) {
      throw new NoSuchMethodException(format("%s#%s", clazz.getName(), setterName));
    }

    invoke(object, method, ConverterRegistry.getConverter().asObject((String)value, parameterTypes[0]));
  }

  /**
   * Variant for {@link #invokeSetter(Object, String, String)} but no exception if setter not found.
   * 
   * @param object object instance,
   * @param name setter name,
   * @param value value to set.
   * @throws Exception if invocation fail for whatever reason including method logic.
   */
  public static void invokeOptionalSetter(Object object, String name, String value) throws Exception
  {
    String setterName = Strings.getMethodAccessor("set", name);
    Class<?> clazz = object.getClass();

    Method method = null;
    try {
      method = findMethod(clazz, setterName);
    }
    catch(NoSuchMethodException e) {
      log.debug("Setter |%s| not found in class |%s| or its super hierarchy.", setterName, clazz);
      return;
    }
    Class<?>[] parameterTypes = method.getParameterTypes();
    if(parameterTypes.length != 1) {
      log.debug("Setter |%s#%s(%s)| with invalid parameters number.", method.getDeclaringClass(), method.getName(), Strings.join(parameterTypes, ','));
      return;
    }

    invoke(object, method, ConverterRegistry.getConverter().asObject((String)value, parameterTypes[0]));
  }

  /**
   * Do the actual reflexive method invocation.
   * 
   * @param object object instance,
   * @param method reflexive method,
   * @param arguments variable number of arguments.
   * @return value returned by method execution.
   * @throws Exception if invocation fail for whatever reason including method internals.
   */
  public static Object invoke(Object object, Method method, Object... arguments) throws Exception
  {
    Throwable cause = null;
    try {
      method.setAccessible(true);
      return method.invoke(object instanceof Class<?> ? null : object, arguments);
    }
    catch(IllegalAccessException e) {
      throw new BugError(e);
    }
    catch(InvocationTargetException e) {
      cause = e.getCause();
      if(cause instanceof Exception) {
        throw (Exception)cause;
      }
      if(cause instanceof AssertionError) {
        throw (AssertionError)cause;
      }
    }
    throw new BugError("Method |%s| invocation fails: %s", method, cause);
  }

  /**
   * Predicate to test if a class do possess a given method. Java language support for method discovery throws
   * {@link NoSuchMethodException} if method not found. This method does not throws exception but returns a boolean.
   * 
   * @param clazz reflexive class,
   * @param methodName method name,
   * @param parameterTypes formal parameters list.
   * @return true if class has requested method.
   */
  public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
  {
    try {
      clazz.getDeclaredMethod(methodName, parameterTypes);
      return true;
    }
    catch(SecurityException e) {
      throw new BugError(e);
    }
    catch(NoSuchMethodException expectable) {
      // ignore exception since is expected
    }
    return false;
  }

  /**
   * Get class method with runtime exception. JRE throws checked {@link NoSuchMethodException} if method is missing,
   * behavior that is not desirable for this library. This method uses runtime, unchecked {@link NoSuchBeingException}
   * instead. Returned method has accessibility set to true.
   * 
   * @param clazz Java class to return method from,
   * @param methodName method name,
   * @param parameterTypes method formal parameters.
   * @return class reflective method.
   * @throws NoSuchBeingException if does not found the method.
   */
  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
  {
    try {
      Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
      method.setAccessible(true);
      return method;
    }
    catch(NoSuchMethodException e) {
      throw new NoSuchBeingException(e);
    }
    catch(SecurityException e) {
      throw new BugError(e);
    }
  }

  /** Prefixes used for getter methods. See {@link #getGetter(Class, String)}. */
  private static final String[] GETTERS_PREFIX = new String[]
  {
      "get", "is"
  };

  /**
   * Get class getter method for requested field. This method is an specialization of
   * {@link #getMethod(Class, String, Class...)}.
   * <p>
   * By convention a method getter for boolean values may have <code>is</code> prefix. Unfortunately this convention is
   * not constrained and there are exception, i.e. use <code>get</code> prefix for booleans. To cope with case this
   * method tries both prefixes and throws exception only if none found.
   * <p>
   * Since getter never has parameters this method does not provide a <code>parameterTypes</code> argument. Also note
   * that parameter <code>fieldName</code> is what its name says: the name of the field not the method name.
   * <p>
   * For convenience field name argument supports dashed case, that is, can contain dash separator. For example
   * <code>phone-number</code> is considered a valid field name and searched method names are
   * <code>getPhoneNumber</code> and <code>isPhoneNumber</code>.
   * 
   * @param clazz class to return getter from,
   * @param fieldName field name.
   * @return getter method.
   * @throws NoSuchMethodException if there is no getter method for requested field.
   */
  public static Method getGetter(Class<?> clazz, String fieldName) throws NoSuchMethodException
  {
    for(String prefix : GETTERS_PREFIX) {
      try {
        Method method = clazz.getDeclaredMethod(Strings.getMethodAccessor(prefix, fieldName));
        method.setAccessible(true);
        return method;
      }
      catch(NoSuchMethodException expectable) {}
      catch(SecurityException e) {
        throw new BugError(e);
      }
    }
    throw new NoSuchMethodException(format("No getter for |%s#%s|.", clazz.getCanonicalName(), fieldName));
  }

  /**
   * Find method with given name and unknown parameter types. Traverses all declared methods from given class and
   * returns the one with specified name; if none found throws {@link NoSuchMethodException}. If there are overloaded
   * methods returns one of them but there is no guarantee which one. Returned method has accessibility enabled.
   * <p>
   * Implementation note: this method is inherently costly since at worst case needs to traverse all class methods. It
   * is recommended to be used with external method cache.
   * 
   * @param clazz Java class to return method from,
   * @param methodName method name.
   * @return class reflective method.
   * @throws NoSuchMethodException if there is no method with requested name.
   */
  public static Method findMethod(Class<?> clazz, String methodName) throws NoSuchMethodException
  {
    for(Method method : clazz.getDeclaredMethods()) {
      if(method.getName().equals(methodName)) {
        method.setAccessible(true);
        return method;
      }
    }

    Class<?> superclass = clazz.getSuperclass();
    if(superclass != null) {
      if(superclass.getPackage().equals(clazz.getPackage())) {
        return findMethod(superclass, methodName);
      }
    }

    throw new NoSuchMethodException(format("%s#%s", clazz.getName(), methodName));
  }

  /**
   * Get source line for the caller method.
   * 
   * @return caller method.
   */
  public static String getCallerMethod()
  {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if(stackTrace.length == 0) {
      return "unknown";
    }
    final StackTraceElement e = stackTrace[0];
    return Strings.concat(e.getClassName(), '#', e.getMethodName(), ':', e.getLineNumber());
  }

  /**
   * Get class field with unchecked runtime exception. JRE throws checked {@link NoSuchFieldException} if field is
   * missing, behavior that is not desirable for this library. This method uses runtime, unchecked
   * {@link NoSuchBeingException} instead. Returned field has accessibility set to true.
   * 
   * @param clazz Java class to return field from,
   * @param fieldName field name.
   * @return class reflective field.
   * @throws NoSuchBeingException if field not found.
   */
  public static Field getField(Class<?> clazz, String fieldName)
  {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    }
    catch(NoSuchFieldException e) {
      throw new NoSuchBeingException(e);
    }
    catch(SecurityException e) {
      throw new BugError(e);
    }
  }

  /**
   * Get class field or null if not found. Try to get named class field and returns null if not found; this method does
   * not throw any exception.
   * 
   * @param clazz Java class to return field from,
   * @param fieldName field name.
   * @return class reflective field or null.
   */
  public static Field getOptionalField(Class<?> clazz, String fieldName)
  {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    }
    catch(NoSuchFieldException expectable) {}
    catch(SecurityException e) {
      throw new BugError(e);
    }
    return null;
  }

  /**
   * Variant of {@link #getOptionalField(Class, String)} that extends field searching on inheritance hierarchy. Field
   * searching behaves similar to {@link #getFieldEx(Class, String)} in the sense that super-classes hierarchy is
   * limited to given class package.
   * <p>
   * Null field name argument is considered not found and this method returns null.
   * 
   * @param clazz Java class to return field from,
   * @param fieldName field name, null accepted.
   * @return class reflective field or null.
   */
  public static Field getOptionalFieldEx(Class<?> clazz, String fieldName)
  {
    if(fieldName == null) {
      return null;
    }
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    }
    catch(NoSuchFieldException e) {
      Class<?> superclass = clazz.getSuperclass();
      if(superclass != null && clazz.getPackage().equals(superclass.getPackage())) {
        return getOptionalFieldEx(superclass, fieldName);
      }
      return null;
    }
    catch(SecurityException e) {
      throw new BugError(e);
    }
  }

  /**
   * Get named field of requested class class or its super-classes package hierarchy, with checked exception. Tries to
   * get requested field from given class; if not found try with super-classes hierarchy but limited to requested class
   * package. If field still not found throw {@link NoSuchFieldException}.
   * <p>
   * Implementation note: if field not found on requested class this method is executed recursively as long as
   * superclass is in the same package as requested base class. Is not possible to retrieve inherited fields if
   * superclass descendant is in different package.
   * 
   * @param clazz class to search for named field,
   * @param fieldName the name of field to retrieve.
   * @return requested field.
   * @throws NoSuchFieldException if class or super-class package hierarchy has no field with requested name.
   */
  public static Field getFieldEx(Class<?> clazz, String fieldName) throws NoSuchFieldException
  {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    }
    catch(NoSuchFieldException e) {
      Class<?> superclass = clazz.getSuperclass();
      if(superclass != null && clazz.getPackage().equals(superclass.getPackage())) {
        return getFieldEx(superclass, fieldName);
      }
      throw e;
    }
    catch(SecurityException e) {
      throw new BugError(e);
    }
  }

  /**
   * Get object instance field value. Reflective field argument should have accessibility set to true and this condition
   * is fulfilled if field is obtained via {@link #getField(Class, String)}.
   * 
   * @param object instance to retrieve field value from,
   * @param field object reflective field.
   * @param <T> field type.
   * @return field value, possible null.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getFieldValue(Object object, Field field)
  {
    assert field.isAccessible();
    try {
      return (T)field.get(object);
    }
    catch(IllegalAccessException e) {
      throw new BugError(e);
    }
  }

  /**
   * Get instance or class field value. Retrieve named field value from given instance; if <code>object</code> argument
   * is a {@link Class} retrieve class static field.
   * 
   * @param object instance or class to retrieve field value from,
   * @param fieldName field name.
   * @param <T> field value type.
   * @return instance or class field value.
   * @throws NullPointerException if object argument is null.
   * @throws NoSuchBeingException if field is missing.
   * @throws BugError if <code>object</code> is a class and field is not static or if <code>object</code> is an instance
   *           and field is static.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getFieldValue(Object object, String fieldName)
  {
    if(object instanceof Class<?>) {
      return getFieldValue(null, (Class<?>)object, fieldName, null, false);
    }

    Class<?> clazz = object.getClass();
    try {
      Field f = clazz.getDeclaredField(fieldName);
      f.setAccessible(true);
      return (T)f.get(Modifier.isStatic(f.getModifiers()) ? null : object);
    }
    catch(java.lang.NoSuchFieldException e) {
      throw new NoSuchBeingException("Missing field |%s| from |%s|.", fieldName, clazz);
    }
    catch(IllegalAccessException e) {
      throw new BugError(e);
    }
  }

  /**
   * Get optional field value from instance or class. Retrieve named field value from given instance or class; if field
   * is missing return null. Note that this method does not throw exceptions. Also, if optional desired field type is
   * present and named field is of different type returns null.
   * 
   * @param object instance or class to retrieve field value from,
   * @param fieldName field name,
   * @param fieldType optional desired field type.
   * @param <T> field value type.
   * @return instance or class field value or null if field not found.
   */
  @SafeVarargs
  public static <T> T getOptionalFieldValue(Object object, String fieldName, Class<T>... fieldType)
  {
    Class<?> clazz = null;
    if(object instanceof Class<?>) {
      clazz = (Class<?>)object;
      object = null;
    }
    else {
      clazz = object.getClass();
    }
    return getFieldValue(object, clazz, fieldName, fieldType.length == 1 ? fieldType[0] : null, true);
  }

  /**
   * Get instance field value declared into superclass. Retrieve inherited field value from given instance throwing
   * exception if field is not declared into superclass.
   * 
   * @param object instance to retrieve field value from,
   * @param clazz instance superclass,
   * @param fieldName field name.
   * @param <T> field value type.
   * @return instance field value.
   * @throws NoSuchBeingException if field is missing.
   */
  public static <T> T getFieldValue(Object object, Class<?> clazz, String fieldName)
  {
    return getFieldValue(object, clazz, fieldName, null, false);
  }

  /**
   * Helper method for field value retrieval. Get object field value, declared into specified class which can be object
   * class or superclass. If desired field type is not null retrieved field should have the type; otherwise returns
   * null. If field not found this method behavior depends on <code>optional</code> argument: if true returns null,
   * otherwise throws exception.
   * 
   * @param object instance to retrieve field value from or null if static field,
   * @param clazz class or superclass where field is actually declared,
   * @param fieldName field name,
   * @param fieldType desired field type or null,
   * @param optional if true, return null if field is missing.
   * @param <T> field value type.
   * @return field value or null.
   * @throws NoSuchBeingException if optional flag is false and field is missing.
   * @throws BugError if object is null and field is not static or if object is not null and field is static.
   */
  @SuppressWarnings("unchecked")
  private static <T> T getFieldValue(Object object, Class<?> clazz, String fieldName, Class<T> fieldType, boolean optional)
  {
    try {
      Field f = clazz.getDeclaredField(fieldName);
      if(fieldType != null && fieldType != f.getType()) {
        return null;
      }
      f.setAccessible(true);
      if(object == null ^ Modifier.isStatic(f.getModifiers())) {
        throw new BugError("Cannot access static field from instance or instance field from null object.");
      }
      return (T)f.get(object);
    }
    catch(java.lang.NoSuchFieldException e) {
      if(optional) {
        return null;
      }
      throw new NoSuchBeingException("Missing field |%s| from |%s|.", fieldName, clazz);
    }
    catch(IllegalAccessException e) {
      throw new BugError(e);
    }
  }

  /**
   * Set field value for object instance, or class if given object instance is null. This setter takes care to enable
   * accessibility for private fields. Also it tries to adapt <code>value</code> to field type: if <code>value</code> is
   * a string and field type is not, delegates {@link Converter#asObject(String, Class)}. Anyway, if <code>value</code>
   * is not a string it should be assignable to field type otherwise bug error is thrown.
   * 
   * @param object instance to set field value to or null,
   * @param field reflective field,
   * @param value field value, possible null.
   * @throws IllegalArgumentException if <code>field</code> parameter is null.
   * @throws ConverterException if attempt to convert string value to field type but there is no registered converted
   *           for that type.
   * @throws BugError if value is not assignable to field type.
   */
  public static void setFieldValue(Object object, Field field, Object value) throws IllegalArgumentException, BugError
  {
    Params.notNull(field, "Field");

    Class<?> type = field.getType();
    if(!type.equals(String.class) && value instanceof String) {
      value = ConverterRegistry.getConverter().asObject((String)value, type);
    }
    if(value != null && !Types.isInstanceOf(value, type)) {
      throw new BugError("Value |%s| is not assignable to field |%s|.", value.getClass(), field);
    }

    try {
      field.setAccessible(true);
      field.set(object, value);
    }
    catch(IllegalAccessException e) {
      throw new BugError(e);
    }
  }

  /**
   * Set instance or class field value. Try to set field value throwing exception if field not found. If
   * <code>object</code> argument is a class, named field should be static; otherwise exception is thrown.
   * <p>
   * This setter tries to adapt <code>value</code> to field type: if <code>value</code> is a string and field type is
   * not, delegates {@link Converter#asObject(String, Class)}. Anyway, if <code>value</code> is not a string it should
   * be assignable to field type otherwise bug error is thrown.
   * 
   * @param object instance or class to set field value to,
   * @param fieldName field name,
   * @param value field value.
   * @throws IllegalArgumentException if <code>object</code> or <code>fieldName</code> argument is null.
   * @throws NoSuchBeingException if field not found.
   * @throws BugError if object is null and field is not static or if object is not null and field is static.
   * @throws BugError if value is not assignable to field type.
   */
  public static void setFieldValue(Object object, String fieldName, Object value)
  {
    Params.notNull(object, "Object instance or class");
    Params.notNull(fieldName, "Field name");
    if(object instanceof Class<?>) {
      setFieldValue(null, (Class<?>)object, fieldName, value);
    }
    else {
      setFieldValue(object, object.getClass(), fieldName, value);
    }
  }

  /**
   * Set instance field declared into superclass. Try to set field value throwing exception if field is not declared
   * into superclass; if field is static object instance should be null.
   * 
   * @param object instance to set field value to or null if field is static,
   * @param clazz instance superclass where field is declared,
   * @param fieldName field name,
   * @param value field value.
   * @throws NoSuchBeingException if field not found.
   * @throws BugError if object is null and field is not static or if object is not null and field is static.
   */
  public static void setFieldValue(Object object, Class<?> clazz, String fieldName, Object value)
  {
    Field field = getField(clazz, fieldName);
    if(object == null ^ Modifier.isStatic(field.getModifiers())) {
      throw new BugError("Cannot access static field |%s| from instance |%s|.", fieldName, clazz);
    }
    setFieldValue(object, field, value);
  }

  /**
   * Set values to a field of array or collection type. If named field is not of supported types throws bug exception.
   * This method has <code>valuesString</code> parameter that is a list of comma separated value objects. Split values
   * string and converter every resulting item to field component type.
   * 
   * @param object instance to set field value to or null if field is static,
   * @param fieldName field name,
   * @param valuesString comma separated values.
   * @throws ConverterException if there is no converter registered for field component type or parsing fails.
   * @throws BugError if named field type is not of supported types.
   */
  @SuppressWarnings("unchecked")
  public static void setFieldValues(Object object, String fieldName, String valuesString)
  {
    Converter converter = ConverterRegistry.getConverter();
    List<String> values = Strings.split(valuesString, ',');

    Field field = getField(object.getClass(), fieldName);
    Type type = field.getType();
    Object instance = null;

    if(Types.isArray(type)) {
      Class<?> componentType = field.getClass().getComponentType();
      instance = Array.newInstance(componentType, values.size());
      for(int i = 0; i < values.size(); i++) {
        Array.set(instance, i, converter.asObject(values.get(i), componentType));
      }
    }
    else if(Types.isCollection(type)) {
      Class<?> componentType = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
      instance = newCollection(type);
      for(int i = 0; i < values.size(); i++) {
        ((Collection<Object>)instance).add(converter.asObject(values.get(i), componentType));
      }
    }
    else {
      throw new BugError("Cannot set values list to field type |%s|.", field.getType());
    }

    // at this point instance cannot be null
    setFieldValue(object, field, instance);
  }

  /**
   * Get resource name qualified with the package of a given class. Given a resource with simple name
   * <code>pdf-view-fop.xconf</code> and class <code>js.fop.PdfView</code> this method returns
   * <code>js/fop/pdf-view-fop.xconf</code>.
   * 
   * @param type class to infer package on which resource reside,
   * @param resourceName simple resource name.
   * @return qualified resource name.
   */
  public static String getPackageResource(Class<?> type, String resourceName)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(type.getPackage().getName().replace('.', '/'));
    if(resourceName.charAt(0) != '/') {
      builder.append('/');
    }
    builder.append(resourceName);
    return builder.toString();
  }

  /**
   * Retrieve URL of the named resource or null if not found. A resource can be any file including Java source files or
   * package directory. A special case is resource with empty name that denotes package root.
   * <p>
   * Resource is searched into next class loaders, in given order:
   * <ul>
   * <li>current thread context class loader,
   * <li>this utility class loader,
   * <li>system class loader, as returned by {@link ClassLoader#getSystemClassLoader()}
   * </ul>
   * 
   * @param name resource qualified name, using path separators instead of dots.
   * @return resource URL or null if not found.
   * @throws IllegalArgumentException if <code>name</code> argument is null.
   */
  public static URL getResource(String name)
  {
    Params.notNull(name, "Resource name");
    // not documented behavior: accept but ignore trailing path separator
    if(!name.isEmpty() && name.charAt(0) == '/') {
      name = name.substring(1);
    }

    return getResource(name, new ClassLoader[]
    {
        Thread.currentThread().getContextClassLoader(), Classes.class.getClassLoader(), ClassLoader.getSystemClassLoader()
    });
  }

  /**
   * Get named resource URL from a list of class loaders. Traverses class loaders in given order searching for requested
   * resource. Return first resource found or null if none found.
   * 
   * @param name resource name with syntax as requested by Java ClassLoader,
   * @param classLoaders target class loaders.
   * @return found resource URL or null.
   */
  private static URL getResource(String name, ClassLoader[] classLoaders)
  {
    // Java standard class loader require resource name to be an absolute path without leading path separator
    // at this point <name> argument is guaranteed to not start with leading path separator

    for(ClassLoader classLoader : classLoaders) {
      URL url = classLoader.getResource(name);
      if(url == null) {
        // it seems there are class loaders that require leading path separator
        // not confirmed rumor but found in similar libraries
        url = classLoader.getResource('/' + name);
      }
      if(url != null) {
        return url;
      }
    }
    return null;
  }

  /**
   * Retrieve resource, identified by qualified name, as input stream. This method does its best to load requested
   * resource but throws exception if fail. Resource is loaded using {@link ClassLoader#getResourceAsStream(String)} and
   * <code>name</code> argument should follow Java class loader convention: it is always considered as absolute path,
   * that is, should contain package but does not start with leading path separator, e.g. <code>js/fop/config.xml</code>
   * .
   * <p>
   * Resource is searched into next class loaders, in given order:
   * <ul>
   * <li>current thread context class loader,
   * <li>this utility class loader,
   * <li>system class loader, as returned by {@link ClassLoader#getSystemClassLoader()}
   * </ul>
   * 
   * @param name resource qualified name, using path separators instead of dots.
   * @return resource input stream.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws NoSuchBeingException if resource not found.
   */
  public static InputStream getResourceAsStream(String name)
  {
    Params.notNullOrEmpty(name, "Resource name");
    // not documented behavior: accept but ignore trailing path separator
    if(name.charAt(0) == '/') {
      name = name.substring(1);
    }

    InputStream stream = getResourceAsStream(name, new ClassLoader[]
    {
        Thread.currentThread().getContextClassLoader(), Classes.class.getClassLoader(), ClassLoader.getSystemClassLoader()
    });
    if(stream == null) {
      throw new NoSuchBeingException("Resource |%s| not found.", name);
    }
    return stream;
  }

  /**
   * Get named resource input stream from a list of class loaders. Traverses class loaders in given order searching for
   * requested resource. Return first resource found or null if none found.
   * 
   * @param name resource name with syntax as required by Java ClassLoader,
   * @param classLoaders target class loaders.
   * @return found resource as input stream or null.
   */
  private static InputStream getResourceAsStream(String name, ClassLoader[] classLoaders)
  {
    // Java standard class loader require resource name to be an absolute path without leading path separator
    // at this point <name> argument is guaranteed to not start with leading path separator

    for(ClassLoader classLoader : classLoaders) {
      InputStream stream = classLoader.getResourceAsStream(name);
      if(stream == null) {
        // it seems there are class loaders that require leading path separator
        // not confirmed rumor but found in similar libraries
        stream = classLoader.getResourceAsStream('/' + name);
      }
      if(stream != null) {
        return stream;
      }
    }
    return null;
  }

  /**
   * Get file resource, that is, resource with <em>file</em> protocol. Try to load resource throwing exception if not
   * found. If resource protocol is <em>file</em> returns it as {@link java.io.File} otherwise throws unsupported
   * operation.
   * 
   * @param name resource name.
   * @return resource file.
   * @throws NoSuchBeingException if named resource can't be loaded.
   * @throws UnsupportedOperationException if named resource protocol is not <em>file</em>.
   */
  public static File getResourceAsFile(String name)
  {
    URL resourceURL = getResource(name);
    if(resourceURL == null) {
      throw new NoSuchBeingException("Resource |%s| not found.", name);
    }
    String protocol = resourceURL.getProtocol();
    if("file".equals(protocol)) try {
      return new File(resourceURL.toURI());
    }
    catch(URISyntaxException e) {
      throw new BugError("Invalid syntax on URL returned by getResource.");
    }
    throw new UnsupportedOperationException("Can't get a file for a resource using protocol" + protocol);
  }

  /**
   * Convenient method to retrieve named resource as reader. Uses {@link #getResourceAsStream(String)} and return the
   * stream wrapped into a reader.
   * 
   * @param name resource name.
   * @return reader for named resource.
   * @throws UnsupportedEncodingException if Java run-time does not support UTF-8 encoding.
   * @throws NoSuchBeingException if resource not found.
   */
  public static Reader getResourceAsReader(String name) throws UnsupportedEncodingException
  {
    return new InputStreamReader(getResourceAsStream(name), "UTF-8");
  }

  /**
   * Retrieve text resource content as a string. Uses {@link #getResourceAsReader(String)} to reader resource content
   * and store it in a String.
   * 
   * @param name resource name.
   * @return resource content as string.
   * @throws NoSuchBeingException if resource not found.
   * @throws IOException if resource reading fails.
   */
  public static String getResourceAsString(String name) throws IOException
  {
    StringWriter writer = new StringWriter();
    Files.copy(getResourceAsReader(name), writer);
    return writer.toString();
  }

  /**
   * Retrieve binary resource as array of bytes. Uses {@link #getResourceAsStream(String)} to read binary resource and
   * return bytes array.
   * 
   * @param name resource name.
   * @return binary resource content as array of bytes.
   * @throws NoSuchBeingException if resource not found.
   * @throws IOException if resource reading fails.
   */
  public static byte[] getResourceAsBytes(String name) throws IOException
  {
    InputStream is = getResourceAsStream(name);
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    try {
      byte[] buffer = new byte[4096];
      int length = 0;
      while((length = is.read(buffer)) != -1) {
        os.write(buffer, 0, length);
      }
    }
    finally {
      Files.close(is);
      Files.close(os);
    }
    return os.toByteArray();
  }

  /**
   * List package resources from local classes or from archives. List resource file names that respect a given pattern
   * from local stored package or from Java archive file. This utility method tries to locate the package with given
   * name using {@link ClassLoader#getResources(String)}. If returned URL starts with <code>jar:file</code>, that is,
   * has JAR protocol the package is contained into a Java archive file and is processed entry by entry. Otherwise
   * protocol should be <code>file</code> and package is located into local classes and is processed as file. If
   * protocol is neither <code>file</code> nor <code>jar:file</code> throws unsupported operation.
   * 
   * @param packageName qualified package name, possible empty for package root,
   * @param fileNamesPattern file names pattern as accepted by {@link FilteredStrings#FilteredStrings(String)}.
   * @return collection of resources from package matching requested pattern, possible empty.
   * @throws NoSuchBeingException if package is not found by current application class loader.
   * @throws UnsupportedOperationException if found package URL protocol is not <code>file</code> or
   *           <code>jar:file</code>.
   */
  public static Collection<String> listPackageResources(String packageName, String fileNamesPattern)
  {
    String packagePath = Files.dot2urlpath(packageName);

    Set<URL> packageURLs = new HashSet<>();
    for(ClassLoader classLoader : new ClassLoader[]
    {
        Thread.currentThread().getContextClassLoader(), Classes.class.getClassLoader(), ClassLoader.getSystemClassLoader()
    }) {
      try {
        packageURLs.addAll(Collections.list(classLoader.getResources(packagePath)));
      }
      catch(IOException e) {
        log.error(e);
      }
    }
    if(packageURLs.isEmpty()) {
      throw new NoSuchBeingException("Package |%s| not found.", packageName);
    }

    Set<String> resources = new HashSet<>();
    for(URL packageURL : packageURLs) {
      resources.addAll(listPackageResources(packageURL, packagePath, fileNamesPattern));
    }
    return resources;
  }

  /**
   * Get package resources that match file name pattern. Returned collection is in no particular order and can be empty
   * if no resource match requested pattern. This helper process both <code>file</code> and <code>jar:file</code> URL
   * protocols meaning it searches on both local classes and <code>.jar</code> files.
   * 
   * @param packageURL package URL as returned by {@link ClassLoader#getResources(String)},
   * @param packagePath package path for resources lookup, possible empty for package root,
   * @param fileNamesPattern file name pattern.
   * @return collection of resources with requested file name pattern, possible empty.
   * @throws UnsupportedOperationException given package URL protocol is not <code>file</code> or <code>jar:file</code>.
   */
  private static Collection<String> listPackageResources(URL packageURL, String packagePath, String fileNamesPattern)
  {
    if("file".equals(packageURL.getProtocol())) {
      FilteredStrings resources = new FilteredStrings(fileNamesPattern);
      try {
        if(!packagePath.isEmpty()) {
          packagePath += "/";
        }
        resources.addAll(packagePath, new File(packageURL.toURI()).list());
      }
      catch(URISyntaxException e) {
        throw new BugError("Invalid syntax on URL |%s| returned by getResource.", packageURL);
      }
      return resources;
    }

    if(packageURL.toExternalForm().startsWith("jar:file")) {
      String path = packageURL.getPath();
      String jarPath = path.substring(5, path.indexOf("!"));

      JarFile jar = null;
      try {
        jar = new JarFile(jarPath);

        FilteredStrings resources = new FilteredStrings(fileNamesPattern);
        assert jar != null;
        Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements()) {
          resources.add(entries.nextElement().getName());
        }
        return resources;
      }
      catch(IOException e) {
        // we are here for not well formed archive or file reading errors
        // both conditions are already checked by class loader when tried to discover the package
        throw new BugError("Java archive |%s| is not well formed or file reading fails.", packageURL.getPath());
      }
      finally {
        if(jar != null) {
          try {
            jar.close();
          }
          catch(IOException e) {
            log.error(e);
          }
        }
      }
    }

    throw new UnsupportedOperationException(format("Bad protocol |%s|.", packageURL));
  }

  /**
   * Create instance of requested type - objects, collections or maps, using a constructor with given arguments. This
   * factory method detect if type is object, collection or map and delegates {@link #newInstance(Class, Object...)},
   * respective {@link #newCollection(Type)} or {@link #newMap(Type)}.
   * 
   * @param type instance type.
   * @param arguments constructor arguments.
   * @param <T> instance type.
   * @return newly created instance.
   * @throws BugError if instance cannot be created.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Type type, Object... arguments)
  {
    if(type instanceof ParameterizedType) {
      ParameterizedType t = (ParameterizedType)type;
      if(!(t.getRawType() instanceof Class<?>)) {
        throw new BugError("Raw type for parameterized type should be a class but is |%s|.", t.getRawType());
      }
      Class<?> rawClass = (Class<?>)t.getRawType();
      if(rawClass.isInterface()) {
        if(Types.isCollection(rawClass)) {
          return newCollection(rawClass);
        }
        if(Types.isMap(rawClass)) {
          return newMap(rawClass);
        }
      }
      return newInstance(t.getRawType(), arguments);
    }

    if(type instanceof Class) {
      return (T)newInstance((Class<?>)type, arguments);
    }

    throw new BugError("Cannot create new instance for type |%s|.", type);
  }

  /**
   * Create a new instance. Handy utility for hidden classes creation. Constructor accepting given arguments, if any,
   * must exists.
   * 
   * @param className fully qualified class name,
   * @param arguments variable number of arguments to be passed to constructor.
   * @param <T> instance type.
   * @return newly created instance.
   * @throws NoSuchBeingException if class or constructor not found.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(String className, Object... arguments)
  {
    return (T)newInstance(Classes.forName(className), arguments);
  }

  /**
   * Create a new object instance of specified class. This factory is specialized in objects creation; for collections,
   * lists and maps see {@link #newCollection(Type)}, {@link #newList(Type)} respective {@link #newMap(Type)}.
   * <p>
   * If arguments are supplied a constructor with exact formal parameters is located otherwise default constructor is
   * used; if none found throws {@link NoSuchBeingException}. This method forces accessibility so is not mandatory for
   * constructor to be public.
   * <p>
   * Requested class should be instantiable, i.e. should not be interface, abstract or void class. Also arrays are not
   * accepted by this factory method.
   * 
   * @param clazz object class to instantiate,
   * @param arguments optional constructor arguments.
   * @param <T> instance type.
   * @return newly created instance
   * @throws BugError if attempt to instantiate interface, abstract, array or void class.
   * @throws NoSuchBeingException if class or constructor not found.
   * @throws BugError for any other failing condition, since there is no particular reason to expect fail.
   * @throws InvocationException with target exception if constructor fails on its execution.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz, Object... arguments) throws BugError, NoSuchBeingException, BugError
  {
    if(clazz.isInterface()) {
      throw new BugError("Attempt to create new instance for interface |%s|.", clazz);
    }
    if(clazz.isArray()) {
      throw new BugError("Attempt to create new array instance of |%s|.", clazz);
    }
    if(Modifier.isAbstract(clazz.getModifiers())) {
      throw new BugError("Attempt to create new instance for abstract class |%s|.", clazz);
    }
    if(Types.isVoid(clazz)) {
      throw new BugError("Attempt to instantiate void class.");
    }

    try {
      Constructor<T> constructor = null;
      if(arguments.length > 0) {
        constructorsLoop: for(Constructor<?> ctor : clazz.getDeclaredConstructors()) {
          Class<?>[] parameters = ctor.getParameterTypes();
          if(parameters.length != arguments.length) {
            continue;
          }
          for(int i = 0; i < arguments.length; i++) {
            if(arguments[i] == null) {
              continue;
            }
            if(!Types.isInstanceOf(arguments[i], parameters[i])) {
              continue constructorsLoop;
            }
          }
          constructor = (Constructor<T>)ctor;
          break;
        }
        if(constructor == null) {
          throw missingConstructorException(clazz, arguments);
        }
      }
      else {
        constructor = clazz.getDeclaredConstructor();
      }
      constructor.setAccessible(true);
      return constructor.newInstance(arguments);
    }
    catch(NoSuchMethodException e) {
      throw missingConstructorException(clazz, arguments);
    }
    catch(InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new BugError(e);
    }
    catch(InvocationTargetException e) {
      throw new InvocationException(e);
    }
  }

  /**
   * Helper for missing constructor exception.
   * 
   * @param clazz constructor class,
   * @param arguments constructor arguments.
   * @return formatted exception.
   */
  private static NoSuchBeingException missingConstructorException(Class<?> clazz, Object... arguments)
  {
    Type[] types = new Type[arguments.length];
    for(int i = 0; i < arguments.length; ++i) {
      types[i] = arguments[i].getClass();
    }
    return new NoSuchBeingException("Missing constructor(%s) for |%s|.", Arrays.toString(types), clazz);
  }

  /** Default implementations for collection interfaces. */
  private static Map<Class<?>, Class<?>> COLLECTIONS = new HashMap<Class<?>, Class<?>>();
  static {
    Map<Class<?>, Class<?>> m = new HashMap<Class<?>, Class<?>>();
    m.put(Collection.class, Vector.class);
    m.put(List.class, ArrayList.class);
    m.put(ArrayList.class, ArrayList.class);
    m.put(Vector.class, Vector.class);
    m.put(Set.class, HashSet.class);
    m.put(HashSet.class, HashSet.class);
    m.put(SortedSet.class, TreeSet.class);
    m.put(TreeSet.class, TreeSet.class);
    COLLECTIONS = Collections.unmodifiableMap(m);
  }

  /**
   * Create new collection of given type.
   * 
   * @param type collection type.
   * @param <T> collection type.
   * @return newly create collection.
   */
  public static <T extends Collection<?>> T newCollection(Type type)
  {
    return newRegisteredInstance(COLLECTIONS, type);
  }

  /** Default implementations for list interfaces. */
  private static Map<Class<?>, Class<?>> LISTS = new HashMap<Class<?>, Class<?>>();
  static {
    Map<Class<?>, Class<?>> m = new HashMap<Class<?>, Class<?>>();
    m.put(List.class, ArrayList.class);
    m.put(AbstractList.class, ArrayList.class);
    m.put(ArrayList.class, ArrayList.class);
    m.put(LinkedList.class, LinkedList.class);
    m.put(Vector.class, Vector.class);
    m.put(Stack.class, Stack.class);
    LISTS = Collections.unmodifiableMap(m);
  }

  /**
   * Get default implementation for requested list type.
   * 
   * @param listType raw list type.
   * @param <T> list type.
   * @return default implementation for requested list.
   */
  @SuppressWarnings("unchecked")
  public static <T extends List<?>> Class<T> getListDefaultImplementation(Type listType)
  {
    return (Class<T>)getImplementation(LISTS, listType);
  }

  /**
   * Create new list of given raw type.
   * 
   * @param type list raw type.
   * @param <T> list type.
   * @return list instance.
   */
  public static <T extends List<?>> T newList(Type type)
  {
    return newRegisteredInstance(LISTS, type);
  }

  /**
   * Lookup implementation into given registry, throwing exception if not found.
   * 
   * @param implementationsRegistry implementations registry,
   * @param interfaceType interface to lookup into registry.
   * @return implementation for requested interface type.
   * @throws BugError if implementation is not found into registry.
   */
  public static Class<?> getImplementation(Map<Class<?>, Class<?>> implementationsRegistry, Type interfaceType)
  {
    Class<?> implementation = implementationsRegistry.get(interfaceType);
    if(implementation == null) {
      throw new BugError("No registered implementation for type |%s|.", interfaceType);
    }
    return implementation;
  }

  /**
   * Lookup implementation for requested interface into given registry and return a new instance of it.
   * 
   * @param implementationsRegistry implementations registry,
   * @param interfaceType interface to lookup into registry.
   * @param <T> instance type.
   * @return implementation instance.
   * @throws BugError if implementation is not found into registry.
   */
  @SuppressWarnings("unchecked")
  private static <T> T newRegisteredInstance(Map<Class<?>, Class<?>> implementationsRegistry, Type interfaceType) throws BugError
  {
    Class<?> implementation = getImplementation(implementationsRegistry, interfaceType);
    try {
      return (T)implementation.newInstance();
    }
    catch(IllegalAccessException e) {
      // illegal access exception is thrown if the class or its no-arguments constructor is not accessible
      // since we use well known JRE classes this condition may never meet
      throw new BugError(e);
    }
    catch(InstantiationException e) {
      // instantiation exception is thrown if class is abstract, interface, array, primitive or void
      // since we use well known JRE classes this condition may never meet
      throw new BugError(e);
    }
  }

  /** Default implementations for maps interfaces. */
  private static Map<Class<?>, Class<?>> MAPS = new HashMap<Class<?>, Class<?>>();
  static {
    Map<Class<?>, Class<?>> m = new HashMap<Class<?>, Class<?>>();
    m.put(Map.class, HashMap.class);
    m.put(HashMap.class, HashMap.class);
    m.put(SortedMap.class, TreeMap.class);
    m.put(TreeMap.class, TreeMap.class);
    m.put(Hashtable.class, Hashtable.class);
    m.put(Properties.class, Properties.class);
    MAPS = Collections.unmodifiableMap(m);
  }

  /**
   * Create new map of given type.
   * 
   * @param type map type.
   * @param <T> map type.
   * @return newly created map.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Map<?, ?>> T newMap(Type type)
  {
    Class<?> implementation = MAPS.get(type);
    if(implementation != null) {
      return (T)newInstance(implementation);
    }
    if(!(type instanceof Class) || !isInstantiable((Class<?>)type)) {
      throw new BugError("No registered implementation for map |%s|.", type);
    }
    return (T)newInstance(type);
  }

  /**
   * Get the underlying class for a type, or null if the type is a variable type.
   * 
   * @param type the type
   * @return the underlying class
   */
  public static Class<?> forType(Type type)
  {
    if(type instanceof Class) {
      return (Class<?>)type;
    }

    if(type instanceof ParameterizedType) {
      return forType(((ParameterizedType)type).getRawType());
    }

    if(!(type instanceof GenericArrayType)) {
      return null;
    }

    Type componentType = ((GenericArrayType)type).getGenericComponentType();
    Class<?> componentClass = forType(componentType);
    return componentClass != null ? Array.newInstance(componentClass, 0).getClass() : null;
  }

  /**
   * Return wrapped instance of a Java Proxy implemented on a {@link InstanceInvocationHandler} handler. If given
   * <code>instance</code> is not a Java Proxy return it as it is and return null if <code>instance</code> is null. It
   * is considered a bug if <code>instance</code> is a Java Proxy and its handler does not implement
   * {@link InstanceInvocationHandler}.
   * 
   * @param instance instance to unproxy, null accepted.
   * @param <T> instance type.
   * @return proxy wrapped instance, possible null.
   * @throws BugError if Java Proxy handler does not implement {@link InstanceInvocationHandler}.
   */
  public static <T> T unproxy(T instance)
  {
    if(instance == null) {
      return null;
    }
    if(!(instance instanceof Proxy)) {
      return instance;
    }
    if(!(Proxy.getInvocationHandler(instance) instanceof InstanceInvocationHandler)) {
      throw new BugError("Cannot unproxy instance |%s|. Proxy handler does not implement |%s|", instance.getClass(), InstanceInvocationHandler.class);
    }
    @SuppressWarnings("unchecked")
    InstanceInvocationHandler<T> handler = (InstanceInvocationHandler<T>)Proxy.getInvocationHandler(instance);
    return handler.getWrappedInstance();
  }
}
