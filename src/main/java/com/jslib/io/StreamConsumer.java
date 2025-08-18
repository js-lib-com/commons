package com.jslib.io;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface StreamConsumer
{

  void handle(OutputStream outputStream) throws IOException;

}
