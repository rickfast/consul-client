package ru.hh.consul.util;

import javax.annotation.Nullable;
import static java.lang.String.format;

public class Checks {
  public static void checkState(boolean expression, @Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }
}
