package com.api.wishoria.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {

    private final EmailValidator emailValidator = new EmailValidator();

    @Test
    void test_shouldReturnTrue_forValidEmails() {
        assertThat(emailValidator.test("user@example.com")).isTrue();
        assertThat(emailValidator.test("user.name@example.com")).isTrue();
        assertThat(emailValidator.test("user+tag@example.co.uk")).isTrue();
        assertThat(emailValidator.test("a@b.co")).isTrue();
        assertThat(emailValidator.test("user_name@domain.org")).isTrue();
    }

    @Test
    void test_shouldReturnFalse_forNull() {
        assertThat(emailValidator.test(null)).isFalse();
    }

    @Test
    void test_shouldReturnFalse_forEmptyString() {
        assertThat(emailValidator.test("")).isFalse();
    }

    @Test
    void test_shouldReturnFalse_forInvalidEmails() {
        assertThat(emailValidator.test("no-at-sign")).isFalse();
        assertThat(emailValidator.test("@nodomain.com")).isFalse();
        assertThat(emailValidator.test("nodomain@")).isFalse();
        assertThat(emailValidator.test("spaces in@email.com")).isFalse();
        assertThat(emailValidator.test("missing-tld@domain")).isFalse();
    }
}
