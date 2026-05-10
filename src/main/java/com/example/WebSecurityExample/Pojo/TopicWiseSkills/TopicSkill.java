package com.example.WebSecurityExample.Pojo.TopicWiseSkills;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

public class TopicSkill {
    @Id
    private String id;

    private String name;
    private List<TopicSkill> children;
    private List<Problem> problem;

    // No-args constructor
    public TopicSkill() {
    }

    // All-args constructor
    public TopicSkill(String id, String name, List<TopicSkill> children, List<Problem> problem) {
        this.id = id;
        this.name = name;
        this.children = children;
        this.problem = problem;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TopicSkill> getChildren() {
        return children;
    }

    public void setChildren(List<TopicSkill> children) {
        this.children = children;
    }

    public List<Problem> getProblem() {
        return problem;
    }

    public void setProblem(List<Problem> problem) {
        this.problem = problem;
    }

    // Nested Problem class
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

        // No-args constructor
        public Problem() {
        }

        // All-args constructor
        public Problem(String id, String title, String description, String example, String difficulty,
                Solution solution, String constrain, String timecomplexity, String avgtime,
                String lastModified, String videoUrl, Map<String, CodeTemplate> codeTemplates) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.Example = example;
            this.difficulty = difficulty;
            this.solution = solution;
            this.constrain = constrain;
            this.timecomplexity = timecomplexity;
            this.avgtime = avgtime;
            this.lastModified = lastModified;
            this.videoUrl = videoUrl;
            this.codeTemplates = codeTemplates;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getExample() {
            return Example;
        }

        public void setExample(String example) {
            Example = example;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public Solution getSolution() {
            return solution;
        }

        public void setSolution(Solution solution) {
            this.solution = solution;
        }

        public String getConstrain() {
            return constrain;
        }

        public void setConstrain(String constrain) {
            this.constrain = constrain;
        }

        public String getTimecomplexity() {
            return timecomplexity;
        }

        public void setTimecomplexity(String timecomplexity) {
            this.timecomplexity = timecomplexity;
        }

        public String getAvgtime() {
            return avgtime;
        }

        public void setAvgtime(String avgtime) {
            this.avgtime = avgtime;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public Map<String, CodeTemplate> getCodeTemplates() {
            return codeTemplates;
        }

        public void setCodeTemplates(Map<String, CodeTemplate> codeTemplates) {
            this.codeTemplates = codeTemplates;
        }

        // Nested Solution class
        public static class Solution {
            private Map<String, LanguageSolution> solutions;

            // No-args constructor
            public Solution() {
            }

            // All-args constructor
            public Solution(Map<String, LanguageSolution> solutions) {
                this.solutions = solutions;
            }

            // Getters and Setters
            public Map<String, LanguageSolution> getSolutions() {
                return solutions;
            }

            public void setSolutions(Map<String, LanguageSolution> solutions) {
                this.solutions = solutions;
            }

            // Nested LanguageSolution class
            public static class LanguageSolution {
                private String solution;

                // No-args constructor
                public LanguageSolution() {
                }

                // All-args constructor
                public LanguageSolution(String solution) {
                    this.solution = solution;
                }

                // Getters and Setters
                public String getSolution() {
                    return solution;
                }

                public void setSolution(String solution) {
                    this.solution = solution;
                }
            }
        }

        // Nested CodeTemplate class
        public static class CodeTemplate {
            private String templateCode;
            private String boilerCode;

            // No-args constructor
            public CodeTemplate() {
            }

            // All-args constructor
            public CodeTemplate(String templateCode, String boilerCode) {
                this.templateCode = templateCode;
                this.boilerCode = boilerCode;
            }

            // Getters and Setters
            public String getTemplateCode() {
                return templateCode;
            }

            public void setTemplateCode(String templateCode) {
                this.templateCode = templateCode;
            }

            public String getBoilerCode() {
                return boilerCode;
            }

            public void setBoilerCode(String boilerCode) {
                this.boilerCode = boilerCode;
            }
        }
    }
}
