package uk.gov.di.ipv.cri.lime.limeade.testfixtures;

import org.mockito.ArgumentCaptor;
import software.amazon.cloudwatchlogs.emf.model.DimensionSet;
import software.amazon.cloudwatchlogs.emf.model.Unit;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetricsProbeTestHelper {
    /**
     * @param callNumber - which call are we comparing (zero based)
     * @param expectedMetricName - String
     * @param expectedDimensionSet - this objects count of keys and values will be compared
     * @param expectedValue - metric value normally 1 if using Count as the Unit
     * @param expectedUnit - AWS Unit type - typically Count
     */
    public static void metricsProbeArgumentVerifier(
            int callNumber,
            String expectedMetricName,
            DimensionSet expectedDimensionSet,
            double expectedValue,
            double expectedValueEpsilon,
            Unit expectedUnit,
            ArgumentCaptor<String> metricNameCaptor,
            ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor,
            ArgumentCaptor<Double> valueArgumentCaptor,
            ArgumentCaptor<Unit> unitArgumentCaptor) {

        assertEquals(expectedMetricName, metricNameCaptor.getAllValues().get(callNumber));
        assertExpectedDimensionSet(
                expectedDimensionSet,
                dimensionSetArgumentCaptor.getAllValues().get(callNumber),
                Collections.emptyList());
        assertEquals(
                expectedValue,
                valueArgumentCaptor.getAllValues().get(callNumber),
                expectedValueEpsilon);
        assertEquals(expectedUnit, unitArgumentCaptor.getAllValues().get(callNumber));
    }

    /**
     * @param callNumber - which call are we comparing (zero based)
     * @param expectedMetricName - String
     * @param expectedDimensionSet - this objects count of keys and values will be compared
     * @param expectedValue - metric value normally 1 if using Count as the Unit
     * @param expectedUnit - AWS Unit type - typlically Count
     */
    public static void metricsProbeArgumentVerifierWithIgnoredDimensionValues(
            int callNumber,
            String expectedMetricName,
            DimensionSet expectedDimensionSet,
            double expectedValue,
            double expectedValueEpsilon,
            Unit expectedUnit,
            ArgumentCaptor<String> metricNameCaptor,
            ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor,
            ArgumentCaptor<Double> valueArgumentCaptor,
            ArgumentCaptor<Unit> unitArgumentCaptor,
            List<String> dimensionToIgnoreValues) {

        System.out.println(metricNameCaptor.getAllValues().get(callNumber));
        assertEquals(expectedMetricName, metricNameCaptor.getAllValues().get(callNumber));
        assertExpectedDimensionSet(
                expectedDimensionSet,
                dimensionSetArgumentCaptor.getAllValues().get(callNumber),
                dimensionToIgnoreValues);
        assertEquals(
                expectedValue,
                valueArgumentCaptor.getAllValues().get(callNumber),
                expectedValueEpsilon);
        assertEquals(expectedUnit, unitArgumentCaptor.getAllValues().get(callNumber));
    }

    public static void assertExpectedDimensionSet(
            DimensionSet expectedDimensionSet,
            DimensionSet actualDimensionSet,
            List<String> dimensionToIgnoreValues) {
        // Keys match
        assertTrue(
                expectedDimensionSet
                        .getDimensionKeys()
                        .containsAll(actualDimensionSet.getDimensionKeys()));
        assertEquals(
                expectedDimensionSet.getDimensionKeys().size(),
                actualDimensionSet.getDimensionKeys().size());

        // Values Match
        expectedDimensionSet
                .getDimensionKeys()
                .forEach(
                        key -> {
                            System.out.println(
                                    key
                                            + ": e "
                                            + expectedDimensionSet.getDimensionValue(key)
                                            + " a "
                                            + actualDimensionSet.getDimensionValue(key));
                            if (!dimensionToIgnoreValues.contains(key)) {
                                assertEquals(
                                        expectedDimensionSet.getDimensionValue(key),
                                        actualDimensionSet.getDimensionValue(key));
                            }
                        });
    }
}
