package com.cfc.platform.service;

import com.cfc.platform.Service.CFToolsService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CFToolsServiceTest {

    @Test
    void categorizesDifficultyFromCodeforcesRating() {
        assertEquals("Easy", CFToolsService.difficultyForRating(800));
        assertEquals("Easy", CFToolsService.difficultyForRating(1200));
        assertEquals("Medium", CFToolsService.difficultyForRating(1300));
        assertEquals("Medium", CFToolsService.difficultyForRating(1800));
        assertEquals("Hard", CFToolsService.difficultyForRating(1900));
        assertEquals("Hard", CFToolsService.difficultyForRating(3500));
    }

    @Test
    void usesMediumWhenCodeforcesDoesNotPublishARating() {
        assertEquals("Medium", CFToolsService.difficultyForRating(null));
    }

    @Test
    void preservesExamplesAndNotesAsPortableHtml() {
        Document document = Jsoup.parse("""
                <div class='problem-statement'>
                  <div class='header'><div class='title'>A. Demo</div></div>
                  <div class='input-specification'><div class='section-title'>Input</div><p>Read n.</p></div>
                  <div class='output-specification'><div class='section-title'>Output</div><p>Print n.</p></div>
                  <div class='sample-tests'><div class='section-title'>Examples</div>
                    <div class='sample-test'><div class='input'><div class='title'>Input</div><pre><div>2</div><div>3</div></pre></div>
                    <div class='output'><div class='title'>Output</div><pre>5</pre></div></div>
                  </div>
                  <div class='note'><div class='section-title'>Note</div><p>This explains the sample.</p></div>
                </div>
                """);
        Element statement = document.selectFirst(".problem-statement");

        String html = CFToolsService.normalizeProblemDescription(statement);
        Document normalized = Jsoup.parseBodyFragment(html);

        assertFalse(html.contains("class=\"header\""));
        assertEquals("Examples", normalized.selectFirst(".sample-tests > h3").text());
        assertEquals("Note", normalized.selectFirst(".note > h3").text());
        assertEquals("Input", normalized.selectFirst(".sample-test .input > h4").text());
        assertEquals("2\n3", normalized.selectFirst(".sample-test .input pre").wholeText());
        assertTrue(html.contains("This explains the sample."));
    }
}
