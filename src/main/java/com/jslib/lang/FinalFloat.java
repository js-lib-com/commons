package com.jslib.lang;

import java.util.Objects;

/**
 * Closure float value usable from anonymous blocks of code, both functions and classes. It is a similar but
 * simplified version of {@link Float} class. See {@link FinalInteger} for description and usage.
 * 
 * @author Iulian Rotaru
 */
public final class FinalFloat
{
  public static FinalFloat valueOf(float value)
  {
    return new FinalFloat(value);
  }

  public static FinalFloat valueOf(String value)
  {
    return new FinalFloat(value);
  }

  private float value;

  public FinalFloat()
  {
  }

  public FinalFloat(float value)
  {
    this.value = value;
  }

  public FinalFloat(String value)
  {
    this.value = Float.valueOf(value);
  }

  public void set(float value)
  {
    this.value = value;
  }

  public float get()
  {
    return value;
  }

  /**
   * Return true if this final float value is not zero.
   * 
   * @return true if this final float value is not zero.
   */
  public boolean booleanValue()
  {
    return value != 0.0F;
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
    return (long)value;
  }

  public float floatValue()
  {
    return value;
  }

  public float doubleValue()
  {
    return value;
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
    FinalFloat other = (FinalFloat)obj;
    return value == other.value;
  }

  @Override
  public String toString()
  {
    return Float.toString(value);
  }
}