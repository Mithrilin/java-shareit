package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

//@SpringBootTest(
//        properties = "db.name=test",
//        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DataJpaTest
class BookingRepositoryTest {
    private static final String NAME_USER_ONE = "user1";
    private static final String NAME_USER_TWO = "user2";
    private static final String NAME_USER_THREE = "user3";
    private static final String EMAIL_USER_ONE = "user1@mail.ru";
    private static final String EMAIL_USER_TWO = "user2@mail.ru";
    private static final String EMAIL_USER_THREE = "user3@mail.ru";
    private static User userOne = null;
    private static User userTwo = null;
    private static User userThree = null;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        userOne = new User(NAME_USER_ONE, EMAIL_USER_ONE);
        userTwo = new User(NAME_USER_TWO, EMAIL_USER_TWO);
        userThree = new User(NAME_USER_THREE, EMAIL_USER_THREE);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Добавление фильма")
    void findByBooker_IdAndEndIsBefore_when() {
        long userOneId = userRepository.save(userOne).getId();
        long userTwoId = userRepository.save(userTwo).getId();
        long userThreeId = userRepository.save(userThree).getId();


    }
}