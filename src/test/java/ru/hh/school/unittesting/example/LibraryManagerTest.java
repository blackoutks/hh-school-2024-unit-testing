package ru.hh.school.unittesting.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.homework.LibraryManager;
import ru.hh.school.unittesting.homework.NotificationService;
import ru.hh.school.unittesting.homework.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
