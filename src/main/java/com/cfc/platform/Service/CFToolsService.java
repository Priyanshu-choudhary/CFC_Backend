package com.cfc.platform.Service;

import com.cfc.platform.DTO.ContestLocator;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.Pojo.Posts.Posts;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CFToolsService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final Pattern PROBLEM_URL_PATTERN = Pattern.compile(
            "(?:problemset/problem|contest|gym)/(\\d+)(?:/problem)?/([A-Z0-9]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTEST_URL_PATTERN = Pattern.compile(
            "(?:contestRegistration|contest|gym)/(\\d+)",
            Pattern.CASE_INSENSITIVE);
    private final Map<Integer, String> editorialUrlCache = new ConcurrentHashMap<>();

    @Autowired
    private PostRepo postRepo;
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> importProblem(String url) {
        ProblemLocator locator = extractProblemLocator(url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .get();

            Element titleEl = doc.selectFirst(".problem-statement .header .title");
            String problemName = titleEl != null
                    ? titleEl.text().replaceAll("^([A-Z0-9]+\\.\\s)", "")
                    : "Unknown Title";
            String title = problemName + " " + locator.contestId() + "-" + locator.index();

            Element timeLimitEl = doc.selectFirst(".problem-statement .header .time-limit");
            double timeLimitSeconds = 2.0;
            if (timeLimitEl != null) {
                String tlText = timeLimitEl.ownText().replaceAll("[^0-9.]", "");
                if (!tlText.isEmpty()) {
                    timeLimitSeconds = Double.parseDouble(tlText);
                }
            }

            Element memoryLimitEl = doc.selectFirst(".problem-statement .header .memory-limit");
            int memoryLimitKb = 262144;
            if (memoryLimitEl != null) {
                String mlText = memoryLimitEl.ownText().replaceAll("[^0-9]", "");
                if (!mlText.isEmpty()) {
                    memoryLimitKb = Integer.parseInt(mlText) * 1024;
                }
            }

            Elements tagElements = doc.select(".tag-box");
            List<String> tags = new ArrayList<>();
            Integer rating = null;
            for (Element tag : tagElements) {
                String tagText = tag.text().trim();
                Matcher ratingMatcher = Pattern.compile("^\\*(\\d{3,4})$").matcher(tagText);
                if (ratingMatcher.matches()) {
                    rating = Integer.parseInt(ratingMatcher.group(1));
                } else if (!tagText.isBlank()) {
                    tags.add(tagText.replace("*", "").trim());
                }
            }
            if (rating != null) tags.add("rating:" + rating);

            String editorialUrl = findEditorialUrl(doc);
            if (editorialUrl.isBlank()) {
                editorialUrl = editorialUrlCache.get(locator.contestId());
                if (editorialUrl == null) {
                    try {
                        Document contestDocument = Jsoup.connect(
                                        "https://codeforces.com/contest/" + locator.contestId())
                                .userAgent(USER_AGENT)
                                .timeout(15_000)
                                .get();
                        editorialUrl = findEditorialUrl(contestDocument);
                    } catch (Exception ignored) {
                        editorialUrl = "";
                        // Some contests do not publish an editorial or may temporarily block
                        // the secondary request. Problem import should still succeed.
                    }
                    editorialUrlCache.put(locator.contestId(), editorialUrl);
                }
            }

            Element problemStatement = doc.selectFirst(".problem-statement");
            String description = "";
            if (problemStatement != null) {
                description = normalizeProblemDescription(problemStatement);
            }

            Map<String, String> testcases = new LinkedHashMap<>();
            Elements inputs = doc.select(".sample-test .input pre");
            Elements outputs = doc.select(".sample-test .output pre");
            for (int i = 0; i < inputs.size() && i < outputs.size(); i++) {
                String in = cleanCFPreTag(inputs.get(i));
                String out = cleanCFPreTag(outputs.get(i));
                testcases.put(in, out);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("title", title);
            response.put("difficulty", difficultyForRating(rating));
            response.put("type", "coding");
            response.put("contestId", locator.contestId());
            response.put("index", locator.index());
            response.put("accuracy", "");
            response.put("tags", tags);
            response.put("companies", new ArrayList<>());
            response.put("constrain", "");
            response.put("description", description);
            response.put("testcase", testcases);
            response.put("codeTemplates", new HashMap<>());
            response.put("timeLimitSeconds", timeLimitSeconds);
            response.put("memoryLimitKb", memoryLimitKb);
            response.put("editorialUrl", editorialUrl);
            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Codeforces URL: " + e.getMessage(), e);
        }
    }

    public static String difficultyForRating(Integer rating) {
        if (rating == null) return "Medium";
        if (rating <= 1200) return "Easy";
        if (rating <= 1800) return "Medium";
        return "Hard";
    }

    public static String normalizeProblemDescription(Element problemStatement) {
        Element statementContent = problemStatement.clone();
        statementContent.select(".header").remove();

        // Codeforces headings are divs styled by its own stylesheet. Semantic tags
        // keep Input, Output, Examples and Note visible without Codeforces CSS.
        for (Element heading : statementContent.select(".section-title")) {
            heading.tagName("h3");
        }
        for (Element heading : statementContent.select(".sample-test .title")) {
            heading.tagName("h4");
        }
        for (Element pre : statementContent.select(".sample-test pre")) {
            pre.text(cleanCFPreTag(pre));
        }

        for (Element element : statementContent.select("[href]")) {
            String absoluteUrl = element.absUrl("href");
            if (!absoluteUrl.isBlank()) element.attr("href", absoluteUrl);
        }
        for (Element element : statementContent.select("[src]")) {
            String absoluteUrl = element.absUrl("src");
            if (!absoluteUrl.isBlank()) element.attr("src", absoluteUrl);
        }
        return statementContent.html();
    }

    private static String findEditorialUrl(Document document) {
        for (Element anchor : document.select("a[href*='/blog/entry/']")) {
            String label = anchor.text().toLowerCase(Locale.ROOT);
            if (label.contains("editorial") || label.contains("tutorial")) {
                return anchor.absUrl("href");
            }
        }
        return "";
    }


    public Posts importAndSaveProblem(String url, String username) {
        return importAndSaveProblem(url, username, false);
    }

    public Posts importAndSaveProblem(String url, String username, boolean reuseExisting) {
        Map<String, Object> parsedResult = importProblem(url);
        Posts post = objectMapper.convertValue(parsedResult, Posts.class);

        Optional<Posts> existing = findExistingProblem(username, post);
        if (existing.isPresent()) {
            if (reuseExisting) {
                return refreshImportedProblem(existing.get(), post);
            }
            throw new IllegalStateException("Problem already exists in your database");
        }

        post.setUserName(username);
        post.setLastModified(new Date());
        userService.setLastdate(username);
        postService.createPost(post, username);
        return post;
    }

    private Posts refreshImportedProblem(Posts existing, Posts parsed) {
        existing.setTitle(parsed.getTitle());
        existing.setDescription(parsed.getDescription());
        existing.setDifficulty(parsed.getDifficulty());
        existing.setType(parsed.getType());
        existing.setContestId(parsed.getContestId());
        existing.setIndex(parsed.getIndex());
        existing.setTags(parsed.getTags());
        existing.setTestcase(parsed.getTestcase());
        existing.setConstrain(parsed.getConstrain());
        existing.setTimeLimitSeconds(parsed.getTimeLimitSeconds());
        existing.setMemoryLimitKb(parsed.getMemoryLimitKb());
        existing.setEditorialUrl(parsed.getEditorialUrl());
        existing.setLastModified(new Date());
        return postRepo.save(existing);
    }

public CodeforcesContestData importContestMetadataFromHtml(String url) {

    ContestLocator locator = normalizeContestUrl(url);

    try {

        Document doc = Jsoup.connect(locator.canonicalUrl())
                .userAgent(USER_AGENT)
                .timeout(15000)
                .get();

        //----------------------------------------------------
        // Contest Name
        //----------------------------------------------------

        String contestName = "";

        Element sidebarTitle = doc.selectFirst("#sidebar .sidebox .rtable th a");

        if (sidebarTitle != null)
            contestName = sidebarTitle.text().trim();

        if (contestName.isBlank()) {

            Element title = doc.selectFirst("title");

            if (title != null) {
                contestName = title.text()
                        .replace(" - Codeforces", "")
                        .replace("Dashboard - ", "")
                        .trim();
            }
        }

        //----------------------------------------------------
        // Contest Phase
        //----------------------------------------------------

        String phase = "FINISHED";

        Element phaseElement = doc.selectFirst(".contest-state-phase");

        if (phaseElement != null)
            phase = phaseElement.text().trim().toUpperCase();

        //----------------------------------------------------
        // Duration
        //----------------------------------------------------

        long durationSeconds = parseContestDuration(doc);

        //----------------------------------------------------
        // Official Start Time
        //----------------------------------------------------

        Date officialStart = parseContestStart(doc);

        //----------------------------------------------------
        // Problems
        //----------------------------------------------------

        List<CodeforcesProblemRef> problems = new ArrayList<>();

        Elements rows = doc.select("table.problems tr");

        for (Element row : rows) {

            Element anchor = row.selectFirst("td a[href*='/problem/']");

            if (anchor == null)
                continue;

            String href = anchor.attr("href");

            if (href.isBlank())
                continue;

            String problemUrl = "https://codeforces.com" + href;

            Matcher matcher = PROBLEM_URL_PATTERN.matcher(problemUrl);

            if (!matcher.find())
                continue;

            int contestId = Integer.parseInt(matcher.group(1));

            String index = matcher.group(2);

            String name = anchor.text();

            problems.add(new CodeforcesProblemRef(
                    contestId,
                    index,
                    name,
                    problemUrl
            ));
        }

        if (problems.isEmpty()) {
            throw new IllegalArgumentException("No contest problems found.");
        }

        return new CodeforcesContestData(
                locator.contestId(),
                contestName,
                phase,
                durationSeconds,
                officialStart,
                locator.canonicalUrl(),
                locator.virtualContest(),
                locator.gym(),
                problems
        );

    } catch (Exception e) {

        throw new IllegalArgumentException(
                "Unable to parse Codeforces contest page: " + e.getMessage(),
                e
        );
    }
}

private ContestLocator normalizeContestUrl(String url) {

    String lower = url.toLowerCase();

    boolean virtualContest =
            lower.contains("contestregistration")
            || lower.contains("/virtual");

    boolean gym = lower.contains("/gym/");

    Pattern pattern = Pattern.compile(
            "(?:contestRegistration|contest|gym|group/[^/]+/contest)/(\\d+)",
            Pattern.CASE_INSENSITIVE
    );

    Matcher matcher = pattern.matcher(url);

    if (!matcher.find()) {
        throw new IllegalArgumentException(
                "Unsupported Codeforces contest url."
        );
    }

    int contestId = Integer.parseInt(matcher.group(1));

    String canonical;

    if (gym)
        canonical = "https://codeforces.com/gym/" + contestId;
    else
        canonical = "https://codeforces.com/contest/" + contestId;

    return new ContestLocator(
            contestId,
            canonical,
            virtualContest,
            gym
    );
}

    private Optional<Posts> findExistingProblem(String username, Posts importedPost) {
        if (importedPost.getContestId() > 0 && importedPost.getIndex() != null && !importedPost.getIndex().isBlank()) {
            Optional<Posts> exact = postRepo.findFirstByContestIdAndIndex(
                    importedPost.getContestId(),
                    importedPost.getIndex());
            if (exact.isPresent()) {
                return exact;
            }
        }

        return postRepo.findFirstByTitleIgnoreCase(importedPost.getTitle());
    }

    private ProblemLocator extractProblemLocator(String url) {
        Matcher matcher = PROBLEM_URL_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unsupported Codeforces problem URL: " + url);
        }
        return new ProblemLocator(Integer.parseInt(matcher.group(1)), matcher.group(2).toUpperCase());
    }

