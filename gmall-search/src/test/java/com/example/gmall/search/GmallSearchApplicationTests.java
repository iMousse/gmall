package com.example.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.entity.*;
import com.example.gmall.search.client.GmallPmsClient;
import com.example.gmall.search.client.GmallWmsClient;
import com.example.gmall.search.pojo.Goods;
import com.example.gmall.search.pojo.SearchAttr;
import com.example.gmall.search.repository.GoodsRepository;
import com.example.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    void createIndex() {
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
    }

    /**
     * 分页查询spu
     * 根据spuId查询spu下的sku
     * 根据品牌id查询品牌 brandName
     * 根据分类id查询分类 categoryName
     * 根据spuId查询该商品对应的搜索属性值 pms_attr   pms_product_attr_value
     */
    @Test
    void insertGoods() {
        Long pageNum = 1L;
        Long pageSize = 100L;
        do {
            //分页查询spu
            QueryCondition condition = new QueryCondition();
            condition.setPage(pageNum);
            condition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> spusByPage = this.pmsClient.querySpusByPage(condition);
            List<SpuInfoEntity> spus = spusByPage.getData();
            //遍历spu查询sku
            spus.forEach(spuInfoEntity -> {
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
            });


            //导入索引库
            pageSize = (long) spus.size();
            pageNum++;

        } while (pageSize == 100);
    }
}
