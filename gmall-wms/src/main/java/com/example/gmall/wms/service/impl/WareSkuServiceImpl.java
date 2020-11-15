package com.example.gmall.wms.service.impl;

import com.example.gmall.wms.vo.SkuLockVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.example.gmall.wms.dao.WareSkuDao;
import com.example.gmall.wms.entity.WareSkuEntity;
import com.example.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public String checkAndLockStore(List<SkuLockVO> skuLockVOS) {
        if (CollectionUtils.isEmpty(skuLockVOS)) {
            return "没有选中的商品";
        }

        //检验并锁定库存
        skuLockVOS.forEach(this::lockStore);

        List<SkuLockVO> unlockSku = skuLockVOS.stream().filter(skuLockVO -> {
            return !skuLockVO.getLock();
        }).collect(Collectors.toList());

        //有库存不足的商品
        if (!CollectionUtils.isEmpty(unlockSku)) {
            //解锁已锁定商品的库存
            List<SkuLockVO> lockSku = skuLockVOS.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
            lockSku.forEach(skuLockVO -> {
                //根据库存id和数量解锁库存
                this.wareSkuDao.unLockStore(skuLockVO.getWareSkuId(), skuLockVO.getCount());
            });

            //提示锁定失败的商品
            List<Long> skuIds = unlockSku.stream().map(SkuLockVO::getSkuId).collect(Collectors.toList());
            return "下单失败，商品库存不足:" + skuIds.toString();
        }
        return null;
    }

    /**
     * 锁定商品库存，对商品的库存信息和锁定库存信息进行修改
     * @param skuLockVO
     */
    private void lockStore(SkuLockVO skuLockVO) {
        RLock lock = this.redissonClient.getLock("stock" + skuLockVO.getSkuId());
        lock.lock();
        //查询库存够不够
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStore(skuLockVO.getSkuId(), skuLockVO.getCount());

        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            //锁定库存信息
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            Long id = wareSkuEntity.getId();
            //根据仓库id和sku数量来锁定库存
            this.wareSkuDao.lockStore(id, skuLockVO.getCount());
            skuLockVO.setWareSkuId(id);
            skuLockVO.setLock(true);
        } else {
            skuLockVO.setLock(false);
        }

        lock.unlock();
    }

}