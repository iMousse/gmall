package com.example.gmall.sms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gmall.sms.dao.SkuBoundsDao;
import com.example.gmall.sms.dao.SkuFullReductionDao;
import com.example.gmall.sms.dao.SkuLadderDao;
import com.example.gmall.sms.entity.SkuBoundsEntity;
import com.example.gmall.sms.entity.SkuFullReductionEntity;
import com.example.gmall.sms.entity.SkuLadderEntity;
import com.example.gmall.sms.service.SkuBoundsService;
import com.example.gmall.sms.vo.SaleVO;
import com.example.gmall.sms.vo.SkuSaleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuLadderDao skuLadderDao;

    @Autowired
    private SkuFullReductionDao reductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSales(SkuSaleVO skuSaleVO) {
        //3.保存sms_sku信息的3张表
        Long skuId = skuSaleVO.getSkuId();
        //3.1 保存sms_sku_bounds 商品sku积分设置
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuId);
        skuBoundsEntity.setGrowBounds(skuSaleVO.getGrowBounds());
        skuBoundsEntity.setBuyBounds(skuSaleVO.getBuyBounds());
        //二进制保存work的值
        // 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]")
        List<Integer> work = skuSaleVO.getWork();
        skuBoundsEntity.setWork(work.get(3) * 1 + work.get(2) * 2 + work.get(1) * 4 + work.get(0) * 8);
        this.save(skuBoundsEntity);


        //3.2 保存sms_sku_ladder
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(skuId);
        ladderEntity.setFullCount(skuSaleVO.getFullCount());
        ladderEntity.setDiscount(skuSaleVO.getDiscount());
        ladderEntity.setAddOther(skuSaleVO.getLadderAddOther());
        this.skuLadderDao.insert(ladderEntity);

        //3.3 保存sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setSkuId(skuId);
        reductionEntity.setAddOther(skuSaleVO.getFullAddOther());
        reductionEntity.setFullPrice(skuSaleVO.getFullPrice());
        reductionEntity.setReducePrice(skuSaleVO.getReducePrice());
        this.reductionDao.insert(reductionEntity);

    }

    @Override
    public List<SaleVO> querySaleBySKuId(Long skuId) {
        List<SaleVO> saleVOS = new ArrayList<>();
        //查询积分信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            SaleVO bounds = new SaleVO();
            bounds.setType("积分");
            StringBuffer sb = new StringBuffer();
            BigDecimal growBounds = skuBoundsEntity.getGrowBounds();
            if (growBounds != null && growBounds.intValue() > 0) {
                sb.append("成长积分送").append(growBounds);
            }
            BigDecimal buyBounds = skuBoundsEntity.getBuyBounds();
            if (buyBounds != null && buyBounds.intValue() > 0) {
                if (StringUtils.isNotBlank(sb)) {
                    sb.append(",");
                }
                sb.append("赠送积分送").append(skuBoundsEntity.getBuyBounds());
            }
            bounds.setDesc(sb.toString());
            saleVOS.add(bounds);
        }

        //查询打折信息
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            SaleVO ladderVo = new SaleVO();
            ladderVo.setType("打折");
            ladderVo.setDesc("满" + skuLadderEntity.getFullCount() + "件，打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            saleVOS.add(ladderVo);
        }

        //查询满级活动
        SkuFullReductionEntity reductionEntity = this.reductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (reductionEntity != null) {
            SaleVO reductionVo = new SaleVO();
            reductionVo.setType("满减");
            reductionVo.setDesc("满" + reductionEntity.getFullPrice() + "减" + reductionEntity.getReducePrice());
            saleVOS.add(reductionVo);
        }
        return saleVOS;
    }

}