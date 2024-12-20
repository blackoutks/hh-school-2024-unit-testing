package ru.hh.school.unittesting.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.homework.LibraryManager;
import ru.hh.school.unittesting.homework.NotificationService;
import ru.hh.school.unittesting.homework.UserService;
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
  void testCalculateDynamicLateFeeThrowsExceptionForNegativeDays() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, false, false)
    );

    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @Test
  void testCalculateDynamicLateFeeWithoutPremiumOrBestseller() {
    double fee = libraryManager.calculateDynamicLateFee(6, false, false);
    assertEquals(3, fee);
  }

  @Test
  void testCalculateDynamicLateFeeWithBestseller() {
    double fee = libraryManager.calculateDynamicLateFee(6, true, false);
    assertEquals(4.5, fee); // 6 * 0.5 * 1.5
  }

  @Test
  void testCalculateDynamicLateFeeWithPremiumMember() {
    double fee = libraryManager.calculateDynamicLateFee(6, false, true);
    assertEquals(2.4, fee); // 6 * 0.5 * 0.8
  }

  @Test
  void testCalculateDynamicLateFeeWithBestsellerAndPremiumMember() {
    double fee = libraryManager.calculateDynamicLateFee(6, true, true);
    assertEquals(3.6, fee); // (6 * 0.5 * 1.5) * 0.8
  }
}
