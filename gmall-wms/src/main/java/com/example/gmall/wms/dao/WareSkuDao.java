package com.example.gmall.wms.dao;

import com.example.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-09-06 14:12:08
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    /**
     * 检查库存
     * @param skuId
     * @param count
     * @return
     */
    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId, @Param("count") Integer count);

    /**
     * 锁库存
     * @param id
     * @param count
     * @return
     */
    int lockStore(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 解库存
     * @param wareSkuId
     * @param count
     * @return
     */
    int unLockStore(@Param("wareSkuId") Long wareSkuId,@Param("count") Integer count);
}
