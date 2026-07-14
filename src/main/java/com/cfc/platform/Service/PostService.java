package com.cfc.platform.Service;

import com.cfc.platform.MongoRepo.ContestRepo;
import com.cfc.platform.MongoRepo.CourseRepo;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.MongoRepo.UserRepo;
import com.cfc.platform.Pojo.Contest;
import com.cfc.platform.Pojo.Course.Course;
import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Pojo.User;
//import org.slf4j.// logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PostService {

    // private static final // logger // logger =
    // LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostRepo postRepo;
    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private ContestRepo contestRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    // @Cacheable("Posts")
    public List<Posts> getAllPosts() {
        return postRepo.findAll();
    }

    public List<Posts> getQuestionsByTags(List<String> tags) {
        return postRepo.findByTagsIn(tags);
    }

    public Optional<Posts> getUserById(String id) {
        return postRepo.findById(id);
    }

    public Date getLastModifiedForUser(String username) {
        User user = userService.findByName(username);
        if (user != null && postRepo.countByUserName(username) > 0) {
            Date date = user.getLastModifiedUser();
            return date;
        }
        return new Date(0);
    }

    public Page<Posts> findPostsByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepo.findByUserName(username, pageable);
    }

    public Page<Posts> findProblemSet(String search, List<String> difficulties, List<String> statuses,
                                      List<String> companies, List<String> topics, String sort,
                                      int page, int size) {
        List<Criteria> groups = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            Pattern value = containsIgnoreCase(search.trim());
            groups.add(new Criteria().orOperator(
                    Criteria.where("title").regex(value),
                    Criteria.where("tags").regex(value),
                    Criteria.where("index").regex(value)));
        }
        addArrayOrScalarFilter(groups, "difficulty", difficulties);
        addArrayOrScalarFilter(groups, "companies", companies);
        addArrayOrScalarFilter(groups, "tags", topics);

        if (statuses != null && !statuses.isEmpty()) {
            List<Criteria> statusOptions = new ArrayList<>();
            for (String status : statuses) {
                if (status.equalsIgnoreCase("Solved")) {
                    statusOptions.add(Criteria.where("status").regex(exactIgnoreCase("solved")));
                    statusOptions.add(Criteria.where("status").regex(exactIgnoreCase("done")));
                } else if (status.equalsIgnoreCase("Attempted")) {
                    statusOptions.add(Criteria.where("status").regex(exactIgnoreCase("attempted")));
                    statusOptions.add(Criteria.where("status").regex(exactIgnoreCase("attempting")));
                } else if (status.equalsIgnoreCase("Todo")) {
                    statusOptions.add(Criteria.where("status").regex(exactIgnoreCase("todo")));
                    statusOptions.add(Criteria.where("status").exists(false));
                    statusOptions.add(Criteria.where("status").is(null));
                }
            }
            if (!statusOptions.isEmpty()) groups.add(new Criteria().orOperator(statusOptions));
        }

        Query query = new Query();
        if (!groups.isEmpty()) query.addCriteria(new Criteria().andOperator(groups));
        long total = mongoTemplate.count(query, Posts.class);

        Sort ordering = switch (sort == null ? "" : sort) {
            case "acc" -> Sort.by(Sort.Direction.DESC, "accuracy");
            case "new" -> Sort.by(Sort.Direction.DESC, "lastModified");
            default -> Sort.by(Sort.Direction.DESC, "_id");
        };
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), ordering);
        query.with(pageable);
        return new PageImpl<>(mongoTemplate.find(query, Posts.class), pageable, total);
    }

    private static void addArrayOrScalarFilter(List<Criteria> groups, String field, List<String> values) {
        if (values == null || values.isEmpty()) return;
        List<Criteria> options = values.stream()
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .map(value -> Criteria.where(field).regex(exactIgnoreCase(value.trim())))
                .toList();
        if (!options.isEmpty()) groups.add(new Criteria().orOperator(options));
    }

    private static Pattern containsIgnoreCase(String value) {
        return Pattern.compile(Pattern.quote(value), Pattern.CASE_INSENSITIVE);
    }

    private static Pattern exactIgnoreCase(String value) {
        return Pattern.compile("^" + Pattern.quote(value) + "$", Pattern.CASE_INSENSITIVE);
    }

    public void createPost(Posts posts, String username) {
        try {
            postRepo.save(posts);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Error saving post", e);
        }
    }

    // @Transactional
    public void createPostWithRefCourse(Posts post, User user, String username) {
        try {

            postRepo.save(post);

            Course course = post.getCourse();
            if (course != null) {
                course.getPosts().add(post);
                courseRepo.save(course);
            }

            // logger.info("(Ref course)Post created successfully for user: {}", username);
        } catch (Exception e) {
            // logger.error("(Ref course)Error creating post for user: {}", username, e);
            throw new RuntimeException("(Ref course)Error creating post", e);
        }
    }

    public void createPostWithRefContest(Posts post, User user, String username) {
        try {

            if (user == null) {
                throw new RuntimeException("(Ref contest)User not found");
            }

            postRepo.save(post);
            // user.getPosts().add(post);
            userRepo.save(user);

            Contest contest = post.getContest();
            if (contest != null) {
                contest.getPosts().add(post);
                contestRepo.save(contest);
            }

            // logger.info("(Ref constest)Post created successfully for user: {}",
            // username);
        } catch (Exception e) {
            // logger.error("(Ref constest)Error creating post for user: {}", username, e);
            throw new RuntimeException("(Ref course)Error creating post", e);
        }
    }

    public void deleteUserById(String id, String name) {
        try {
            postRepo.deleteById(id);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Error deleting post", e);
        }
    }

    public Posts updatePost(String id, Posts newPost, String username) {
        try {
            Optional<Posts> existingPostOpt = postRepo.findById(id);

            if (existingPostOpt.isPresent()) {
                Posts existingPost = existingPostOpt.get();
                if (username.equals(existingPost.getUserName())) {
                    // logger.error("user {} contain post found: {}", username,existingPost);
                    existingPost
                            .setTitle(newPost.getTitle() != null && !newPost.getTitle().isEmpty() ? newPost.getTitle()
                                    : existingPost.getTitle());
                    existingPost.setDescription(newPost.getDescription() != null && !newPost.getDescription().isEmpty()
                            ? newPost.getDescription()
                            : existingPost.getDescription());
                    existingPost.setAnswer(
                            newPost.getAnswer() != null && !newPost.getAnswer().isEmpty() ? newPost.getAnswer()
                                    : existingPost.getAnswer());
                    existingPost.setExample(
                            newPost.getExample() != null && !newPost.getExample().isEmpty() ? newPost.getExample()
                                    : existingPost.getExample());
                    existingPost.setDifficulty(newPost.getDifficulty() != null && !newPost.getDifficulty().isEmpty()
                            ? newPost.getDifficulty()
                            : existingPost.getDifficulty());
                    existingPost.setConstrain(
                            newPost.getConstrain() != null && !newPost.getConstrain().isEmpty() ? newPost.getConstrain()
                                    : existingPost.getConstrain());
                    existingPost.setTimecomplixity(
                            newPost.getTimecomplixity() != null && !newPost.getTimecomplixity().isEmpty()
                                    ? newPost.getTimecomplixity()
                                    : existingPost.getTimecomplixity());
                    existingPost.setAvgtime(
                            newPost.getAvgtime() != null && !newPost.getAvgtime().isEmpty() ? newPost.getAvgtime()
                                    : existingPost.getAvgtime());
                    existingPost.setType(newPost.getType() != null && !newPost.getType().isEmpty() ? newPost.getType()
                            : existingPost.getType());
                    existingPost.setOptionA(
                            newPost.getOptionA() != null && !newPost.getOptionA().isEmpty() ? newPost.getOptionA()
                                    : existingPost.getOptionA());
                    existingPost.setOptionB(
                            newPost.getOptionB() != null && !newPost.getOptionB().isEmpty() ? newPost.getOptionB()
                                    : existingPost.getOptionB());
                    existingPost.setOptionC(
                            newPost.getOptionC() != null && !newPost.getOptionC().isEmpty() ? newPost.getOptionC()
                                    : existingPost.getOptionC());
                    existingPost.setOptionD(
                            newPost.getOptionD() != null && !newPost.getOptionD().isEmpty() ? newPost.getOptionD()
                                    : existingPost.getOptionD());
                    existingPost.setVideoUrl(
                            newPost.getVideoUrl() != null && !newPost.getVideoUrl().isEmpty() ? newPost.getVideoUrl()
                                    : existingPost.getVideoUrl());
                    existingPost.setSequence(
                            newPost.getSequence() != null && !newPost.getSequence().isEmpty() ? newPost.getSequence()
                                    : existingPost.getSequence());
                    existingPost.setTags(newPost.getTags() != null && !newPost.getTags().isEmpty() ? newPost.getTags()
                            : existingPost.getTags());
                    existingPost.setCompanies(
                            newPost.getCompanies() != null && !newPost.getCompanies().isEmpty() ? newPost.getCompanies()
                                    : existingPost.getCompanies());
                    existingPost.setAccuracy(
                            newPost.getAccuracy() != null && !newPost.getAccuracy().isEmpty() ? newPost.getAccuracy()
                                    : existingPost.getAccuracy());

                    // Update codeTemplates
                    if (newPost.getCodeTemplates() != null) {
                        if (existingPost.getCodeTemplates() == null) {
                            existingPost.setCodeTemplates(new HashMap<>());
                        }
                        newPost.getCodeTemplates().forEach((language, newHelperCode) -> {
                            existingPost.getCodeTemplates().put(language, newHelperCode);
                        });
                    }

                    // Update solution
                    if (newPost.getSolution() != null) {
                        if (existingPost.getSolution() == null) {
                            existingPost.setSolution(new HashMap<>());
                        }
                        newPost.getSolution().forEach((language, newSolutionCode) -> {
                            existingPost.getSolution().put(language, newSolutionCode);
                        });
                    }

                    return postRepo.save(existingPost);
                } else {
                    throw new RuntimeException("Post does not belong to the user");
                }
            } else {
                // logger.error("existing post not found: {}", username);
                throw new RuntimeException("Post not found");
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while updating the post", e);
        }
    }

}