private long parseContestDuration(Document doc) {

    Elements rows = doc.select("table.rtable tr");

    for (Element row : rows) {

        if (!row.text().toLowerCase().contains("duration"))
            continue;

        String text = row.text();

        Matcher matcher = Pattern.compile("(\\d+):(\\d+)").matcher(text);

        if (matcher.find()) {

            long hours = Long.parseLong(matcher.group(1));

            long minutes = Long.parseLong(matcher.group(2));

            return hours * 3600 + minutes * 60;
        }
    }

    return 7200;
}

private Date parseContestStart(Document doc) {

    Elements rows = doc.select("table.rtable tr");

    for (Element row : rows) {

        if (!row.text().toLowerCase().contains("start"))
            continue;

        String text = row.text();

        Matcher matcher = Pattern.compile(
                "(\\w{3})/(\\d{2})/(\\d{4})\\s(\\d{2}):(\\d{2})"
        ).matcher(text);

        if (!matcher.find())
            continue;

        String month = matcher.group(1);

        int day = Integer.parseInt(matcher.group(2));

        int year = Integer.parseInt(matcher.group(3));

        int hour = Integer.parseInt(matcher.group(4));

        int minute = Integer.parseInt(matcher.group(5));

        Month m = Month.valueOf(month.toUpperCase(Locale.ENGLISH));

        LocalDateTime ldt = LocalDateTime.of(
                year,
                m,
                day,
                hour,
                minute
        );

        return Date.from(
                ldt.atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    return null;
}

    private static String cleanCFPreTag(Element preElement) {
        String html = preElement.html();
        html = html.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n");
        html = html.replace("</div>", "\n</div>");

        String plainText = html.replaceAll("<[^>]*>", "");
        plainText = plainText.replaceAll("\\n{2,}", "\n");
        return org.jsoup.parser.Parser.unescapeEntities(plainText, false).trim();
    }

    private record ProblemLocator(int contestId, String index) {
    }

    public record CodeforcesProblemRef(int contestId, String index, String name, String url) {
    }

    public record CodeforcesContestData(
            int contestId,
            String name,
            String phase,
            long durationSeconds,
            Date officialStart,
            String canonicalContestUrl,
            boolean virtualRequest,
            boolean gym,
            List<CodeforcesProblemRef> problems) {
    }


}
