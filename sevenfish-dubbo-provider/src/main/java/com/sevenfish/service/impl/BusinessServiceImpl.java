package com.sevenfish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sevenfish.service.*;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import org.apache.dubbo.config.annotation.DubboService;
import com.sevenfish.common.Result;
import com.sevenfish.entity.Account;
import com.sevenfish.entity.Order;
import com.sevenfish.entity.Storage;
import com.sevenfish.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@DubboService(timeout = 60000)
public class BusinessServiceImpl implements BusinessService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserService userService;

    @Override
    @GlobalTransactional
    public Object purchase(String userId, String commodityCode, int orderCount) throws TransactionException {
        try {
            QueryWrapper<Account> query = new QueryWrapper<>();
            query.eq("user_id",userId);
            Account account = accountService.getOne(query);

            Storage storage = new Storage();
            storage.setCommodityCode(commodityCode);
            storage = storageService.getOne(new QueryWrapper<>(storage));

            if(account == null || account.getMoney() == null){
                return Result.error("账户异常");
            }
            if(storage == null  || storage.getCount() == null || storage.getPrice() == null){
                return Result.error("商品异常");
            }

            int totalMoney = storage.getPrice()*orderCount;
            account.setMoney(account.getMoney() - totalMoney);
            accountService.saveOrUpdate(account);

            storage.setCount(storage.getCount() - orderCount);
            storageService.saveOrUpdate(storage);

            Order order = new Order();
            order.setMoney(totalMoney);
            order.setCount(orderCount);
            order.setCommodityCode(commodityCode);
            order.setUserId(account.getUserId());
            orderService.saveOrUpdate(order);

            //模拟异常测试分布式事务
            Account na = accountService.getById(account.getId());
            Storage ns = storageService.getById(storage.getId());
            if(na.getMoney() < 0 || ns.getCount() < 0){
                throw new RuntimeException("账户余额或库存不足");
            }
        } catch (Exception e) {
            e.printStackTrace();
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Object createUser(User user) {
        if(user == null || user.getName().isEmpty() || user.getSex() == null){
            return Result.error("请完善用户信息");
        }
        try {
            User uquery = new User();
            uquery.setName(user.getName());
            User existUser = userService.getOne(new QueryWrapper<>(user));
            userService.saveOrUpdate(existUser != null ? existUser : user);
            //模拟异常测试本地事务
            if(user.getSex() > 1 || user.getSex() < 0){
                throw new RuntimeException("性别不符");
            }
            if(existUser == null){
                Account account = new Account();
                account.setUserId(user.getId().toString());
                account.setMoney(100);
                accountService.saveOrUpdate(account);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    @Override
    public Object userList() {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("sex",1);
        return userService.list(query);
    }
}
