package com.sevenfish.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sevenfish.dao.StorageMapper;
import com.sevenfish.entity.Storage;
import com.sevenfish.service.StorageService;
import org.springframework.stereotype.Service;

@Service
@DS("master_3")
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {
}
