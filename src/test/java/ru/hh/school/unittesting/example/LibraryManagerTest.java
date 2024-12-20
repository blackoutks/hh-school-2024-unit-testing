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

}
