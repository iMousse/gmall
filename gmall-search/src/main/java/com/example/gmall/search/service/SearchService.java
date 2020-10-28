package com.example.gmall.search.service;

import com.example.gmall.search.pojo.Goods;
import com.example.gmall.search.vo.SearchParamVO;
import com.example.gmall.search.vo.SearchResponseAttrVO;
import com.example.gmall.search.vo.SearchResponseVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {


    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final Gson gson = new Gson();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SearchResponseVO search(SearchParamVO searchParamVO) throws IOException {
        SearchResponse response = this.restHighLevelClient.search(this.buildQueryDSL(searchParamVO), RequestOptions.DEFAULT);
        log.info("response:{}",response);

        SearchResponseVO responseVO = this.parseSearchResult(response);
        responseVO.setPageNum(searchParamVO.getPageNum());
        responseVO.setPageSize(searchParamVO.getPageSize());
        log.info("responseVO:{} ", responseVO);
        return responseVO;
    }

    private SearchResponseVO parseSearchResult(SearchResponse response) throws JsonProcessingException {
        SearchResponseVO responseVO = new SearchResponseVO();

        //获取总记录数
        SearchHits hits = response.getHits();
        responseVO.setTotal(hits.totalHits);

        //获取到聚合结果集
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        //解析品牌的聚合结果集
        SearchResponseAttrVO brand = new SearchResponseAttrVO();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<String> brandValue = brandIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            //获取品牌id
            map.put("id", bucket.getKeyAsString());
            //获取品牌
            Map<String, Aggregation> brandIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandIdSubMap.get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", brandName);

            try {
                return OBJECT_MAPPER.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return null;
        }).collect(Collectors.toList());

        brand.setName("品牌");
        brand.setValue(brandValue);
        responseVO.setBrand(brand);

        //获取分类的聚合结果集
        SearchResponseAttrVO catelog = new SearchResponseAttrVO();
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<String> cateValues = categoryIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            //获取分类id
            map.put("id", bucket.getKeyAsString());
            //获取分类name
            Map<String, Aggregation> categoryIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms categoryNameAgg = (ParsedStringTerms) categoryIdSubMap.get("categoryNameAgg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", categoryName);

            try {
                return OBJECT_MAPPER.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return null;
        }).collect(Collectors.toList());
        catelog.setName("分类");
        catelog.setValue(cateValues);
        responseVO.setCatelog(catelog);

        //解析规格参数
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg =attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            responseVO.setAttrs(buckets.stream().map(bucket -> {
                SearchResponseAttrVO responseAttrVO = new SearchResponseAttrVO();
                //设置规格参数id
                responseAttrVO.setProductAttributeId(bucket.getKeyAsNumber().longValue());
                //设置规格参数name
                List<? extends Terms.Bucket> attrNameAgg = ((ParsedStringTerms) (bucket.getAggregations().get("attrNameAgg"))).getBuckets();
                responseAttrVO.setName(attrNameAgg.get(0).getKeyAsString());

                List<? extends Terms.Bucket> attrValueAgg = ((ParsedStringTerms) attrNameAgg.get(0).getAggregations().get("attrValueAgg")).getBuckets();

                //设置规格参数list
//                List<? extends Terms.Bucket> attrValueAggs = ((ParsedStringTerms) (bucket.getAggregations().get("attrValueAgg"))).getBuckets();

                responseAttrVO.setValue(attrValueAgg.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));

                return responseAttrVO;
            }).collect(Collectors.toList()));
        }


        //解析商品
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        for (SearchHit subHit : subHits) {
            Goods goods = OBJECT_MAPPER.readValue(subHit.getSourceAsString(), new TypeReference<Goods>() {});
            goodsList.add(goods);
        }
        responseVO.setProducts(goodsList);

        return responseVO;
    }

    /**
     * @param searchParamVO
     * @return
     */
    private SearchRequest buildQueryDSL(SearchParamVO searchParamVO) {
        String keyword = searchParamVO.getKeyword();

        if (StringUtils.isEmpty(keyword)) {
            return null;
        }

        //1.条件查询构造器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        //1.1 构建查询条件
        bool.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //1.2 构建过滤条件
        //1.2.1 构建品牌过滤
        String[] brand = searchParamVO.getBrand();
        if (brand != null && brand.length != 0) {
            bool.filter(QueryBuilders.termsQuery("brandId", brand));
        }

        //1.2.2 构建分类过滤
        String[] catelog3 = searchParamVO.getCatelog3();
        if (catelog3 != null && catelog3.length != 0) {
            bool.filter(QueryBuilders.termsQuery("categoryId", catelog3));
        }

        //1.2.3 构建品牌嵌套分类
        String[] props = searchParamVO.getProps();
        if (props != null && props.length != 0) {
            for (String prop : props) {
                //需要对prop进行单独处理     1-attrId 2-attrValue  (以-进行分割 )
                String[] split = StringUtils.split(prop, ":");
                if (split == null || split.length != 2) {
                    continue;
                }
                //构建嵌套查询中的子查询
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termsQuery("attrList.attrId", split[0]));
                boolQuery.must(QueryBuilders.termsQuery("attrList.attrValue", StringUtils.split(split[1], "-")));

                //把嵌套查询放入到过滤器中
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrList", boolQuery, ScoreMode.None);
                bool.filter(nestedQuery);
            }

        }

        //1.2.4 价格范围过滤
        Integer priceFrom = searchParamVO.getPriceFrom();
        Integer priceTo = searchParamVO.getPriceTo();
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
        if (priceFrom != null) {
            rangeQuery.gte(priceFrom);
        }
        if (priceTo != null) {
            rangeQuery.lte(priceTo);
        }
        bool.filter(rangeQuery);


        sourceBuilder.query(bool);

        //2.构建分页
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);


        //3.构建排序
        String order = searchParamVO.getOrder();
        if (StringUtils.isNotEmpty(order)) {
            String[] split = StringUtils.split(order, ",");
            if (split != null && split.length == 2) {
                String field = null;
                switch (split[0]) {
                    case "1":field = "sale";break;
                    case "2":field = "price";break;
                }
                sourceBuilder.sort(field, StringUtils.equals("asc", split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }

        //4.构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));

        //5.构建聚合
        //5.1 品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));

        //5.2 分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        //5.3 规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrList")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrList.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrList.attrName")
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrList.attrValue")))));

        log.info("searchRequest:{}", sourceBuilder.toString());

        //6. 结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","pic","title","price"}, null);

        //查询参数
        SearchRequest searchRequest = new SearchRequest("goods").types("info");
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }
}
