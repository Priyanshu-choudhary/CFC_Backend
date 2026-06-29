package com.cfc.platform.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
@Service
public class CFToolsService {

    public Object importProblem(String url) {
        System.out.println("Received URL: " + url);

        try {
            int contestId = 0;
            String index = "";

            // This regex handles:
            // .../problemset/problem/2236/F1
            // .../contest/2239/problem/A
            // .../gym/105123/problem/B
            Pattern pattern = Pattern.compile("(?:problemset/problem|contest|gym)/(\\d+)(?:/problem)?/([A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                contestId = Integer.parseInt(matcher.group(1)); // Group 1 is the contest ID (e.g., 2236)
                index = matcher.group(2);                       // Group 2 is the index (e.g., F1)
       
            }
            // Fetch the HTML document
            // A User-Agent is sometimes required to prevent CF from blocking the request
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            // 1. Extract Title (Removing the "A. " or "F1. " prefix CF adds)
            Element titleEl = doc.selectFirst(".problem-statement .header .title");
            String title = titleEl != null ? titleEl.text().replaceAll("^([A-Z0-9]+\\.\\s)", "") : "Unknown Title";

            // 2. Extract Limits
            Element timeLimitEl = doc.selectFirst(".problem-statement .header .time-limit");
            double timeLimitSeconds = 2.0; // Default
            if (timeLimitEl != null) {
                // Extract numbers from "time limit per test 2.0 seconds"
                String tlText = timeLimitEl.ownText().replaceAll("[^0-9.]", "");
                if (!tlText.isEmpty()) timeLimitSeconds = Double.parseDouble(tlText);
            }

            Element memoryLimitEl = doc.selectFirst(".problem-statement .header .memory-limit");
            int memoryLimitKb = 262144; // Default 256MB
            if (memoryLimitEl != null) {
                // Extract numbers and convert Megabytes to Kilobytes
                String mlText = memoryLimitEl.ownText().replaceAll("[^0-9]", "");
                if (!mlText.isEmpty()) memoryLimitKb = Integer.parseInt(mlText) * 1024;
            }

            // 3. Extract Tags
            Elements tagElements = doc.select(".tag-box");
            List<String> tags = new ArrayList<>();
            for (Element tag : tagElements) {
                // Filter out the asterisk that CF sometimes appends to tags
                tags.add(tag.text().replace("*", "").trim());
            }

            // 4. Extract Description (Combines main body, input spec, and output spec)
            Element problemStatement = doc.selectFirst(".problem-statement");
            StringBuilder descriptionBuilder = new StringBuilder();

            if (problemStatement != null && problemStatement.children().size() > 1) {
                // We skip child 0 (the header) and append until we hit the sample tests
                for (int i = 1; i < problemStatement.children().size(); i++) {
                    Element child = problemStatement.child(i);
                    if (child.hasClass("sample-tests")) break;
                    descriptionBuilder.append(child.outerHtml());
                }
            }
            String description = descriptionBuilder.toString();

            // 5. Extract Testcases
            Map<String, String> testcases = new LinkedHashMap<>();
            Elements inputs = doc.select(".sample-test .input pre");
            Elements outputs = doc.select(".sample-test .output pre");

            for (int i = 0; i < inputs.size() && i < outputs.size(); i++) {
                String in = cleanCFPreTag(inputs.get(i));
                String out = cleanCFPreTag(outputs.get(i));
                testcases.put(in, out);
            }

            // 6. Build the Response Map
            // Returning a Map in a Spring @RestController automatically serializes it to JSON via Jackson
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("title", title);
            response.put("difficulty", ""); // CF uses rating numbers, so left empty
            response.put("type", "coding");
            response.put("contestId", contestId); // Added
            response.put("index", index);         // Added
            response.put("accuracy", "");
            response.put("tags", tags);
            response.put("companies", new ArrayList<>()); // Left empty
            response.put("constrain", ""); // In CF, constraints are usually embedded in the input description
            response.put("description", description);
            response.put("testcase", testcases);
            response.put("codeTemplates", new HashMap<>()); // Left empty
            response.put("timeLimitSeconds", timeLimitSeconds);
            response.put("memoryLimitKb", memoryLimitKb);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "message", "Failed to parse Codeforces URL: " + e.getMessage(),
                    "url", url
            );
        }
    }

    /**
     * Codeforces uses <br> tags and <div class="test-example-line"> inside their <pre> tags.
     * This helper ensures those are parsed properly into standard \n line breaks.
     */
    /**
     * Extracts raw text from Codeforces <pre> tags while strictly preserving newlines.
     */
    private String cleanCFPreTag(Element preElement) {
        String html = preElement.html();
        
        // Convert explicit break tags to newlines
        html = html.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n");
        
        // Convert div line wrappers to newlines (Codeforces uses these heavily in newer problems)
        html = html.replace("</div>", "\n</div>");
        
        // Strip the remaining HTML tags using regex to prevent Jsoup from collapsing our newlines
        String plainText = html.replaceAll("<[^>]*>", "");
        
        // Clean up multiple consecutive newlines and unescape HTML entities (e.g., &lt; to <)
        plainText = plainText.replaceAll("\\n{2,}", "\n");
        return org.jsoup.parser.Parser.unescapeEntities(plainText, false).trim();
    }
}