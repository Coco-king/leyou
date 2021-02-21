package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.commom.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 分页查询spu
     *
     * @param key      查询条件
     * @param saleable 是否上架
     * @param page     当前页
     * @param rows     每页条数
     * @return 分页结果集
     */
    public PageResult<SpuBo> findSpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //添加过滤条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //判断是否上架
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //判断是否删除
        criteria.andEqualTo("valid", true);
        // 分页
        if (rows > 0)
            PageHelper.startPage(page, rows);
        //执行查询
        List<Spu> spuList = spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spuList);
        //处理结果集
        List<SpuBo> spuBos = spuList.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);
            //查询品牌名字
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            //查询分类名字
            List<String> names = categoryService.findNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, " — "));
            return spuBo;
        }).collect(Collectors.toList());
        //返回结果
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    /**
     * 保存商品
     *
     * @param spuBo 商品spu
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //添加spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insertSelective(spuBo);
        //添加spu_detail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);
        saveSkuAndStock(spuBo);
        //发送消息
        sendMsg("insert", spuBo.getId());
    }

    /**
     * 保存sku和库存
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            //添加sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            skuMapper.insertSelective(sku);
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            //添加sku_stock
            stockMapper.insertSelective(stock);
        });
    }

    /**
     * 根据SpuId查询SpuDetail
     */
    public SpuDetail findSpuDetailBySpuId(Long spuId) {
        return spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spu的id查询到sku
     */
    public List<Sku> findSkusBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(record);
        skus.forEach(sku -> {
            Stock stock = stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 根据spu的id查询到spu
     */
    public Spu findSpuBySpuId(Long spuId) {
        return spuMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 修改商品
     *
     * @param spuBo 商品spu
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //删除sku
        deleteSku(spuBo.getId());
        //新增sku和stock
        saveSkuAndStock(spuBo);
        //更新spu和spuDetail
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        spuMapper.updateByPrimaryKeySelective(spuBo);
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        //发送消息
        sendMsg("update", spuBo.getId());
    }

    /**
     * 根据spuId更新spu，修改数据库中的商品状态
     */
    @Transactional
    public void deleteSpuBySpuId(Long spuId) {
        //更新sku
        Sku record = new Sku();
        record.setEnable(false);
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spuId);
        skuMapper.updateByExampleSelective(record, example);
        Spu spu = new SpuBo();
        spu.setId(spuId);
        spu.setValid(false);
        spuMapper.updateByPrimaryKeySelective(spu);
        //发送消息
        sendMsg("delete", spuId);
    }

    /**
     * 根据spu的id删除spu和stock
     */
    private void deleteSku(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(record);
        skus.forEach(sku -> {
            //删除stock
            stockMapper.deleteByPrimaryKey(sku.getId());
        });
        //删除sku
        skuMapper.delete(record);
    }

    /**
     * 根据spuId更改spu上下架
     */
    @Transactional
    public void updateSpuBySpuId(Long spuId, Boolean saleable) {
        Spu record = new Spu();
        record.setId(spuId);
        record.setSaleable(saleable);
        spuMapper.updateByPrimaryKeySelective(record);
        //发送消息
        sendMsg("update", spuId);
    }

    /**
     * 发送消息到rabbitmq
     */
    private void sendMsg(String type, Long id) {
        try {
            //捕获异常，消息发送成功与否不能影响方法本身的事务
            amqpTemplate.convertAndSend("item." + type, id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据sku的id查询到sku
     */
    public Sku findSkuBySkuId(Long skuId) {
        return skuMapper.selectByPrimaryKey(skuId);
    }
}
