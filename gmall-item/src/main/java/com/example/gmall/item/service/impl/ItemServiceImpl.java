package com.example.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.example.gmall.item.client.GmallPmsClient;
import com.example.gmall.item.client.GmallSmsClient;
import com.example.gmall.item.client.GmallWmsClient;
import com.example.gmall.item.service.ItemService;
import com.example.gmall.item.vo.ItemVO;
import com.example.gmall.pms.entity.*;
import com.example.gmall.sms.vo.SaleVO;
import com.example.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public ItemVO queryItemVO(Long skuId) {
        ItemVO item = new ItemVO();

        item.setSkuId(skuId);

        //开一个线程池，supply是有返回值
        CompletableFuture<Object> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据id查询sku
            SkuInfoEntity skuInfo = this.pmsClient.querySkuInfoBySkuId(skuId).getData();
            if (skuInfo == null) {
                return item;
            }
            item.setSkuTitle(skuInfo.getSkuTitle());
            item.setSubtitle(skuInfo.getSkuSubtitle());
            item.setPrice(skuInfo.getPrice());
            item.setWeight(skuInfo.getWeight());

            return skuInfo;
            //thenAccept只需要返回值，不需要依赖，处理就行
        }, threadPoolExecutor);


        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            //根据sku获取spuId查询spu
            item.setSpuId(((SkuInfoEntity) sku).getSpuId());
            SpuInfoEntity spuInfo = pmsClient.querySpuById(((SkuInfoEntity) sku).getSpuId()).getData();
            if (spuInfo != null) {
                item.setSpuName(spuInfo.getSpuName());
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> cateCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            //根据sku中的brandId和categoryID查询品牌和分类
            BrandEntity brandEntity = this.pmsClient.queryBrandInfo(((SkuInfoEntity) sku).getBrandId()).getData();
            item.setBrandEntity(brandEntity);
            CategoryEntity categoryEntity = this.pmsClient.queryCategoryInfo(((SkuInfoEntity) sku).getCatalogId()).getData();
            item.setCategoryEntity(categoryEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrsCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            //根据spuId查询所有skuIds,再去查询销售属性
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = this.pmsClient.querySkuSaleAttrValueBySpuId(((SkuInfoEntity) sku).getSpuId()).getData();
            item.setSaleAttrs(skuSaleAttrValueEntities);
        }, threadPoolExecutor);


        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            //根据spuId查询商品描述海报
            SpuInfoDescEntity spuInfoDescEntity = this.pmsClient.querySpuDescBySpuId(((SkuInfoEntity) sku).getSpuId()).getData();
            if (spuInfoDescEntity != null) {
                String decript = spuInfoDescEntity.getDecript();
                String[] split = StringUtils.split(decript, ",");
                item.setDesc(Arrays.asList(split));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> groupsCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            //根据spuId和cateId查询组及组下规格参数(带值)
            item.setGroups(this.pmsClient.queryItemGroupVOByCidAndSpuId(((SkuInfoEntity) sku).getCatalogId(), ((SkuInfoEntity) sku).getSpuId()).getData());
        }, threadPoolExecutor);


        //在开一个串行任务,不需要返回值并且是相同的线程池
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuId查询图片列表
            List<SkuImagesEntity> skuImagesEntities = this.pmsClient.querySkuImagesBySkuId(skuId).getData();
            item.setPics(skuImagesEntities);
        }, threadPoolExecutor);


        //在开一个串行任务,不需要返回值并且是相同的线程池
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuId查询营销信息
            List<SaleVO> saleVOS = this.smsClient.querySkuSalesBySkuId(skuId).getData();
            item.setSales(saleVOS);
        }, threadPoolExecutor);

        //在开一个串行任务,不需要返回值并且是相同的线程池
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuId查询库存信息
            List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareSkusBySkuId(skuId).getData();
            item.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }, threadPoolExecutor);


        CompletableFuture.allOf(
                spuCompletableFuture, cateCompletableFuture, saleAttrsCompletableFuture,
                descCompletableFuture, groupsCompletableFuture, imageCompletableFuture,
                salesCompletableFuture, storeCompletableFuture).join();


        return item;
    }
}
