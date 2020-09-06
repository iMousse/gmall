package com.example.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.example.gmall.pms.vo.GroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.gmall.pms.entity.AttrGroupEntity;
import com.example.gmall.pms.service.AttrGroupService;


/**
 * 属性分组
 *
 * @author mousse
 * @email 958860184@qq.com
 * @date 2020-08-26 16:05:46
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<GroupVO>> queryGroupWithAttrsByCid(@PathVariable("catId") Long cid) {

        List<GroupVO> list = this.attrGroupService.queryGroupWithAttrsByCid(cid);

        return Resp.ok(list);
    }

    @ApiOperation("根据属性组id查询和属性组相关的属性")
    @ApiModelProperty(name = "gid", value = "分组id", required = true)
    @GetMapping("withattr/{gid}")
    public Resp<GroupVO> queryGroupWithAttrsByGid(@PathVariable("gid") Long gid) {

        GroupVO groupsVOS = this.attrGroupService.queryGroupWithAttrsByGid(gid);

        return Resp.ok(groupsVOS);
    }

    @ApiOperation("根据分类的id查询分类组的数据")
    @ApiModelProperty(name = "catId", value = "分类id", required = true)
    @GetMapping("{catId}")
    public Resp<PageVo> queryGroupByPage(QueryCondition condition, @PathVariable("catId") Long catId) {

        PageVo page = attrGroupService.getGroupByCidAndPage(condition, catId);

        return Resp.ok(page);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {

        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
