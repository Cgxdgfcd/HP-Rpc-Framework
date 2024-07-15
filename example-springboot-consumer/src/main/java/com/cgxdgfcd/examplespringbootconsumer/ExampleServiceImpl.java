package com.cgxdgfcd.examplespringbootconsumer;

import com.cgxdgfcd.example.common.model.User;
import com.cgxdgfcd.example.common.service.UserService;
import com.cgxdgfcd.rpc.springboot.starter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {

    @RpcReference
    private UserService userService;

    public void test() {
        User user = new User();
        user.setName("wjc");
        User result = userService.getUser(user);
        System.out.println(result);
    }
}
