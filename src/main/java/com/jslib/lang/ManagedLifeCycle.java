package com.jslib.lang;

/**
 * A managed life cycle covers instance creation, post-construction and pre-destroying. Post-construction and
 * pre-destroying are inherited from {@link ManagedPostConstruct}, respective {@link ManagedPreDestroy}. This interface
 * only adds automatic creation at container start.
 * <p>
 * To benefit from managed life cycle a class should implement this interface, as in sample code below.
 * 
 * <pre>
 * class DemoClass implements ManagedLifeCycle
 * {
 *  void postConstruct() ...
 * 
 *  void preDestroy() ...
 * }
 * </pre>
 * <p>
 * Managed life cycle interface can be used on class implementation, as in example above or can be extended by class
 * interface, like below:
 * 
 * <pre>
 * class DemoInterface extends ManagedLifeCycle
 * {
 * }
 * 
 * class DemoClass implements DemoInterface
 * {
 *  void postConstruct() ...
 * 
 *  void preDestroy() ...
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public interface ManagedLifeCycle extends ManagedPostConstruct, ManagedPreDestroy
{
  /**
   * Hook executed after class instance was created and initialized.
   * 
   * @throws Exception bubble-up any exception post-construct logic may rise.
   */
  void postConstruct() throws Exception;

  /**
   * Hook executed just before managed class instance destruction.
   * 
   * @throws Exception propagate any exception pre-destroy logic may rise.
   */
  void preDestroy() throws Exception;
}
