package com.studentgui.apphelpers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SqlGenerateTest {

    @Test
    public void testInitializeCreatesContactLogTable() throws Exception {
        SqlGenerate.initializeDatabase();
        Path db = Helpers.DATABASE_PATH;
        assertTrue(Files.exists(db));
        String url = "jdbc:sqlite:" + db.toString();
        try (Connection c = DriverManager.getConnection(url)) {
            try (Statement st = c.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='ContactLog'");
                assertTrue(rs.next(), "ContactLog table should exist after initialization");
            }
        }
    }
}
