package com.example.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.example.gmall.pms.vo.SpuInfoVO;


/**
 * spu信息
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    /**
     * 根据查询条件和分类id查询spu
     * @param condition
     * @param cid
     * @return
     */
    PageVo querySpuPage(QueryCondition condition, Long cid);

    /**
     * 保存SpuInfo表
     * @param spuInfo
     * @return
     */
    int saveSkuInfo(SpuInfoVO spuInfo);
}

