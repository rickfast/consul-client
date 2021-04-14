package ru.hh.consul.util;

public class LongParser {
  public static long decodeFromAnyRadix(String numberAsString) throws NumberFormatException {
    if (numberAsString.length() == 0) {
      throw new NumberFormatException("empty string");
    }

    String rawValue;
    int radix;
    char firstChar = numberAsString.charAt(0);
    if (numberAsString.startsWith("0x") || numberAsString.startsWith("0X")) {
      rawValue = numberAsString.substring(2);
      radix = 16;
    } else if (firstChar == '#') {
      rawValue = numberAsString.substring(1);
      radix = 16;
    } else if (firstChar == '0' && numberAsString.length() > 1) {
      rawValue = numberAsString.substring(1);
      radix = 8;
    } else {
      rawValue = numberAsString;
      radix = 10;
    }
    return Long.parseUnsignedLong(rawValue, radix);
  }
}
