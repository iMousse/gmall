package com.example.gmall.pms.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.entity.AttrEntity;
import com.example.gmall.pms.service.AttrService;
import com.example.gmall.pms.vo.AttrVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


/**
 * 商品属性
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
@Api(tags = "商品属性 管理")
@RestController
@RequestMapping("pms/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @ApiOperation("根据分类条件和分类id和属性表的类别查询规格参数")
    @GetMapping
    public Resp<PageVo> queryByPageAndCidWithType(QueryCondition queryCondition,
                                                  @RequestParam(value = "cid", required = true) Long cid,
                                                  @RequestParam(value = "type", defaultValue = "1") Integer type) {

        PageVo page = attrService.queryByCidAndType(queryCondition, cid, type);
        return Resp.ok(page);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attr:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrId}")
    @PreAuthorize("hasAuthority('pms:attr:info')")
    public Resp<AttrEntity> info(@PathVariable("attrId") Long attrId) {
        AttrEntity attr = attrService.getById(attrId);

        return Resp.ok(attr);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attr:save')")
    public Resp<Object> save(@RequestBody AttrVO attr) {
        attrService.saveAttr(attr);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attr:update')")
    public Resp<Object> update(@RequestBody AttrVO attr) {
        attrService.updateAttrById(attr);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attr:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return Resp.ok(null);
    }

}
