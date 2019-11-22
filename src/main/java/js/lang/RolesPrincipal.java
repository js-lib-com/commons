package js.lang;

import java.security.Principal;
import java.util.List;

/**
 * Roles based principal. Principal is a mean to identify a subject in a security context. This interface uses roles for
 * identification.
 * 
 * @author Iulian Rotaru
 */
public interface RolesPrincipal extends Principal
{
  /**
   * Get roles principal.
   * 
   * @return roles principal.
   */
  List<String> getRoles();
}
