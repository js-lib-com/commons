package com.jslib.rmi;

public class TestException extends Exception
{
  private static final long serialVersionUID = -7429260651101038377L;

  private final Integer start;
  private final Integer end;

  @ExceptionParameters("message, start, end")
  public TestException(String message, Integer start, Integer end)
  {
    super(message);
    this.start = start;
    this.end = end;
  }

  public TestException()
  {
    super();
    this.start = null;
    this.end = null;
  }

  public TestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
    this.start = null;
    this.end = null;
  }

  public TestException(String message, Throwable cause)
  {
    super(message, cause);
    this.start = null;
    this.end = null;
  }

  public TestException(String message)
  {
    super(message);
    this.start = null;
    this.end = null;
  }

  public TestException(Throwable cause)
  {
    super(cause);
    this.start = null;
    this.end = null;
  }

  public Integer getStart()
  {
    return start;
  }

  public Integer getEnd()
  {
    return end;
  }
}
