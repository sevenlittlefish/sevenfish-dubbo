package com.sevenfish.service;

import com.sevenfish.entity.User;
import io.seata.core.exception.TransactionException;

public interface BusinessService {

    Object purchase(String userId, String commodityCode, int orderCount) throws TransactionException;

    Object createUser(User user);

    Object userList();
}
