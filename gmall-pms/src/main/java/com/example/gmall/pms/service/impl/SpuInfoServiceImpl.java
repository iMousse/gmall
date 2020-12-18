package com.example.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gmall.pms.client.GmallSmsClient;
import com.example.gmall.pms.dao.SkuInfoDao;
import com.example.gmall.pms.dao.SpuInfoDao;
import com.example.gmall.pms.dao.SpuInfoDescDao;
import com.example.gmall.pms.entity.*;
import com.example.gmall.pms.service.*;
import com.example.gmall.pms.vo.BaseAttrVO;
import com.example.gmall.pms.vo.SkuInfoVO;
import com.example.gmall.pms.vo.SpuInfoVO;
import com.example.gmall.sms.vo.SkuSaleVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService descService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService attrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Override
    public PageVo queryPage(QueryCondition params) {

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuPage(QueryCondition condition, Long cid) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        //根据商品id或商品命进行查询，
        //select * from pms_spu_info where catelog_id = 225 and (id='iPhone' or spu_name like '%iPhone%')

        //判断id
        if (cid != null && cid != 0) {
            wrapper.eq("catalog_id", cid);
        }

        //关键字
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.like("id", key).or().like("spu_name", key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );
        return new PageVo(page);
    }

    @Override
    @GlobalTransactional//分布式事务
    public void saveSpuInfo(SpuInfoVO spuInfoVO) {
        //1.保存spu相关的3张表
        //1.1 保存pms_spu_info
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);


        //1.2 保存pms_spu_info_desc
        //事务传播需要不同的service,并且service默认是@Transactional(propagation = Propagation.REQUIRED)
        this.descService.saveSpuInfoDesc(spuInfoVO);

        //1.3 保存pms_product_attr_value
        this.saveSpuBaseAttrs(spuInfoVO);


        this.saveSkuInfoWhtiSaleInfo(spuInfoVO);


        sendMsg("insert", spuInfoVO.getId());
    }

    private void sendMsg(String type, Long spuId) {
        this.amqpTemplate.convertAndSend("GMALL-PMS-EXCHANGE", "item." + type, spuId);
    }

    private void saveSpuBaseAttrs(SpuInfoVO spuInfoVO) {
        List<BaseAttrVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //批量保存
            this.productAttrValueService.saveBatch(
                    //数据强转
                    baseAttrs.stream().map(baseAttrVO -> {
                        //设置spuId
                        baseAttrVO.setSpuId(spuInfoVO.getId());
                        return (ProductAttrValueEntity) baseAttrVO;
                    }).collect(Collectors.toList()));
        }
    }

    private void saveSkuInfoWhtiSaleInfo(SpuInfoVO spuInfoVO) {
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(sku -> {
            //2.保存pms_sku相关的3张表
            //2.1 保存pms_sku_info
            sku.setSpuId(spuInfoVO.getId());
            sku.setSkuCode(UUID.randomUUID().toString());
            sku.setBrandId(spuInfoVO.getBrandId());
            sku.setCatalogId(spuInfoVO.getCatalogId());
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                sku.setSkuDefaultImg(StringUtils.isNotBlank(sku.getSkuDefaultImg()) ? sku.getSkuDefaultImg() : images.get(0));
            }

            this.skuInfoDao.insert(sku);

            //获取skuId
            Long skuId = sku.getSkuId();

            //2.2 保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> imagesEntities = images.stream().map(image -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setImgUrl(image);
                    imagesEntity.setSkuId(skuId);
                    //设置默认图片，1为默认图片，0为不是
                    imagesEntity.setDefaultImg(StringUtils.equals(sku.getSkuDefaultImg(), image) ? 1 : 0);

                    return imagesEntity;
                }).collect(Collectors.toList());

                this.skuImagesService.saveBatch(imagesEntities);
            }

            //2.3 保存pms_sku_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(saleAttr -> saleAttr.setSkuId(skuId));
                attrValueService.saveBatch(saleAttrs);
            }

            //3.保存sms_sku信息的3张表 feign远程调用
            //3.1 保存sms_sku_bounds
            //3.2 保存sms_sku_ladder
            //3.3 保存sms_sku_full_reduction
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            BeanUtils.copyProperties(sku, skuSaleVO);
            skuSaleVO.setSkuId(skuId);
            this.gmallSmsClient.saveSales(skuSaleVO);
        });
    }

}