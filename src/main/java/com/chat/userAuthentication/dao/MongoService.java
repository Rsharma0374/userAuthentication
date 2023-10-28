package com.chat.userAuthentication.dao;

import com.chat.userAuthentication.request.UserCreation;

public interface MongoService {
    boolean saveData(UserCreation userCreation);

    boolean checkExistence(UserCreation userCreation);

    UserCreation getUserFromUserName(String userName);
}
