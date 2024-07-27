package com.example.WebSecurityExample.controller;

import com.example.WebSecurityExample.Pojo.User;
import com.example.WebSecurityExample.Service.CourseService;
import com.example.WebSecurityExample.Service.PostService;
import com.example.WebSecurityExample.Service.UserService;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/Public")
//@CrossOrigin(origins = {"https://code-with-challenge.vercel.app", " http://localhost:5173"})

public class PublicController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private MongoClient mongoClient;

    @GetMapping("HealthCheck")
    public Map<String, Object> healthCheck() {
        Map<String, Object> healthDetails = new HashMap<>();

        // Server Status
        healthDetails.put("server_status", "running");

        // Database Status
        try {
            mongoClient.getDatabase("admin").runCommand(new org.bson.Document("ping", 1));
            healthDetails.put("database_status", "up");
        } catch (Exception e) {
            healthDetails.put("database_status", "down");
        }

        // CPU Load
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        healthDetails.put("cpu_load", osBean.getSystemLoadAverage());

        // Memory Consumption
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        healthDetails.put("heap_memory_used", formatBytes(heapUsage.getUsed()));
        healthDetails.put("heap_memory_max", formatBytes(heapUsage.getMax()));
        healthDetails.put("non_heap_memory_used", formatBytes(nonHeapUsage.getUsed()));
        healthDetails.put("non_heap_memory_max", formatBytes(nonHeapUsage.getMax()));

        // Disk Space
        long diskFreeSpace = new java.io.File("/").getFreeSpace();
        long diskTotalSpace = new java.io.File("/").getTotalSpace();
        healthDetails.put("disk_free_space", formatBytes(diskFreeSpace));
        healthDetails.put("disk_total_space", formatBytes(diskTotalSpace));

        return healthDetails;
    }
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return bytes / 1024 + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return bytes / (1024 * 1024) + " MB";
        } else {
            return bytes / (1024 * 1024 * 1024) + " GB";
        }
    }
    @GetMapping("getAllUser")
    public List getAllUsersbyUserName() {
        return  userService.getAllUsers();
    }

    @GetMapping("showUser/{username}")
    public User getUsersbyUserName(@PathVariable String username) {
        return  userService.findByName(username);
    }


    @CrossOrigin(origins = {"https://www.codeforchallenge.online", "http://localhost:5173"})
    @PostMapping("/Create-User")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            if (userService.existsByName(user.getName())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT); // Return 409 Conflict if user already exists
            }
            userService.createNewUser(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> users= userService.getUserById(id);
        if (users.isPresent()) {
            return new ResponseEntity<>(users.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>( HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/user/id/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        try {
            userService.deleteUserById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
