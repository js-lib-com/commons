package com.jslib.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.jslib.util.Files;

public class VariableWriterUnitTest
{
  private static final String template = "I was born on ${birth-year}, on Jassy.";
  private static final Map<String, String> variables = new HashMap<String, String>();
  static {
    variables.put("birth-year", "1964");
  }

  @Test
  public void testInternalStringWriter() throws IOException
  {
    VariablesWriter writer = new VariablesWriter(variables);
    Files.copy(new StringReader(template), writer);
    assertEquals("I was born on 1964, on Jassy.", writer.toString());
  }

  @Test
  public void testWriterDecorator() throws IOException
  {
    StringWriter writer = new StringWriter();
    Files.copy(new StringReader(template), new VariablesWriter(writer, variables));
    assertEquals("I was born on 1964, on Jassy.", writer.toString());
  }
}
