package com.jslib.lang;

import java.util.Objects;

/**
 * Closure double value usable from anonymous blocks of code, both functions and classes. It is a similar but
 * simplified version of {@link Double} class. See {@link FinalInteger} for description and usage.
 * 
 * @author Iulian Rotaru
 */
public final class FinalDouble
{
  public static FinalDouble valueOf(double value)
  {
    return new FinalDouble(value);
  }

  public static FinalDouble valueOf(String value)
  {
    return new FinalDouble(value);
  }

  private double value;

  public FinalDouble()
  {
  }

  public FinalDouble(double value)
  {
    this.value = value;
  }

  public FinalDouble(String value)
  {
    this.value = Double.valueOf(value);
  }

  public void set(double value)
  {
    this.value = value;
  }

  public double get()
  {
    return value;
  }

  /**
   * Return true if this final double value is not zero.
   * 
   * @return true if this final double value is not zero.
   */
  public boolean booleanValue()
  {
    return value != 0.0;
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
    return (float)value;
  }

  public double doubleValue()
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
    FinalDouble other = (FinalDouble)obj;
    return value == other.value;
  }

  @Override
  public String toString()
  {
    return Double.toString(value);
  }
}