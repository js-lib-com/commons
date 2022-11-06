package com.jslib.lang;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Closure integer value usable from anonymous blocks of code, both functions and classes. It is a similar but
 * simplified version of {@link Integer} class.
 * 
 * It is designed to be used whenever need to update an integer value declared in a closure from an anonymous but
 * synchronous block of code. It is not thread safe; use {@link AtomicInteger} if concurrency is needed.
 * 
 * <pre>
 * final FinalInteger counter = new FinalInteger();
 * list.forEach(item -> counter.increment());
 * if(counter.booleanValue()) { ... }
 * </pre>
 *
 * Although this classes is used with <code>final</code> variable modifier it has mutator methods.
 * 
 * @author Iulian Rotaru
 */
public final class FinalInteger
{
  public static FinalInteger valueOf(int value)
  {
    return new FinalInteger(value);
  }

  public static FinalInteger valueOf(String value)
  {
    return new FinalInteger(value);
  }

  private int value;

  public FinalInteger()
  {
  }

  public FinalInteger(int value)
  {
    this.value = value;
  }

  public FinalInteger(String value)
  {
    this.value = Integer.valueOf(value);
  }

  public void set(int value)
  {
    this.value = value;
  }

  public int get()
  {
    return value;
  }

  /**
   * Return true if this final integer value is not zero.
   * 
   * @return true if this final integer value is not zero.
   */
  public boolean booleanValue()
  {
    return value != 0;
  }

  public byte byteValue()
  {
    return (byte)value;
  }

  public short shortValue()
  {
    return (short)value;
  }

  public int intValue()
  {
    return value;
  }

  public long longValue()
  {
    return value;
  }

  public float floatValue()
  {
    return value;
  }

  public double doubleValue()
  {
    return value;
  }

  public void increment()
  {
    ++value;
  }

  public void increment(int increment)
  {
    value += increment;
  }

  public void decrement()
  {
    --value;
  }

  public void decrement(int decrement)
  {
    value -= decrement;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(value);
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    FinalInteger other = (FinalInteger)obj;
    return value == other.value;
  }

  @Override
  public String toString()
  {
    return Integer.toString(value);
  }
}