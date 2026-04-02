package ym.signLock.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LockBatchTargetParserTest {

    private final LockBatchTargetParser parser = new LockBatchTargetParser();

    @Test
    void parseSupportsLegacySingleTargetAndMixedSeparators() {
        assertEquals(List.of("Alice"), parser.parse("Alice"));
        assertEquals(List.of("Alice", "Bob", "Charlie"), parser.parse("Alice Bob,Charlie"));
    }

    @Test
    void parseDeduplicatesTargetsWhilePreservingFirstSeenOrder() {
        assertEquals(
                List.of("Alice", "bob", "CHARLIE"),
                parser.parse(" Alice, bob Alice  CHARLIE,charlie Bob ")
        );
    }
}
