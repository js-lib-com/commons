package com.jslib.rmi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of parameters names for exception constructor. This annotation is for programmatic exception creation and
 * provides access to constructor parameters names that are not preserved on compiled Java byte code.
 * 
 * The good practice recommends to create immutable exception instances, with final fields and fields values provided by
 * constructor. This annotation assumes constructor parameter and related field names are the same.
 * 
 * @author Iulian Rotaru
 */
@Target(
{
    ElementType.CONSTRUCTOR, ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionParameters {

  /**
   * Comma separated list of parameters names for exception constructor. By convention constructor parameters names are
   * the same with initialized fields.
   * 
   * @return list of parameters names.
   */
  String value();

}
