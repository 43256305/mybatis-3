package com.example.study;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/7 10:06
 * @description：
 * @modified By：
 * @version:
 */
public class SecondCacheTest {

  private static SqlSessionFactory sqlSessionFactory;

  private static Configuration configuration;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("com/example/study/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    // populate database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "com/example/study/CreateDB.sql");
    configuration = sqlSessionFactory.getConfiguration();
  }

  /**
   * 二级缓存也称之为应用级缓存，与一级缓存不同的是他的作用范围是整个应用，可以跨线程使用。所以二级缓存有更高命中率，适合缓存一些修改较少的数据。
   *
   * 二级缓存特性：
   * 1存储：内存、硬盘、redis中。2.淘汰算法：FIFO、LRU。3.过期清理。4.线程安全。5.命中率统计。6.序列化（跨线程使用某个对象，则不可能使用同一个对象，一定要序列化）。
   * 使用责任链模式实现以上功能，不同的Cache子类实现不同的功能。
   *
   * 缓存顶层接口：org.apache.ibatis.cache.Cache
   * 缓存子类：SynchronizedCache：线程同步-->LoggingCache：记录命中率-->LruCache：LRU算法-->ScheduledCache：过期清理-->BlockingCache：防穿透-->PerpetualCache：内存存储
   * 每一个缓存都持有下一个缓存对象，当前处理完成后，把请求交给下一个缓存处理。
   * */
  @Test
  void test1(){
    Cache cache = configuration.getCache("com.example.study.Mapper");
    User user1 = new User(2, "name");
    cache.putObject("test", user1);
    User user2 = (User) cache.getObject("test");
    // 根据console打印Cache Hit Ratio [com.example.study.Mapper]: 1.0 可以知道user2是从缓存中取的。
    // 但是user1并不会==user2，因为user2是序列化之后生成的
    System.out.println(user1 == user2);

  }

  /**
   * 二级缓存命中条件：
   * 1.会话提交后
   * 2.sql语句、参数相同
   * 3.statementId相同
   * 5.RowBounds相同
   *
   * 配置：
   * 全局缓存开关：cacheEnabled
   * statement缓存开关：useCache
   * 缓存清除：flushCache，清空所有缓存
   * 声明缓存空间：<cache/>或者@CacheNamespace
   * 引用缓存空间：<cache-ref/>或者@CacheNamespaceRef
   *
   * 二级默认缓存默认是不开启的，需要为其声明缓存空间才可以使用，通过@CacheNamespace 或 为指定的MappedStatement声明。
   * 声明之后该缓存为该Mapper所独有，其它Mapper不能访问。如需要多个Mapper共享一个缓存空间可通过@CacheNamespaceRef 或进行引用同一个缓存空间。
   *
   * 一个sqlSession对应一个CachingExecutor对应一个TransactionalCacheManager，对应多个暂存区（TransactionalCache），对应多个缓存区（SynchronizedCache）。
   * 暂存区与缓存区为一对一的关系，每个缓存空间(Mapper)对应一个暂存区一个缓存区。即如果一个sqlSession中使用了多个Mapper，则会存在多个TransactionalCache。
   * sqlSession销毁后，TransactionalCacheManager、TransactionalCache都会被销毁。
   * 只有事务提交后，暂存区中的缓存才会放置到缓存区中。
   * */
  @Test
  void test2(){
    SqlSession sqlSession1 = sqlSessionFactory.openSession(true);
    Mapper mapper1 = sqlSession1.getMapper(Mapper.class);
    mapper1.getUsersXml();
//    就算开启了自动提交，也是要提交之后才能命中缓存
    sqlSession1.commit();

    SqlSession sqlSession2 = sqlSessionFactory.openSession(true);
    Mapper mapper2 = sqlSession2.getMapper(Mapper.class);
    mapper2.getUsersXml();

  }

}
