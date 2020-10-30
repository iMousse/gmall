package com.example.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.example.gmall.index.service.IndexService;
import com.example.gmall.pms.entity.CategoryEntity;
import com.example.gmall.pms.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryCate1() {

        List<CategoryEntity> categoryEntities = this.indexService.queryCate1();

        return Resp.ok(categoryEntities);
    }

    @GetMapping("cates/{pid}")
    public Resp<List<CategoryVO>> querySubCategories(@PathVariable("pid") Long pid) {
        List<CategoryVO> categoryVOS = this.indexService.querySubCategories(pid);
        return Resp.ok(categoryVOS);
    }

    @GetMapping("testLock")
    public Resp<Object> testLock() {
        indexService.testLock();

        return Resp.ok(null);
    }

    /**
     * 读写锁
     * - 同时访问写：一个写完之后，等待一会儿（约10s），另一个写开始
     *
     * - 同时访问读：不用等待
     *
     * - 先写后读：读要等待（约10s）写完成
     *
     * - 先读后写：写要等待（约10s）读完成
     * @return
     */
    @GetMapping("read")
    public Resp<Object> read() {
        String msg = indexService.readLock();

        return Resp.ok(msg);
    }

    /**
     * 读写锁
     * @return
     */
    @GetMapping("write")
    public Resp<Object> write() {
        String msg = indexService.testWrite();
        return Resp.ok(msg);
    }



    @GetMapping("latch")
    public Resp<Object> latch() {
        String msg = indexService.testLatch();
        return Resp.ok(msg);
    }

    @GetMapping("count")
    public Resp<Object> count() {
        String msg = indexService.testCount();
        return Resp.ok(msg);
    }

}
