package com.cfc.platform.Service;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over a code-execution backend.
 *
 * Two implementations exist and are selected at startup via the
 * {@code code.execution.provider} property:
 *
 * <ul>
 *   <li>{@code judge0}  -> {@link Judge0Service} (RapidAPI Judge0 CE)</li>
 *   <li>{@code goboxd}  -> {@link GoboxdService} (self-hosted nsjail sandbox)</li>
 * </ul>
 *
 * Both implementations return identical Map shapes so the controller and the
 * frontend never need to know which engine ran the code.
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
