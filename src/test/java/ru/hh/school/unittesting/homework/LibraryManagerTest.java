package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class LibraryManagerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        libraryManager = new LibraryManager(notificationService, userService);
    }


    // Тесты для метода addBook ---> тесты показывают необходимость добавления логики валидации входных данных в методе;
    @Test
    @DisplayName("addBook тест позитивный: успешное добавление новой книги.")
    void testAddNewBook() {
        libraryManager.addBook("book1", 5);
        assertEquals(5, libraryManager.getAvailableCopies("book1"),
                "Добавляем 5 экземпляров новой книги 'book1', ожидаем количество экземпляров равное 5.");
    }

    @Test
    @DisplayName("addBook тест позитивный: на успешное изменение экземпляров существующей книги.")
    void testAddExistingBook() {
        libraryManager.addBook("book1", 3);
        assertEquals(3, libraryManager.getAvailableCopies("book1"),
                "Добавляем 3 экземпляров новой книги 'book1', ожидаем количество экземпляров равное 3.");

        libraryManager.addBook("book1", 2);
        assertEquals(5, libraryManager.getAvailableCopies("book1"),
                "После добавления 2 экземпляров существующей книги, ожидаем общее количество экземпляров равное 5.");

        libraryManager.addBook("book1", -4);
        assertEquals(1, libraryManager.getAvailableCopies("book1"),
                "После вычитания 4 экземпляров существующей книги, ожидаем общее количество экземпляров равное 1.");
    }

    @Test
    @DisplayName("addBook тест позитивный: на успешное добавление книги с количеством экземпляров равным нулю.")
    void testAddBookZeroCopies() {
        libraryManager.addBook("book1", 0);
        assertEquals(0, libraryManager.getAvailableCopies("book1"),
                "Добавляем 0 экземпляров новой книги, ожидаем количество экземпляров равное 0.");
    }

    @Test
    @DisplayName("addBook тест позитивный: на успешное добавление книги с bookId равным `null`.")
    void testAddBookNullId() {
        libraryManager.addBook(null, 1);
        assertEquals(1, libraryManager.getAvailableCopies(null),
                "Добавляем 1 экземпляров книги c bookId == `null`, ожидаем количество экземпляров равное 1.");

        libraryManager.addBook(null, 3);
        assertEquals(4, libraryManager.getAvailableCopies(null),
                "Добавляем дополнительно 3 экземпляра книги c bookId == `null`, ожидаем количество экземпляров равное 4.");
    }

    @Test
    @DisplayName("addBook тест позитивный: на успешное добавление книги `book1` с отрицательным количеством экземпляров.")
    void testAddBookNegativeQuantity() {
        libraryManager.addBook("book1", -1);
        assertEquals(-1, libraryManager.getAvailableCopies("book1"),
                "Добавляем `-1` экземпляров новой книги, ожидаем количество экземпляров равное `-1`.");

        libraryManager.addBook("book1", -3);
        assertEquals(-4, libraryManager.getAvailableCopies("book1"),
                "Добавляем дополнительно `-3` экземпляров существующей книги, ожидаем количество экземпляров равное `-4`.");
    }


    //Тесты для borrowBook ---> тесты показывают необходимость получения данным по книгам (активным / в аренде);
    @Test
    @DisplayName("borrowBook тест негативный: пользователь с неактивной учетной записью берёт книгу.")
    void testBorrowBookUserInactive() {
        String bookId = "book1";
        String userId = "user1";

        //задаём желаемое поведение сервиса
        when(userService.isUserActive(userId)).thenReturn(false);

        boolean result = libraryManager.borrowBook(bookId, userId);
        assertFalse(result, "Ожидаем что метод вернёт false для неактивного пользователя.");
        verify(notificationService).notifyUser(userId, "Your account is not active.");
    }

    @Test
    @DisplayName("borrowBook тест негативный: пользователь с активной записью, книга отсутствует в библиотеке.")
    void testBorrowBookNotAvailable() {
        String bookId = "book1"; // не добавлена в библиотеку
        String userId = "user1";

        when(userService.isUserActive(userId)).thenReturn(true);

        boolean result = libraryManager.borrowBook(bookId, userId);
        assertFalse(result, "Ожидаем что метод вернёт false для активного пользователя при отсутствии книги.");
    }

    @Test
    @DisplayName("borrowBook тест позитивный: успешное заимствование книги")
    void testBorrowBookSuccess() {
        String bookId = "book1";
        String userId = "user1";

        when(userService.isUserActive(userId)).thenReturn(true);
        libraryManager.addBook(bookId, 1); // добавляем книгу

        boolean result = libraryManager.borrowBook(bookId, userId);
        assertTrue(result, "Ожидаем что метод вернёт true для активного пользователя при наличии книги");
        verify(notificationService).notifyUser(userId, "You have borrowed the book: " + bookId);
    }

    @Test
    @DisplayName("borrowBook тест негативный: книга отсутствует в наличии для активного пользователя.")
    void testBorrowBookOutOfStock() {
        String bookId = "book1";
        String userId = "user1";

        when(userService.isUserActive(userId)).thenReturn(true);

        libraryManager.addBook(bookId, 0);
        boolean result = libraryManager.borrowBook(bookId, userId);
        assertFalse(result, "Метод должен вернуть false, если кол-во экземпляров книги нулевое.");

        libraryManager.addBook(bookId, -3);
        boolean resultSecond = libraryManager.borrowBook(bookId, userId);
        assertFalse(resultSecond, "Метод должен вернуть false, если кол-во книги отрицательное.");
    }

    //Тесты для returnBook:
    @Test
    @DisplayName("returnBook тест негативный: книга не была заимствована пользователем.")
    void testReturnBookNotBorrowed() {
        String bookId = "book1";
        String userId = "user1";

        boolean result = libraryManager.returnBook(bookId, userId);
        assertFalse(result, "Метод должен вернуть false, если книга и пользователь отсутствуют.");
    }

    @Test
    @DisplayName("returnBook тест негативный: книга была заимствована, но не этим пользователем")
    void testReturnBookNotBorrowedByUser() {
        String bookId = "book1";
        String userId1 = "user1";
        String userId2 = "user2";

        libraryManager.borrowBook(bookId, userId2);

        boolean result = libraryManager.returnBook(bookId, userId1);
        assertFalse(result, "Метод должен вернуть false, если книга была заимствована другим пользователем.");
    }

    @Test
    @DisplayName("returnBook тест позитивный: успешное возвращение книги")
    void testReturnBookSuccess() {
        String bookId = "book1";
        String userId = "user1";

        when(userService.isUserActive(userId)).thenReturn(true);

        // Добавляем книгу в инвентарь и заимствуем её
        libraryManager.addBook(bookId, 1);
        libraryManager.borrowBook(bookId, userId);

        boolean result = libraryManager.returnBook(bookId, userId);

        assertTrue(result, "Метод должен вернуть true при успешном возвращении книги.");
        assertEquals(1, libraryManager.getAvailableCopies(bookId),
                "Количество книг в наличии должно увеличиться на 1.");
        verify(notificationService).notifyUser(userId, "You have returned the book: " + bookId);
    }

    @Test
    @DisplayName("returnBook тест позитивный: изменение количества книг в инвентаре при возврате книги.")
    void testReturnBookIncreasesInventory() {
        String bookId = "book1";
        String userId = "user1";

        // Добавляем книгу в инвентарь и заимствуем её
        libraryManager.addBook(bookId, 10);
        libraryManager.borrowBook(bookId, userId);
        assertEquals(10, libraryManager.getAvailableCopies(bookId),
                "Количество книг должно остаться 10 , потому что пользователь неактивный.");

        when(userService.isUserActive(userId)).thenReturn(true);

        libraryManager.borrowBook(bookId, userId);
        assertEquals(9, libraryManager.getAvailableCopies(bookId),
                "Количество книг в инвентаре должно уменьшиться на 1 после того как активный пользователь одолжит книгу.");

        libraryManager.returnBook(bookId, userId);
        assertEquals(10, libraryManager.getAvailableCopies(bookId),
                "Количество книг в инвентаре должно увеличиться на 1 (стать 10) после возвращения.");
    }

    //Тесты для getAvailableCopies:
    @Test
    @DisplayName("getAvailableCopies тест негативный: получение количества экземпляров несуществующей книги.")
    void testGetAvailableCopiesNoCopies() {
        assertEquals(0, libraryManager.getAvailableCopies("book2"),
                "Ожидается что метод вернёт 0 доступных экземпляров книги, книги нет в наличии.");
    }

    @Test
    @DisplayName("getAvailableCopies тест позитивный: успешное получение количества экземпляров существующей книги.")
    void testGetAvailableCopiesAvailableCopies() {
        libraryManager.addBook("book3", 10);
        assertEquals(10, libraryManager.getAvailableCopies("book3"),
                "Ожидается что метод вернёт 10 экземпляров книги book3, книга в наличии  10 шт.");
        assertEquals(0, libraryManager.getAvailableCopies("book2"),
                "Ожидается что метод вернёт 0 экземпляров книги book2, книги нет в наличии."); // Проверка на несуществующую книгу
    }

    @Test
    @DisplayName("getAvailableCopies тест негативный: получение отрицательного или нулевого значение экземпляров существующей книги.")
    void testGetAvailableCopiesNegativeCopies() {
        libraryManager.addBook("book4", -5);
        libraryManager.addBook("book5", 0);
        assertEquals(-5, libraryManager.getAvailableCopies("book4"),
                "Ожидается что метод вернёт `-5` экземпляров книги book4, кол-во книги при добавлении `-5`");
        assertEquals(0, libraryManager.getAvailableCopies("book5"),
                "Ожидалось, что количество доступных экземпляров книги 'book5' будет равно 0.");
    }

    //Тесты для calculateDynamicLateFee
    @ParameterizedTest
    @CsvSource({
            "0, false, false, 0.00",    // Никаких просроченных дней
            "1, false, false, 0.50",    // Обычная книга за 1 день
            "1, true, false, 0.75",     // Бестселлер за 1 день
            "1, false, true, 0.40",     // Обычная книга с премиум скидкой за 1 день
            "1, true, true, 0.60",      // Бестселлер с премиум скидкой за 1 день
            "3, false, false, 1.50",    // Обычная книга за 3 дня
            "3, true, false, 2.25",     // Бестселлер за 3 дня
            "3, false, true, 1.20",     // Обычная книга с премиум скидкой за 3 дня
            "3, true, true, 1.80"       // Бестселлер с премиум скидкой за 3 дня
    })
    @DisplayName("calculateDynamicLateFee параметризованный тест: расчет динамического штрафа за просрочку")
    void testCalculateDynamicLateFeeParameterized(int overdueDays, boolean isBestseller, boolean isPremiumMember, double expectedFee) {

        double actualFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);

        assertEquals(expectedFee, actualFee,
                String.format("Расчет штрафа для %d дней просрочки (бестселлер: %b, премиум: %b). Ожидалось: %.2f, получено: %.2f",
                        overdueDays, isBestseller, isPremiumMember, expectedFee, actualFee));
    }

    @Test
    @DisplayName("calculateDynamicLateFee тест: негативные дни просрочки вызывают исключение")
    void testCalculateDynamicLateFeeNegativeOverdueDays() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            libraryManager.calculateDynamicLateFee(-1, false, false);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            libraryManager.calculateDynamicLateFee(-10, false, false);
        });

        assertEquals("Overdue days cannot be negative.", exception.getMessage());
    }

    @Test
    @DisplayName("calculateDynamicLateFee тест позитивный: обычная книга без скидок")
    void testCalculateDynamicLateFeeRegularBook() {
        double fee = libraryManager.calculateDynamicLateFee(5, false, false);
        assertEquals(2.50, fee, "Должен быть расчет обычной книги без скидок.");
    }

    @Test
    @DisplayName("calculateDynamicLateFee тест позитивный: книга-бестселлер без скидок")
    void testCalculateDynamicLateFeeBestseller() {
        double fee = libraryManager.calculateDynamicLateFee(5, true, false);
        assertEquals(3.75, fee, "Должен быть расчет бестселлера без скидок.");
    }

    @Test
    @DisplayName("calculateDynamicLateFee тест позитивный: обычная книга с премиум скидкой")
    void testCalculateDynamicLateFeePremiumMember() {
        double fee = libraryManager.calculateDynamicLateFee(5, false, true);
        assertEquals(2.00, fee, "Должен быть расчет обычной книги с премиум скидкой.");
    }

    @Test
    @DisplayName("calculateDynamicLateFee тест позитивный: бестселлер с премиум скидкой")
    void testCalculateDynamicLateFeeBestsellerWithPremiumDiscount() {
        double fee = libraryManager.calculateDynamicLateFee(5, true, true);
        assertEquals(3.00, fee, "Должен быть расчет бестселлера с премиум скидкой.");
    }
}
