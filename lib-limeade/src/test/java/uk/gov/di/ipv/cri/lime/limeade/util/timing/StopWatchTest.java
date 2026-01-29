package uk.gov.di.ipv.cri.lime.limeade.util.timing;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StopWatchTest {

    private final StopWatch stopWatch = new StopWatch();

    @ParameterizedTest
    @CsvSource({
        "1000", "3000", "5000",
    })
    void shouldCaptureTimeForMilliseconds(long milliseconds) {

        stopWatch.start();

        long timeToWait = System.currentTimeMillis() + milliseconds;
        while (System.currentTimeMillis() < timeToWait) {
            // Do nothing - to avoid thread sleep
        }

        long result = stopWatch.stop();

        assertEquals(milliseconds, result, 500);
    }
}
