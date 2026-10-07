package com.lesofn.archforge.cli.proc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/** Test double: records every external command instead of running it; chosen commands "fail". */
public class RecordingProcessRunner extends ProcessRunner {

    public record Call(List<String> command, Map<String, String> env) {
    }

    public final List<Call> calls = new ArrayList<>();
    private final Predicate<List<String>> fails;
    private final int failureCode;

    public RecordingProcessRunner(Predicate<List<String>> fails, int failureCode) {
        this.fails = fails;
        this.failureCode = failureCode;
    }

    public static RecordingProcessRunner succeeding() {
        return new RecordingProcessRunner(command -> false, 0);
    }

    @Override
    public int run(List<String> command, Path workingDir, Map<String, String> extraEnv, boolean inheritIo,
            @Nullable Path stdoutFile, @Nullable Path stdinFile) {
        calls.add(new Call(List.copyOf(command), Map.copyOf(extraEnv)));
        return fails.test(command) ? failureCode : 0;
    }

    public boolean ran(String fragment) {
        return calls.stream().anyMatch(call -> String.join(" ", call.command()).contains(fragment));
    }
}
