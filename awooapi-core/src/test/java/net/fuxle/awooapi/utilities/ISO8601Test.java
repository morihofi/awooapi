package net.fuxle.awooapi.utilities;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

class ISO8601Test {

	@Test
	void testFromCalendar() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(2023, Calendar.MARCH, 1, 13, 0, 0);
		String result = ISO8601.fromCalendar(calendar);

		assertTrue(result.startsWith("2023-03-01T13:00:00"), "Should format calendar to ISO 8601 format");
	}

	@Test
	void testNow() {
		String now = ISO8601.now();
		assertNotNull(now, "Should return the current date and time as ISO 8601 string");
	}

	@Test
	void testToCalendarValidInput() throws ParseException {
		String iso8601 = "2023-03-01T13:00:00+01:00";
		Calendar calendar = ISO8601.toCalendar(iso8601);

		assertEquals(2023, calendar.get(Calendar.YEAR));
		assertEquals(Calendar.MARCH, calendar.get(Calendar.MONTH));
		assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	void testToCalendarInvalidInput() {
		String invalidInput = "Invalid String";
		assertThrows(ParseException.class, () -> ISO8601.toCalendar(invalidInput),
			"Should throw ParseException for invalid ISO 8601 string");
	}

	@Test
	void testFromLocalDate() {
		LocalDate date = LocalDate.of(2023, 3, 1);
		String result = ISO8601.fromLocalDate(date);

		assertEquals("2023-03-01", result, "Should format LocalDate to ISO 8601 date format");
	}

	@Test
	void testToLocalDate() {
		String iso8601 = "2023-03-01";
		LocalDate date = ISO8601.toLocalDate(iso8601);

		assertEquals(LocalDate.of(2023, 3, 1), date, "Should parse ISO 8601 date string to LocalDate");
	}

	@Test
	void testFromLocalDateTime() {
		LocalDateTime dateTime = LocalDateTime.of(2023, 3, 1, 13, 0);
		String result = ISO8601.fromLocalDateTime(dateTime);

		assertEquals("2023-03-01T13:00:00", result, "Should format LocalDateTime to ISO 8601 date-time format");
	}

	@Test
	void testToLocalDateTime() {
		String iso8601 = "2023-03-01T13:00:00"; // Provide full datetime
		LocalDateTime dateTime = ISO8601.toLocalDateTime(iso8601);

		assertEquals(LocalDateTime.of(2023, 3, 1, 13, 0), dateTime,
			"Should parse ISO 8601 date-time string to LocalDateTime");
	}


	@Test
	void testFromLocalTime() {
		LocalTime time = LocalTime.of(13, 0);
		String result = ISO8601.fromLocalTime(time);

		assertEquals("13:00:00", result, "Should format LocalTime to ISO 8601 time format");
	}

	@Test
	void testToLocalTime() {
		String iso8601 = "13:00:00";
		LocalTime time = ISO8601.toLocalTime(iso8601);

		assertEquals(LocalTime.of(13, 0), time, "Should parse ISO 8601 time string to LocalTime");
	}

	@Test
	void testToZonedDateTime() {
		String iso8601 = "2023-03-01T13:00:00+01:00";
		ZonedDateTime zonedDateTime = ISO8601.toZonedDateTime(iso8601);

		assertEquals(2023, zonedDateTime.getYear(), "Should parse year correctly");
		assertEquals(3, zonedDateTime.getMonthValue(), "Should parse month correctly");
		assertEquals(1, zonedDateTime.getDayOfMonth(), "Should parse day correctly");
		assertEquals(13, zonedDateTime.getHour(), "Should parse hour correctly");
	}
}
