package com.cfc.platform.DTO;

enum ContestType
{
    LIVE,
    VIRTUAL,
    GYM
}
public record ContestLocator(

    int contestId,

    String canonicalUrl,


    boolean virtualContest,

    boolean gym

){}