package net.fuxle.awooapi.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserIdentificationUtilTest {

	@Test
	void testGetRequestedUserIdWithAtMe() {
		int selfUserId = 42;
		String input = "@me";

		int result = UserIdentificationUtil.getRequestedUserId(input, selfUserId);

		assertEquals(selfUserId, result, "Should return selfUserId when input is '@me'");
	}

	@Test
	void testGetRequestedUserIdWithValidInteger() {
		int selfUserId = 42;
		String input = "123";

		int result = UserIdentificationUtil.getRequestedUserId(input, selfUserId);

		assertEquals(123, result, "Should return the integer value parsed from the input string");
	}

	@Test
	void testGetRequestedUserIdWithNullInput() {
		int selfUserId = 42;

		assertThrows(NullPointerException.class, () ->
				UserIdentificationUtil.getRequestedUserId(null, selfUserId),
			"Should throw NullPointerException when input is null");
	}

	@Test
	void testGetRequestedUserIdWithInvalidInteger() {
		int selfUserId = 42;
		String input = "invalid";

		assertThrows(NumberFormatException.class, () ->
				UserIdentificationUtil.getRequestedUserId(input, selfUserId),
			"Should throw NumberFormatException when input cannot be parsed as an integer");
	}

	@Test
	void testGetRequestedUserIdWithEmptyString() {
		int selfUserId = 42;
		String input = "";

		assertThrows(NumberFormatException.class, () ->
				UserIdentificationUtil.getRequestedUserId(input, selfUserId),
			"Should throw NumberFormatException when input is an empty string");
	}

	@Test
	void testGetRequestedUserIdWithNegativeInteger() {
		int selfUserId = 42;
		String input = "-5";

		int result = UserIdentificationUtil.getRequestedUserId(input, selfUserId);

		assertEquals(-5, result, "Should return the parsed negative integer from the input string");
	}
}
