package net.fuxle.awooapi.autodiscovery.loader;

import net.fuxle.awooapi.exceptions.AwooApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoaderHelperTest {

    @Test
    void validatePath_throwsException_whenPathIsEmpty() {
        AwooApiException exception = assertThrows(AwooApiException.class, () -> {
            LoaderHelper.validatePath("");
        });
        assertEquals("API Path cannot be empty", exception.getMessage());
    }

    @Test
    void validatePath_throwsException_whenPathDoesNotStartWithSlash() {
        AwooApiException exception = assertThrows(AwooApiException.class, () -> {
            LoaderHelper.validatePath("path/without/slash");
        });
        assertEquals("API Path must start with a \"/\"", exception.getMessage());
    }

    @Test
    void validatePath_validatesSuccessfully_whenPathIsValid() {
        assertDoesNotThrow(() -> LoaderHelper.validatePath("/valid/path"));
    }

    @Test
    void constructPath_constructsCorrectPath_withApiVersion() {
        String result = LoaderHelper.constructPath("/api", "v1", "/endpoint");
        assertEquals("/api/v1/endpoint", result);
    }

    @Test
    void constructPath_constructsCorrectPath_withoutApiVersion() {
        String result = LoaderHelper.constructPath("/api", "", "/endpoint");
        assertEquals("/api/endpoint", result);
    }
}
