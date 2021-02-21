package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 把一个spu解析成goods
     */
    public Goods buildGoods(Spu spu) throws IOException {
        //查询分类名
        List<String> names = categoryClient.findNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询品牌
        Brand brand = brandClient.findBrandById(spu.getBrandId());
        //查询spu对应的sku
        List<Sku> skus = goodsClient.findSkusBySpuId(spu.getId());
        //遍历skus拿到价格
        List<Long> prices = new ArrayList<>();
        //初始化sku列表，收集必要信息
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            //一个sku有多个图片，只取第一个
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuMapList.add(map);
        });
        //获取规格参数对象
        List<SpecParam> params = specificationClient.findParams(null, spu.getCid3(), null, true);
        //获取参数详情
        SpuDetail spuDetail = goodsClient.findSpuDetailBySpuId(spu.getId());
        //把通用的规格参数值反序列化为一个对象
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        //把特殊的规格参数值反序列化为一个对象
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });
        //初始化规格参数
        Map<String, Object> specs = new HashMap<>();
        params.forEach(param -> {
            //判断参数的类型是否通用
            if (param.getGeneric()) {
                //从通用规格参数获取一个值
                String value = genericSpecMap.get(param.getId().toString()).toString();
                //判断是否是数值，返回一个区间
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            } else {
                //从特殊规格参数获取一个值
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(), value);
            }
        });

        Goods g = new Goods();
        g.setId(spu.getId());
        g.setSubTitle(spu.getSubTitle());
        g.setCreateTime(spu.getCreateTime());
        g.setCid1(spu.getCid1());
        g.setCid3(spu.getCid2());
        g.setCid3(spu.getCid3());
        g.setBrandId(spu.getBrandId());
        //需要标题，分类名，品牌名称
        g.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand.getName());
        //设置价格
        g.setPrice(prices);
        //设置sku信息
        g.setSkus(MAPPER.writeValueAsString(skuMapList));
        //设置规格参数
        g.setSpecs(specs);
        return g;
    }

    /**
     * 判断value在哪个区间
     */
    private String chooseSegment(String value, SpecParam specParam) {
        double val = NumberUtils.toDouble(value);
        String res = "其他";
        for (String segment : specParam.getSegments().split(",")) {
            String[] split = segment.split("-");
            //读取数值范围
            double begin = NumberUtils.toDouble(split[0]);
            double end = Double.MAX_VALUE;
            if (split.length == 2) end = NumberUtils.toDouble(split[1]);
            //判断是否在范围
            if (val >= begin && val < end) {
                if (split.length == 1) res = split[0] + specParam.getUnit() + "以上";
                else if (begin == 0) res = split[1] + specParam.getUnit() + "以下";
                else res = segment + specParam.getUnit();
                break;
            }
        }
        return res;
    }

    /**
     * 分页搜索
     */
    public SearchResult search(SearchRequest request) {
        if (request == null || StringUtils.isBlank(request.getKey()))
            return null;
        //自定义查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加条件
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        BoolQueryBuilder basicQuery = buildBoolQueryBuilder(request);
        queryBuilder.withQuery(basicQuery);
        //添加分页
        queryBuilder.withPageable(PageRequest.of(request.getPage() - 1, request.getSize()));
        //结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        //添加分类和品牌的聚合
        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询
        AggregatedPage<Goods> gp = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //获取聚合结果并解析
        List<Map<String, Object>> categories = getCategoryAggResult(gp.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAggResult(gp.getAggregation(brandAggName));
        //判断是否只有一个分类时进行聚合，否则规格参数过多无意义
        List<Map<String, Object>> specs = null;
        if (!CollectionUtils.isEmpty(categories) && categories.size() == 1) {
            //对规格参数进行聚合
            specs = getParamAggResult((Long) categories.get(0).get("id"), basicQuery);
        }
        //打包分页结果集
        return new SearchResult(gp.getTotalElements(), gp.getTotalPages(), gp.getContent(), categories, brands, specs);
    }

    /**
     * 构建bool查询
     */
    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //添加基本查询
        boolQuery.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        //获取过滤条件map，添加过滤条件
        request.getFilter().forEach((searchName, searchValue) -> {
            String key;
            if (StringUtils.equals(searchName, "分类")) {
                key = "cid3";
            } else if (StringUtils.equals(searchName, "品牌")) {
                key = "brandId";
            } else {
                key = "specs." + searchName + ".keyword";
            }
            boolQuery.filter(QueryBuilders.termQuery(key, searchValue));
        });
        return boolQuery;
    }

    /**
     * 对规格参数进行聚合
     */
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询条件
        queryBuilder.withQuery(basicQuery);
        //查询参加聚合的规格参数集合
        List<SpecParam> params = specificationClient.findParams(null, cid, null, true);
        //遍历参数集合添加规格参数的聚合
        params.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        });
        //过滤结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //查询聚合
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //解析聚合
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        List<Map<String, Object>> specs = new ArrayList<>();
        aggregationMap.forEach((name, agg) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            //把聚合对象强转为StringTerms后解析为获取桶中的聚合词条
            List<String> options = ((StringTerms) agg).getBuckets().stream().map(StringTerms.Bucket::getKeyAsString).collect(Collectors.toList());
            map.put("options", options);
            specs.add(map);
        });
        return specs;
    }

    /**
     * 把分类的聚合解析为List<Map<String, Object>>
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        return ((LongTerms) aggregation).getBuckets().stream().map(bucket -> {
            //初始化一个map
            Map<String, Object> map = new HashMap<>();
            //获取桶中的分类id
            Long id = bucket.getKeyAsNumber().longValue();
            //根据id查询分类名称
            String name = categoryClient.findNamesByIds(Collections.singletonList(id)).get(0);
            map.put("id", id);
            map.put("name", name);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 把品牌的聚合解析为List<Brand>
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        return ((LongTerms) aggregation).getBuckets().stream().map(
                //获取桶中的所有品牌id查询桶并转化为品牌集合返回
                bucket -> brandClient.findBrandById(bucket.getKeyAsNumber().longValue())
        ).collect(Collectors.toList());
    }
}
