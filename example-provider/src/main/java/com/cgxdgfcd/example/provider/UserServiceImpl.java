package com.cgxdgfcd.example.provider;

import com.cgxdgfcd.example.common.model.User;
import com.cgxdgfcd.example.common.service.UserService;

/**
 * 用户服务类实现
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名: " + user.getName());
        return user;
    }
}
