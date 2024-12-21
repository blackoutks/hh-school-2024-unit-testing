package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryManagerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  @Test
  void testAddBookIncreasesBookInventory() {
    libraryManager.addBook("book1", 5);
    assertEquals(5, libraryManager.getAvailableCopies("book1"));

    libraryManager.addBook("book1", 3);
    assertEquals(8, libraryManager.getAvailableCopies("book1"));
  }

  @Test
  void testBorrowBookSuccessfully() {
    when(userService.isUserActive("user1")).thenReturn(true);
    libraryManager.addBook("book1", 5);

    boolean result = libraryManager.borrowBook("book1", "user1");

    assertTrue(result);
    assertEquals(4, libraryManager.getAvailableCopies("book1"));
    verify(notificationService).notifyUser("user1", "You have borrowed the book: book1");
  }

  @Test
  void testBorrowBookWhenUserInactive() {
    when(userService.isUserActive("user1")).thenReturn(false);

    boolean result = libraryManager.borrowBook("book1", "user1");

    assertFalse(result);
    verify(notificationService).notifyUser("user1", "Your account is not active.");
  }

  @Test
  void testBorrowBookWhenNoCopiesAvailable() {
    when(userService.isUserActive("user1")).thenReturn(true);
    libraryManager.addBook("book1", 0);

    boolean result = libraryManager.borrowBook("book1", "user1");

    assertFalse(result);
    assertEquals(0, libraryManager.getAvailableCopies("book1"));
  }

  @Test
  void testReturnBookSuccessfully() {
    when(userService.isUserActive("user1")).thenReturn(true);
    libraryManager.addBook("book1", 5);
    libraryManager.borrowBook("book1", "user1");

    boolean result = libraryManager.returnBook("book1", "user1");

    assertTrue(result);
    assertEquals(5, libraryManager.getAvailableCopies("book1"));
    verify(notificationService).notifyUser("user1", "You have returned the book: book1");
  }

  @Test
  void testReturnBookNotBorrowed() {
    boolean result = libraryManager.returnBook("book2", "user2");

    assertFalse(result);
  }

  @Test
  void testReturnBookNotBorrowedThisUser() {
    when(userService.isUserActive("user1")).thenReturn(true);
    libraryManager.addBook("book1", 5);
    libraryManager.borrowBook("book1", "user1");

    boolean result = libraryManager.returnBook("book1", "user2");

    assertFalse(result);
  }

  @Test
  void testCalculateDynamicLateFeeThrowsExceptionForNegativeDays() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, false, false)
    );

    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
      "0, 0, false, false", // просрочка 0 дней, задолженность 0
      "6, 3, false, false", // просрочка 6 дней, задолженность 3 (6*0.5)
      "6, 4.5, true, false", // просрочка 6 дней, задолженность 4.5 (6 * 0.5 * 1.5)
      "6, 2.4, false, true", // просрочка 6 дней, задолженность 2.4 (6 * 0.5 * 0.8)
      "6, 3.6, true, true" // просрочка 6 дней, задолженность 3.6 (6 * 0.5 * 1.5) * 0.8)
  })
  void testCalculateDynamicLateFee(int overdueDays, double expectedFee, boolean isBestseller, boolean isPremium) {
    double fee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremium);
    assertEquals(expectedFee, fee);
  }
}
