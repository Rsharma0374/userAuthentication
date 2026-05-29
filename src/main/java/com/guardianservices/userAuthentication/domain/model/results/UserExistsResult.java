package com.guardianservices.userAuthentication.domain.model.results;

public sealed interface UserExistsResult
        permits UserExistsResult.UserExists,
        UserExistsResult.EmailExists,
        UserExistsResult.UserNotExists {
    record UserExists()    implements UserExistsResult {}
    record EmailExists()   implements UserExistsResult {}
    record UserNotExists() implements UserExistsResult {}
}
