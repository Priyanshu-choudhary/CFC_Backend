package com.example.WebSecurityExample.Service;

import com.example.WebSecurityExample.MongoRepo.PostRepo;
import com.example.WebSecurityExample.Pojo.Posts;
import com.example.WebSecurityExample.Pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    @Autowired
    private PostRepo postRepo;
    @Autowired
    private UserService userService;

    public List<Posts> getAllUsers() {
        return postRepo.findAll();
    }

    public Optional<Posts> getUserById(String id) {
        return postRepo.findById(id);
    }
    @Transactional
    public void createUser(Posts posts,String inputuser) {
        try{
            User myuser = userService.findByName(inputuser);//get user

            Posts saved= postRepo.save(posts);//saved in posts DB
            myuser.getPosts().add(saved);

            userService.createUser(myuser);//saved in user DB(creating ref)
        }catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("an error occur while saving an entry",e);
        }
    }
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
