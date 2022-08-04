package js.lang;

/**
 * Hook executed after managed instance was created, initialized and configured. Post-construct is application defined
 * logic and can fail in not controlled ways. For this reason there is no assumption about a particular exception. This
 * interface simply throws all exceptions post-construct logic may rise.
 * 
 * @author Iulian Rotaru
 */
public interface ManagedPostConstruct
{
  /**
   * Hook executed after managed instance was created, initialized and configured.
   * 
   * @throws Exception bubble-up any exception post-construct logic may rise.
   */
  void postConstruct() throws Exception;
}
