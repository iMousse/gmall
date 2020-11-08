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

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;

    @Override
    public ItemVO queryItemVO(Long skuId) {
        ItemVO item = new ItemVO();

        item.setSkuId(skuId);
        //根据id查询sku
        SkuInfoEntity skuInfo = this.pmsClient.querySkuInfoBySkuId(skuId).getData();
        if (skuInfo == null) {
            return item;
        }
        item.setSkuTitle(skuInfo.getSkuTitle());
        item.setSubtitle(skuInfo.getSkuSubtitle());
        item.setPrice(skuInfo.getPrice());
        item.setWeight(skuInfo.getWeight());

        //根据sku获取spuId查询spu
        Long spuId = skuInfo.getSpuId();
        item.setSpuId(spuId);
        SpuInfoEntity spuInfo = pmsClient.querySpuById(spuId).getData();
        if (spuInfo != null) {
            item.setSpuName(spuInfo.getSpuName());
        }

        //根据skuId查询图片列表
        List<SkuImagesEntity> skuImagesEntities = this.pmsClient.querySkuImagesBySkuId(skuId).getData();
        item.setPics(skuImagesEntities);

        //根据sku中的brandId和categoryID查询品牌和分类
        BrandEntity brandEntity = this.pmsClient.queryBrandInfo(skuInfo.getBrandId()).getData();
        item.setBrandEntity(brandEntity);
        CategoryEntity categoryEntity = this.pmsClient.queryCategoryInfo(skuInfo.getCatalogId()).getData();
        item.setCategoryEntity(categoryEntity);

        //根据skuId查询营销信息
        List<SaleVO> saleVOS = this.smsClient.querySkuSalesBySkuId(skuId).getData();
        item.setSales(saleVOS);

        //根据skuId查询库存信息
        List<WareSkuEntity> wareSkuEntities = this.wmsClient.queryWareSkusBySkuId(skuId).getData();
        item.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));

        //根据spuId查询所有skuIds,再去查询销售属性
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = this.pmsClient.querySkuSaleAttrValueBySpuId(spuId).getData();
        item.setSaleAttrs(skuSaleAttrValueEntities);

        //根据spuId查询商品描述海报
        SpuInfoDescEntity spuInfoDescEntity = this.pmsClient.querySpuDescBySpuId(spuId).getData();
        if (spuInfoDescEntity != null) {
            String decript = spuInfoDescEntity.getDecript();
            String[] split = StringUtils.split(decript, ",");
            item.setDesc(Arrays.asList(split));
        }

        //根据spuId和cateId查询组及组下规格参数(带值)
        item.setGroups(this.pmsClient.queryItemGroupVOByCidAndSpuId(skuInfo.getCatalogId(), spuId).getData());


        return item;
    }
}
