package js.lang;

/**
 * Hook executed just before managed instance destruction. Implementation should use this hook to release resources used
 * by managed instance, especially external resources. After executing this hook managed instance, reference is released
 * for garbage collect. Do not attempt to use an instance after executing pre-destroy.
 * <p>
 * Pre-destroy is application defined logic and can fail in not controlled ways. For this reason there is no assumption
 * about a particular exception. This interface simply throws all exceptions pre-destroy logic may rise.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface ManagedPreDestroy
{
  /**
   * Hook executed just before managed instance destruction.
   * 
   * @throws Exception propagate any exception pre-destroy logic may rise.
   */
  void preDestroy() throws Exception;
}
