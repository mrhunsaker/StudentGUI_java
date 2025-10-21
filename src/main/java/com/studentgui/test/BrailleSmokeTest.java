package com.studentgui.test;

/**
 * Legacy smoke main retained for reference. Converted to a no-op deprecated
 * holder to avoid duplicate Javadoc warnings now that an equivalent JUnit
 * test exists under src/test/java.
 *
 * @deprecated Use {@code src/test/java/com/studentgui/test/BrailleSmokeTest.java}
 *             (the JUnit 5 replacement) for automated smoke testing.
 */
@Deprecated
public final class BrailleSmokeTest {
    // intentionally empty - preserved for historical reference

    /**
     * Private constructor to prevent instantiation of this utility holder.
     * The real smoke test has been converted to a JUnit test under src/test.
     */
    private BrailleSmokeTest() {
        throw new AssertionError("Not instantiable");
    }
}
