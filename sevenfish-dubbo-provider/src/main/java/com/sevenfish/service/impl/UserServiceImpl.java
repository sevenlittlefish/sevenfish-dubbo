package com.sevenfish.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sevenfish.dao.UserMapper;
import com.sevenfish.entity.User;
import com.sevenfish.service.UserService;
import org.springframework.stereotype.Service;

@Service
@DS("master_1")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
