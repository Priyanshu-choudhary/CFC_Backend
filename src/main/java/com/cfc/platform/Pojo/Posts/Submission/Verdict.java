package com.cfc.platform.Pojo.Posts.Submission;



public enum Verdict {

    QUEUED,

    PROCESSING,

    ACCEPTED,

    WRONG_ANSWER,

    TIME_LIMIT_EXCEEDED,

    MEMORY_LIMIT_EXCEEDED,

    RUNTIME_ERROR,

    COMPILATION_ERROR,

    OUTPUT_LIMIT_EXCEEDED,

    INTERNAL_ERROR
}