package com.example.WebSecurityExample.Pojo.TopicWiseSkills;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicSkill {
    @Id
    private String id;

    private String name;
    private List<TopicSkill> children;
    private List<Problem> problem;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Problem {
        private String id;
        private String title;
        private String description;
        private String Example;
        private String difficulty;
        private Solution solution;
        private String constrain;
        private String timecomplexity;
        private String avgtime;
        private String lastModified;
        private String videoUrl;
        private Map<String, CodeTemplate> codeTemplates;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Solution {
            private Map<String, LanguageSolution> solutions;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class LanguageSolution {
                private String solution;
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CodeTemplate {
            private String templateCode;
            private String boilerCode;
        }
    }
}
