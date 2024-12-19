package ru.hh.school.unittesting.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MethodsEnvironment extends ConstantsEnvironment {

  protected Object[][] addBooks_getDefaultTestCases() {
    return new Object[][]{
        {BOOK_ID_1, DEFAULT_QUANTITY, DEFAULT_QUANTITY},
        {BOOK_ID_2, -DEFAULT_QUANTITY, -DEFAULT_QUANTITY},
        {BOOK_ID_3, ZERO_QUANTITY, ZERO_QUANTITY}
    };
  }

  protected Object[][] addBooks_getTestCasesForExistentBook(String bookId) {
    return new Object[][]{
        {bookId, DEFAULT_QUANTITY, DEFAULT_QUANTITY},
        {bookId, -DEFAULT_QUANTITY * 2, -DEFAULT_QUANTITY},
        {bookId, ZERO_QUANTITY, -DEFAULT_QUANTITY}
    };
  }

  protected static double round(double value) {
    return BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
  }
}
