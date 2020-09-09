package com.example.gmall.pms.service.impl;

import com.example.gmall.pms.vo.SpuInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.example.gmall.pms.dao.SpuInfoDescDao;
import com.example.gmall.pms.entity.SpuInfoDescEntity;
import com.example.gmall.pms.service.SpuInfoDescService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoDescService")
public class SpuInfoDescServiceImpl extends ServiceImpl<SpuInfoDescDao, SpuInfoDescEntity> implements SpuInfoDescService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoDescEntity> page = this.page(
                new Query<SpuInfoDescEntity>().getPage(params),
                new QueryWrapper<SpuInfoDescEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveSpuInfoDesc(SpuInfoVO spuInfoVO) {
        List<String> spuImages = spuInfoVO.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
            //数据库中spu_id不是主键，且这个表没有主键，所以需要单独设置TableId
            descEntity.setSpuId(spuInfoVO.getId());
            descEntity.setDecript(StringUtils.join(spuImages, ","));
            this.save(descEntity);
        }
    }

}