package com.cfc.platform.Service;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over a code-execution backend.
 *
 * Only one implementation currently exists — {@link GoboxdService}, which
 * proxies to the self-hosted goboxd ECS service (nsjail sandbox).  The
 * interface is kept so we can swap engines later (e.g. WASM, Firecracker)
 * without changing callers.
 *
 * Judge0Service was removed once goboxd reached parity; see git history if you
 * need the old RapidAPI implementation back.
 */
public interface CodeExecutionService {

    /**
     * Playground run with no expected-output check.
     *
     * @return a map containing at least: stdout, stderr, compile_output,
     *         statusDescription, statusId, time, memory.
     */
    Map<String, Object> runCode(String sourceCode, String language, String stdin);

    /**
     * Run the source against a set of test cases.
     *
     * @param testCases ordered map of stdin -> expectedOutput
     * @return a map containing: allPassed, totalTestCases, passedCount,
     *         results (list of per-test maps).
     */
    Map<String, Object> submitWithTestCases(
            String sourceCode,
            String language,
            Map<String, String> testCases,
            Double timeLimitSeconds,
            Integer memoryLimitKb);

    /** @return the languages this provider supports. */
    List<Map<String, Object>> getLanguages();

    /** @return the provider id, e.g. "judge0" or "goboxd". */
    String providerName();
}
