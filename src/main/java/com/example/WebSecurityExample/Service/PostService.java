package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.CourseRepo;
import com.example.WebSecurityExample.MongoRepo.PostRepo;
import com.example.WebSecurityExample.MongoRepo.UserRepo;
import com.example.WebSecurityExample.Pojo.Course;
import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.controller.PostController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

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

    @Cacheable("Posts")
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
        if (user != null && user.getPosts() != null && !user.getPosts().isEmpty()) {
            logger.info("try to get it inside function lastModifyed ");
            Date date = user.getLastModifiedUser();
            logger.info("date>>> :{}",date);
            return date;
        }
        return new Date(0); // Return a default date if user or posts not found
    }

    @Caching(evict = {
            @CacheEvict(value = "Posts", allEntries = true),
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "userPostsCache", key = "#username")
    })
    @Transactional
    public void createPost(Posts posts,String inputuser,User myuser) {
        try{

            Posts saved= postRepo.save(posts);//saved in posts DB
            myuser.getPosts().add(saved);

            userService.createUser(myuser);//saved in user DB(creating ref)
        }catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("(Ref course)an error occur while saving an entry",e);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "Posts", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    @Transactional
    public void createPostWithRefCourse(Posts post, User user,String username) {
        try {

            if (user == null) {
                throw new RuntimeException("(Ref course)User not found");
            }

            postRepo.save(post);
            user.getPosts().add(post);
            userRepo.save(user);

            Course course = post.getCourse();
            if (course != null) {
                course.getPosts().add(post);
                courseRepo.save(course);
            }

            logger.info("(Ref course)Post created successfully for user: {}", username);
        } catch (Exception e) {
            logger.error("(Ref course)Error creating post for user: {}", username, e);
            throw new RuntimeException("(Ref course)Error creating post", e);
        }
    }


    @Caching(evict = {
            @CacheEvict(value = "Posts", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    @Transactional
    public void deleteUserById(String id, String name) {
      try {
          User myuser = userService.findByName(name);
          boolean b = myuser.getPosts().removeIf(x -> x.getId().equals(id));
          if (b) {
              userService.createUser(myuser);
              postRepo.deleteById(id);
          }
      }catch (Exception e){

          System.out.println(e);
          throw new RuntimeException("Error occur while delete post",e);
      }

    }
    @Caching(evict = {
            @CacheEvict(value = "Posts", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    @Transactional
    public Posts updatePost(String id, Posts newPost, String username) {
        try {
            User user = userService.findByName(username);
            Optional<Posts> existingPostOpt = postRepo.findById(id);

            if (existingPostOpt.isPresent()) {
                Posts existingPost = existingPostOpt.get();
                if (user.getPosts().contains(existingPost)) {
                    existingPost.setTitle(newPost.getTitle() != null && !newPost.getTitle().isEmpty() ? newPost.getTitle() : existingPost.getTitle());
                    existingPost.setDescription(newPost.getDescription() != null && !newPost.getDescription().isEmpty() ? newPost.getDescription() : existingPost.getDescription());
                    existingPost.setAnswer(newPost.getAnswer() != null && !newPost.getAnswer().isEmpty() ? newPost.getAnswer() : existingPost.getAnswer());
                    existingPost.setExample(newPost.getExample() != null && !newPost.getExample().isEmpty() ? newPost.getExample() : existingPost.getExample());
                    existingPost.setSolution(newPost.getSolution() != null && !newPost.getSolution().isEmpty() ? newPost.getSolution() : existingPost.getSolution());
                    existingPost.setDifficulty(newPost.getDifficulty() != null && !newPost.getDifficulty().isEmpty() ? newPost.getDifficulty() : existingPost.getDifficulty());
                    existingPost.setConstrain(newPost.getConstrain() != null && !newPost.getConstrain().isEmpty() ? newPost.getConstrain() : existingPost.getConstrain());
                    existingPost.setTimecomplixity(newPost.getTimecomplixity() != null && !newPost.getTimecomplixity().isEmpty() ? newPost.getTimecomplixity() : existingPost.getTimecomplixity());
                    existingPost.setAvgtime(newPost.getAvgtime() != null && !newPost.getAvgtime().isEmpty() ? newPost.getAvgtime() : existingPost.getAvgtime());
                    existingPost.setBoilerCode(newPost.getBoilerCode() != null && !newPost.getBoilerCode().isEmpty() ? newPost.getBoilerCode() : existingPost.getBoilerCode());
                    existingPost.setType(newPost.getType() != null && !newPost.getType().isEmpty() ? newPost.getType() : existingPost.getType());
                    existingPost.setOptionA(newPost.getOptionA() != null && !newPost.getOptionA().isEmpty() ? newPost.getOptionA() : existingPost.getOptionA());
                    existingPost.setOptionB(newPost.getOptionB() != null && !newPost.getOptionB().isEmpty() ? newPost.getOptionB() : existingPost.getOptionB());
                    existingPost.setOptionC(newPost.getOptionC() != null && !newPost.getOptionC().isEmpty() ? newPost.getOptionC() : existingPost.getOptionC());
                    existingPost.setOptionD(newPost.getOptionD() != null && !newPost.getOptionD().isEmpty() ? newPost.getOptionD() : existingPost.getOptionD());
                    existingPost.setVideoUrl(newPost.getVideoUrl() != null && !newPost.getVideoUrl().isEmpty() ? newPost.getVideoUrl() : existingPost.getVideoUrl());

                    return postRepo.save(existingPost);
                } else {
                    throw new RuntimeException("Post does not belong to the user");
                }
            } else {
                throw new RuntimeException("Post not found");
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while updating the post", e);
        }
}
}
