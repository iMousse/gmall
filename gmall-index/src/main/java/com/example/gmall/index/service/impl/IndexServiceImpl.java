package com.example.gmall.index.service.impl;

import com.atguigu.core.bean.Resp;
import com.example.gmall.index.client.GmallPmsClient;
import com.example.gmall.index.service.IndexService;
import com.example.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Override
    public List<CategoryEntity> queryCate1() {
        Resp<List<CategoryEntity>> listResp = this.pmsClient.queryByCategoriesByPidOrLevel(1, null);
        return listResp.getData();
    }

}
