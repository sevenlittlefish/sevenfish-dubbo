package com.sevenfish.controller;

import com.sevenfish.entity.User;
import com.sevenfish.service.BusinessService;
import io.seata.core.exception.TransactionException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusinessController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @DubboReference
    private BusinessService businessService;

    @PostMapping("purchase")
    public Object purchase(@RequestParam("userId") String userId, @RequestParam("commodityCode")String commodityCode, @RequestParam("orderCount")int orderCount) throws TransactionException {
        return businessService.purchase(userId, commodityCode, orderCount);
    }

    @PostMapping("createUser")
    public Object createUser(User user){
        return businessService.createUser(user);
    }

    @GetMapping("userList")
    public Object userList(){
        return businessService.userList();
    }
}
