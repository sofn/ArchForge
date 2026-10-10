package com.lesofn.archforge.cli.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetaCommandTest {

    /**
     * The one-shot sync process boots the admin application against the same database as a running server: it must
     * not serve HTTP, and it must not become a second db-scheduler node that picks up (and on shutdown waits for)
     * due jobs.
     */
    @Test
    void theSyncProcessIsNeitherAWebServerNorASchedulerNode() {
        List<String> args = MetaCommand.syncArgs("check", Path.of("project-definition", "meta"), new MetaCommand.SyncOptions(),
                false);

        assertTrue(args.contains("--spring.main.web-application-type=none"), args.toString());
        assertTrue(args.contains("--arch-forge.scheduler.enabled=false"), args.toString());
        assertTrue(args.contains("--arch-forge.meta.sync.mode=check"), args.toString());
    }
}
