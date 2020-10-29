package com.example.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.entity.*;
import com.example.gmall.search.client.GmallPmsClient;
import com.example.gmall.search.client.GmallWmsClient;
import com.example.gmall.search.pojo.Goods;
import com.example.gmall.search.pojo.SearchAttr;
import com.example.gmall.search.repository.GoodsRepository;
import com.example.gmall.search.service.SearchService;
import com.example.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            //队列，队列的值和队列持久化，因为一个es集群可以绑定一个队列 ，不用忽略声明异常
            value = @Queue(value = "gmall-search-queue", durable = "true"),
            //交换机，交换机的值和交换机的类型，不忽略声明异常，因为一个es集群绑定相同的交换机会报错，所以需要忽略声明异常
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}
    ))
    public void listener(Long spuId) {
        Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.querySpuById(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();

        Resp<List<SkuInfoEntity>> skus = this.pmsClient.querySkusBySpuId(spuInfoEntity.getId());
        List<SkuInfoEntity> skuInfoEntities = skus.getData();
        if (!CollectionUtils.isEmpty(skuInfoEntities)) {
            this.goodsRepository.saveAll(skuInfoEntities.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();

                //查询搜索属性并设置
                Resp<List<ProductAttrValueEntity>> attrValueResp = this.pmsClient.querySearchAttrValueBySpuId(spuInfoEntity.getId());
                List<ProductAttrValueEntity> attrValueEntities = attrValueResp.getData();
                if (!CollectionUtils.isEmpty(attrValueEntities)) {
                    List<SearchAttr> attrList = attrValueEntities.stream().map(productAttrValueEntity -> {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttr;
                    }).collect(Collectors.toList());

                    goods.setAttrList(attrList);
                }

                //查询品牌并设置
                Resp<BrandEntity> brandEntityResp = this.pmsClient.queryBrandInfo(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goods.setBrandId(brandEntity.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                }

                //查询分类并设置
                Resp<CategoryEntity> categoryEntityResp = this.pmsClient.queryCategoryInfo(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(categoryEntity.getCatId());
                    goods.setCategoryName(categoryEntity.getName());
                }


                goods.setCreateTime(spuInfoEntity.getCreateTime());
                goods.setPic(skuInfoEntity.getSkuDefaultImg());

                goods.setPrice(skuInfoEntity.getPrice().doubleValue());

                goods.setSale(0L);
                goods.setSkuId(skuInfoEntity.getSkuId());

                goods.setTitle(skuInfoEntity.getSkuTitle());
                //查询库存信息
                Resp<List<WareSkuEntity>> listResp = this.wmsClient.queryWareSkusBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = listResp.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                }

                return goods;
            }).collect(Collectors.toList()));
        }
    }
}
