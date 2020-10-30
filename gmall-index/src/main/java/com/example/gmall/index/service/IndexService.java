package com.example.gmall.index.service;

import com.example.gmall.pms.entity.CategoryEntity;
import com.example.gmall.pms.vo.CategoryVO;

import java.util.List;

public interface IndexService {

    /**
     * 查询1级目录
     * @return
     */
    List<CategoryEntity> queryCate1();

    /**
     * 根据一级目录查询下面的目录
     * @param pid
     * @return
     */
    List<CategoryVO> querySubCategories(Long pid);

    /**
     * 单节点的锁的解决方案
     */
    void testLock();

    /**
     * 分布式锁的解决方案
     */
    void testLock2();

    /**
     * Redisson解决的分布式锁
     */
    void testRedisson();

    /**
     * 读写锁
     * @return
     */
    String readLock();

    /**
     * 读写锁
     * @return
     */
    String testWrite();

    String testLatch();

    String testCount();
}
