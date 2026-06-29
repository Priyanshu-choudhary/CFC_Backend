package com.cfc.platform.controller;

import com.cfc.platform.MongoRepo.ContestRepo;
import com.cfc.platform.MongoRepo.PostRepo;
import com.cfc.platform.Pojo.Contest;
import com.cfc.platform.Pojo.Course.Course;
import com.cfc.platform.Pojo.Posts.Posts;
import com.cfc.platform.Service.CourseService;
import com.cfc.platform.Service.PostService;
import com.cfc.platform.Service.UserService;
//import org.slf4j.// logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Date;

@RestController
@RequestMapping("/Posts")
//@CrossOrigin(origins = {"https://www.codeforchallenge.online", "http://localhost:5173"})
public class PostController {
//    private static final // logger // logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private ContestRepo contestRepo;

    @GetMapping("/ProblemSet")
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Page<Posts> result = postRepo.findAll(PageRequest.of(page, size));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getAllPosts(@PathVariable String username,
            @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
        try {
            List<Posts> all = postRepo.findByUserName(username);
            return new ResponseEntity<>(all, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/username/{username}/posts")
    public ResponseEntity<?> getAllPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Fetch paginated posts directly from the database
            Page<Posts> postsPage = postService.findPostsByUsername(username, page, size);
            return new ResponseEntity<>(postsPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    @GetMapping
//    public ResponseEntity<?> getPostByUsername(@RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {
//        try {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            String username = auth.getName();
//            // logger.info("Fetching posts for user: {}", username);
//            // logger.info("ifModifiedSince: {}", ifModifiedSince);
//
//            User users = userService.findByName(username);
//            if (users == null) {
//                // logger.error("User not found: {}", username);
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            List<Posts> all = users.getPosts();
//            if (all == null || all.isEmpty()) {
//                // logger.info("No posts found for user: {}", username);
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//            // logger.info("try to fetch lastModified ");
//            Date lastModified = postService.getLastModifiedForUser(username);
//            // logger.info("lastModifyed : {}", lastModified);
//
//            if (ifModifiedSince != null) {
//                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
//                Date ifModifiedSinceDate = dateFormat.parse(ifModifiedSince);
//                if (!lastModified.after(ifModifiedSinceDate)) {
//                    // logger.info("Posts not modified since: {}", ifModifiedSince);
//                    return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
//                }
//            }
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setLastModified(lastModified.getTime());
//            // logger.info("Returning posts for user: {}", username);
//            return new ResponseEntity<>(all, headers, HttpStatus.OK);
//        } catch (ParseException e) {
//            // logger.error("Error parsing If-Modified-Since header", e);
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            // logger.error("Error fetching posts by username", e);
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/filter")
    public ResponseEntity<?> getQuestionsByExactTags(@RequestParam List<String> tags,
            @RequestParam(required = false, defaultValue = "true") boolean exactMatch,
            @RequestParam(required = false) String username) {
        try {
            List<Posts> filteredPosts;
            if (username != null && !username.isBlank()) {
                // Scoped to one user (e.g. "OfficialCources") â€” single MongoDB query
                filteredPosts = postRepo.findByUserNameAndTagsAll(username, tags);
            } else {
                // All public posts matching the tags
                filteredPosts = postRepo.findByTagsAll(tags);
            }
            if (filteredPosts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(filteredPosts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/user/id/{myid}")
    public ResponseEntity<?> getUserById(@PathVariable String myid, @RequestParam(required = false) String username) {
        try {
            Optional<Posts> post = postRepo.findById(myid);
            if (post.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            // If username provided, verify ownership
            if (username != null && !username.equals(post.get().getUserName())) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(post.get(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/id/{myid}")
    public ResponseEntity<?> getPostsById(@PathVariable String myid) {
        try {
            // Fetch the post by ID using postRepository
            Optional<Posts> post = postRepo.findById(myid);

            if (post.isPresent()) {
                // Return the found post
                return new ResponseEntity<>(post.get(), HttpStatus.OK);
            } else {
                // Return 404 if the post is not found
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Return 500 in case of an error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/Course/{courseName}/username/{username}")
    public ResponseEntity<?> getPostsByCourse(@PathVariable String courseName, @PathVariable String username) {
        try {
            // Find course directly (no user @DBRef expansion)
            Course course = courseService.findCourseByTitle(courseName);
            if (course == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            // Fetch posts via PostRepo using course id (no @DBRef expansion on course.posts)
            List<Posts> posts = postRepo.findByCourseId(course.getId());
            if (posts == null || posts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/Contest/{contestName}/username/{username}")
    public ResponseEntity<?> getPostsByContest(@PathVariable String contestName, @PathVariable String username) {
        try {
            Contest contest = contestRepo.findByNameOfContest(contestName);
            if (contest == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            List<Posts> posts = postRepo.findByContestId(contest.getId());
            if (posts == null || posts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/username/{username}")
    public ResponseEntity<Posts> createPost(@PathVariable String username, @RequestBody Posts post) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!auth.getName().equals(username)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            // Duplicate title check â€” direct repo query, no @DBRef expansion
            List<Posts> existing = postRepo.findByUserName(username);
            if (existing != null && existing.stream().anyMatch(p -> p.getTitle().equals(post.getTitle()))) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            post.setUserName(username);
            post.setLastModified(new Date());
            userService.setLastdate(username);
            postService.createPost(post, username);
            return new ResponseEntity<>(post, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/Course/{courseName}/username/{username}")
    public ResponseEntity<?> createPostRefCourse(@PathVariable String username, @PathVariable String courseName,
            @RequestBody Posts post) {
        try {
            Course course = courseService.findCourseByTitle(courseName);
            if (course == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            post.setUserName(username);
            post.setLastModified(new Date());
            post.setCourse(course);
            postService.createPostWithRefCourse(post, null, username);
            return new ResponseEntity<>(post, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }





    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Verify the post belongs to the authenticated user
            Optional<Posts> postOpt = postRepo.findById(id);
            if (postOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Posts post = postOpt.get();
            if (!post.getUserName().equals(username)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            postService.deleteUserById(id, username);
            userService.setLastdate(username);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/username/{username}/id/{myId}")
    public ResponseEntity<?> updatePostById(@PathVariable String myId,@PathVariable String username ,@RequestBody Posts newPost) {
        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
            // logger.info("Updating post with ID: {} for user: {}", myId, username);

            Posts updatedPost = postService.updatePost(myId, newPost, username);
            userService.setLastdate(username);
            // logger.info("Post updated successfully with ID: {} for user: {}", myId, username);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            // logger.error("Error updating post by ID", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


}
