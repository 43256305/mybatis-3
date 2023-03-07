/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.study;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.Reader;
import java.util.List;

class FirstCacheTest {

  private static SqlSession sqlSession;

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("com/example/study/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    // populate database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "com/example/study/CreateDB.sql");
    sqlSession = sqlSessionFactory.openSession();
  }

  /**
   * 一级缓存命中条件：
   * 1.sql与参数相同
   * 2.相同的StatementId，即相同的mapper，相同的方法
   * 3.sqlSession必须一样（会话级缓存，sqlSession与Executor是一对一的关系）
   * 4.RowBounds返回范围必须相同
   * */
  @Test
  void test1(){
    Mapper mapper = sqlSession.getMapper(Mapper.class);
    User user1 = mapper.getUserById(1);
    User user2 = mapper.getUserById(1);
    // 命中一级缓存：sql和参数必须相同
    System.out.println("是否使用一级缓存：" + (user1 == user2));
    User user3 = mapper.getUserByIdTest(1);
    // 使用的sql与参数相同，但是mapper方法不同，则不会使用一级缓存
    System.out.println("不同mapper方法，是否使用一级缓存：" + (user1 == user3));
    User user4 = sqlSessionFactory.openSession().getMapper(Mapper.class).getUserById(1);
    // 使用不同的sqlSession时没有命中缓存
    System.out.println("不同sqlSession，是否使用一级缓存：" + (user1 == user4));
    Object user5 = sqlSession.selectOne("com.example.study.Mapper.getUserById", 1);
    System.out.println("sqlSession使用不同方法，是否使用一级缓存：" + (user1 == user5));
    // RowBounds即是分页参数
    List<Object> userList = sqlSession.selectList("com.example.study.Mapper.getUserById", 1, new RowBounds(0, 10));
    System.out.println("加上rowBound，是否使用一级缓存：" + (user1 == userList.get(0)));
    // 底层都使用了RowBounds.DEFAULT，所以能命中
    List<Object> userList1 = sqlSession.selectList("com.example.study.Mapper.getUserById", 1, RowBounds.DEFAULT);
    System.out.println("加上RowBounds.DEFAULT，是否使用一级缓存：" + (user1 == userList1.get(0)));
  }

  /**
   * 一级缓存命中条件：
   * 1.未调用Options.FlushCachePolicy.TRUE的查询
   * 2.未执行Update
   * 3.缓存作用于不是STATEMENT
   * 4.未手动清空缓存（提交，回滚）
   * */
  @Test
  void test2(){
    Mapper mapper = sqlSession.getMapper(Mapper.class);
    User user = mapper.getUserById(1);
    User user1 = mapper.getUserByIdOption(1);
    User user2 = mapper.getUserByIdOption(1);
    // 加了Options.FlushCachePolicy.TRUE后，每次查询后都会清空缓存
    System.out.println("加了Options.FlushCachePolicy.TRUE，是否使用一级缓存：" + (user1 == user2));
    User user3 = mapper.getUserById(1);
    // 可以看到，调用了Options.FlushCachePolicy.TRUE方法后，所有缓存都被清空了，等同于sqlSession.clearCache();
    System.out.println("中途调用了使用Options.FlushCachePolicy.TRUE的方法，缓存是否还生效：" + (user == user3));

    // 修改某条数据后（尽管修改的id与我们缓存的id不同，或者@Update中使用的是select语句），缓存还是会被清空
    mapper.updateNameById("name", 2);
    User user4 = mapper.getUserById(1);
    System.out.println("修改某条数据后，缓存是否还生效：" + (user3 == user4));
    // 这里清空了缓存
    sqlSession.commit();
  }

  @Test
  void test3(){
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc-config.xml");
    Mapper mapper = context.getBean(Mapper.class);
    // spring中mapper调用链：Mapper（dynamic proxy）->SqlSessionTemplate->SqlSessionInterceptor（dynamic proxy）->SqlSessionFactory
    // SqlSessionInterceptor.invoke()->SqlSessionUtils.getSqlSession()->TransactionSynchronizationManager.getResource()。
    // 通过TransactionSynchronizationManager获取sqlSession缓存（存储在ThreadLocal中），获取到了则返回，没有获取到则创建一个新的sqlSession
    User user1 = mapper.getUserById(1);
    User user2 = mapper.getUserById(1);
    // 可以看到，上面两次调用相同的sql与参数的情况下，并没有使用缓存
    // 因为每次调用mapper.getUserById(1)都会构造一个新的会话
    System.out.println("使用spring获取sqlSession时，是否使用缓存：" + (user1 == user2));

    // 手动开启事务
    DataSourceTransactionManager txManager = (DataSourceTransactionManager) context.getBean("txManager");
    TransactionStatus transactionStatus = txManager.getTransaction(new DefaultTransactionDefinition());
    User user3 = mapper.getUserById(1);
    User user4 = mapper.getUserById(1);
    // 可以看到，开启事务后，使用了缓存。
    System.out.println("使用spring开启事务时，是否使用缓存：" + (user3 == user4));

  }

}
