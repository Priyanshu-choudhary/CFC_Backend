package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.Pojo.Posts.Posts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AtCoderToolsService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CFC-Importer/1.0";
    private static final Pattern TASK_URL_PATTERN = Pattern.compile(
            "atcoder\\.jp/contests/([^/?#]+)/tasks/([^/?#]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTEST_URL_PATTERN = Pattern.compile(
            "atcoder\\.jp/contests/([^/?#]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAMPLE_HEADING_PATTERN = Pattern.compile(
            "Sample (Input|Output)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter ATCODER_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");

    private final PostRepo postRepo;
    private final PostService postService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AtCoderToolsService(PostRepo postRepo, PostService postService, UserService userService, ObjectMapper objectMapper) {
        this.postRepo = postRepo;
        this.postService = postService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public boolean supports(String url) {
        return url != null && url.toLowerCase(Locale.ROOT).contains("atcoder.jp/");
    }

    public Map<String, Object> importProblem(String url) {
        TaskLocator locator = extractTaskLocator(url);
        String canonicalUrl = "https://atcoder.jp/contests/" + locator.contestKey()
                + "/tasks/" + locator.taskKey() + "?lang=en";

        try {
            Document document = Jsoup.connect(canonicalUrl).userAgent(USER_AGENT).timeout(20_000).get();
            Element titleElement = document.selectFirst("span.h2");
            String rawTitle = titleElement == null ? locator.taskKey() : titleElement.ownText().trim();
            String title = rawTitle.replaceFirst("^[A-Z0-9]+\\s*-\\s*", "");

            String limitText = Optional.ofNullable(document.selectFirst("p:contains(Time Limit)"))
                    .map(Element::text)
                    .orElse(document.text());
            double timeLimitSeconds = parseDouble(limitText, "Time Limit:\\s*([0-9.]+)\\s*sec", 2.0);
            int memoryMiB = (int) parseDouble(limitText, "Memory Limit:\\s*([0-9.]+)\\s*MiB", 1024);

            Element english = document.selectFirst("#task-statement .lang-en");
            if (english == null) {
                throw new IllegalArgumentException("English problem statement is unavailable.");
            }

            Map<Integer, String> sampleInputs = new LinkedHashMap<>();
            Map<Integer, String> sampleOutputs = new LinkedHashMap<>();

            for (Element heading : english.select("h3")) {
                Matcher sampleMatcher = SAMPLE_HEADING_PATTERN.matcher(heading.text().trim());
                if (!sampleMatcher.matches()) continue;
                Element pre = heading.nextElementSibling();
                if (pre != null && "pre".equals(pre.tagName())) {
                    int number = Integer.parseInt(sampleMatcher.group(2));
                    if ("input".equalsIgnoreCase(sampleMatcher.group(1))) {
                        sampleInputs.put(number, pre.wholeText().trim());
                    } else {
                        sampleOutputs.put(number, pre.wholeText().trim());
                    }
                }
            }

            Element descriptionElement = english.clone();
            for (Element heading : new ArrayList<>(descriptionElement.select("h3"))) {
                if (!SAMPLE_HEADING_PATTERN.matcher(heading.text().trim()).matches()) continue;
                Element section = heading.parent();
                if (section != null && "section".equals(section.tagName())) section.remove();
                else {
                    Element pre = heading.nextElementSibling();
                    if (pre != null && "pre".equals(pre.tagName())) pre.remove();
                    heading.remove();
                }
            }

            Map<String, String> testcases = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> entry : sampleInputs.entrySet()) {
                String output = sampleOutputs.get(entry.getKey());
                if (output != null) testcases.put(entry.getValue(), output);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("title", title);
            response.put("difficulty", "");
            response.put("type", "coding");
            response.put("contestId", 0);
            response.put("index", locator.taskKey());
            response.put("accuracy", "");
            response.put("tags", new ArrayList<>());
            response.put("companies", new ArrayList<>());
            response.put("constrain", "");
            response.put("description", descriptionElement.html());
            response.put("testcase", testcases);
            response.put("codeTemplates", new HashMap<>());
            response.put("timeLimitSeconds", timeLimitSeconds);
            response.put("memoryLimitKb", memoryMiB * 1024);
            return response;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse AtCoder problem: " + exception.getMessage(), exception);
        }
    }

    public Posts importAndSaveProblem(String url, String username, boolean reuseExisting) {
        Posts post = objectMapper.convertValue(importProblem(url), Posts.class);
        Optional<Posts> existing = postRepo.findFirstByContestIdAndIndex(0, post.getIndex());
        if (existing.isEmpty()) {
            existing = postRepo.findFirstByTitleIgnoreCase(post.getTitle());
        }
        if (existing.isPresent()) {
            if (reuseExisting) return existing.get();
            throw new IllegalStateException("Problem already exists in your database");
        }

        post.setUserName(username);
        post.setLastModified(new Date());
        userService.setLastdate(username);
        postService.createPost(post, username);
        return post;
    }

    public AtCoderContestData importContestMetadata(String url) {
        String contestKey = extractContestKey(url);
        String canonicalUrl = "https://atcoder.jp/contests/" + contestKey;
        try {
            Document contestDocument = Jsoup.connect(canonicalUrl + "?lang=en")
                    .userAgent(USER_AGENT).timeout(20_000).get();
            Element titleElement = contestDocument.selectFirst("span.h2");
            String name = titleElement == null
                    ? contestDocument.title().replace(" - AtCoder", "").trim()
                    : titleElement.text().trim();

            Elements timeElements = contestDocument.select(".contest-duration time");
            if (timeElements.size() < 2) {
                throw new IllegalArgumentException("Could not read the contest start/end time.");
            }
            Date start = parseDate(timeElements.get(0).text());
            Date end = parseDate(timeElements.get(1).text());
            long durationSeconds = Math.max(60, Duration.ofMillis(end.getTime() - start.getTime()).toSeconds());

            Document tasksDocument = Jsoup.connect(canonicalUrl + "/tasks?lang=en")
                    .userAgent(USER_AGENT).timeout(20_000).get();
            List<AtCoderProblemRef> problems = new ArrayList<>();
            for (Element row : tasksDocument.select("tr:has(a[href*='/tasks/'])")) {
                Elements links = row.select("a[href*='/tasks/']");
                if (links.isEmpty()) continue;
                Element indexLink = links.get(0);
                Element nameLink = links.size() > 1 ? links.get(1) : indexLink;
                String problemUrl = indexLink.absUrl("href");
                Matcher taskMatcher = TASK_URL_PATTERN.matcher(problemUrl);
                if (!taskMatcher.find()) continue;
                problems.add(new AtCoderProblemRef(indexLink.text().trim(), nameLink.text().trim(), problemUrl));
            }
            if (problems.isEmpty()) throw new IllegalArgumentException("No contest tasks were found.");

            Date now = new Date();
            String phase = now.before(start) ? "BEFORE" : now.before(end) ? "CODING" : "FINISHED";
            return new AtCoderContestData(contestKey, name, phase, durationSeconds, start, end, canonicalUrl, problems);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse AtCoder contest: " + exception.getMessage(), exception);
        }
    }

    private TaskLocator extractTaskLocator(String url) {
        Matcher matcher = TASK_URL_PATTERN.matcher(String.valueOf(url));
        if (!matcher.find()) throw new IllegalArgumentException("Unsupported AtCoder problem URL.");
        return new TaskLocator(matcher.group(1), matcher.group(2));
    }

    private String extractContestKey(String url) {
        Matcher matcher = CONTEST_URL_PATTERN.matcher(String.valueOf(url));
        if (!matcher.find()) throw new IllegalArgumentException("Unsupported AtCoder contest URL.");
        return matcher.group(1);
    }

    private double parseDouble(String input, String regex, double fallback) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : fallback;
    }

    private Date parseDate(String value) {
        return Date.from(OffsetDateTime.parse(value.trim(), ATCODER_TIME).toInstant());
    }

    private record TaskLocator(String contestKey, String taskKey) {}

    public record AtCoderProblemRef(String index, String name, String url) {}

    public record AtCoderContestData(
            String contestKey,
            String name,
            String phase,
            long durationSeconds,
            Date officialStart,
            Date officialEnd,
            String canonicalContestUrl,
            List<AtCoderProblemRef> problems) {}
}
