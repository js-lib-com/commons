package com.jslib.lang;

import java.util.Objects;

/**
 * Closure long value usable from anonymous blocks of code, both functions and classes. It is a similar but
 * simplified version of {@link Long} class. See {@link FinalInteger} for description and usage.
 * 
 * @author Iulian Rotaru
 */
public final class FinalLong
{
  public static FinalLong valueOf(long value)
  {
    return new FinalLong(value);
  }

  public static FinalLong valueOf(String value)
  {
    return new FinalLong(value);
  }

  private long value;

  public FinalLong()
  {
  }

  public FinalLong(long value)
  {
    this.value = value;
  }

  public FinalLong(String value)
  {
    this.value = Long.valueOf(value);
  }

  public void set(long value)
  {
    this.value = value;
  }

  public long get()
  {
    return value;
  }

  /**
   * Return true if this final long value is not zero.
   * 
   * @return true if this final long value is not zero.
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
    return (int)value;
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

  public void increment(long increment)
  {
    value += increment;
  }

  public void decrement()
  {
    --value;
  }

  public void decrement(long decrement)
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
    FinalLong other = (FinalLong)obj;
    return value == other.value;
  }

  @Override
  public String toString()
  {
    return Long.toString(value);
  }
}