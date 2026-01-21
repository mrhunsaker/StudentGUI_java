package com.studentgui.apppages;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

/**
 * Small unit test to validate deterministic jitter reproducibility.
 * The test uses reflection to invoke the private addJitter(double) helper
 * on two separate JLineGraph instances configured with the same seed and
 * deterministic mode. The produced sequences must match exactly.
 */
public class JLineGraphDeterministicJitterTest {

    @Test
    /**
     * deterministicJitterProducesSameSequence - TODO: describe this method
     */

    public void deterministicJitterProducesSameSequence() throws Exception {
        JLineGraph g1 = new JLineGraph();
        JLineGraph g2 = new JLineGraph();

        g1.setJitterDeterministic(true);
        g2.setJitterDeterministic(true);
        g1.setJitterSeed(123456789L);
        g2.setJitterSeed(123456789L);

        Method addJitter = JLineGraph.class.getDeclaredMethod("addJitter", double.class);
        addJitter.setAccessible(true);

        final int N = 10;
        double[] seq1 = new double[N];
        double[] seq2 = new double[N];

        double base = 2.0;
        for (int i = 0; i < N; i++) {
            seq1[i] = (double) addJitter.invoke(g1, base);
            seq2[i] = (double) addJitter.invoke(g2, base);
        }

        // sequences must match exactly when using same seed
        assertArrayEquals(seq1, seq2, 0.0);
    }
}
