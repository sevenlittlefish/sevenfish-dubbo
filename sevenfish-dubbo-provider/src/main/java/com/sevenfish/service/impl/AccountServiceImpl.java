package com.sevenfish.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sevenfish.dao.AccountMapper;
import com.sevenfish.entity.Account;
import com.sevenfish.service.AccountService;
import org.springframework.stereotype.Service;

@Service
@DS("master_1")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
}
