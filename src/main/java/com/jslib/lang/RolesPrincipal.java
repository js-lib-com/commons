package com.jslib.lang;

import java.security.Principal;

/**
 * Principal with roles based authorization. Principal is a mean to authenticate a subject in a security context. This
 * interface extends Java security principal with roles based authorization.
 * 
 * @author Iulian Rotaru
 */
public interface RolesPrincipal extends Principal
{
  /**
   * Get principal authorization roles.
   * 
   * @return principal authorization roles.
   */
  String[] getRoles();
}
