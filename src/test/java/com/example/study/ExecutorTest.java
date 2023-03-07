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
import org.apache.ibatis.executor.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutorTest {

  private static SqlSessionFactory sqlSessionFactory;

  private static SqlSession sqlSession;

  private static Configuration configuration;

  private static Connection connection;

  private static JdbcTransaction jdbcTransaction;

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
    sqlSession = sqlSessionFactory.openSession();
    connection = sqlSession.getConnection();
    jdbcTransaction = new JdbcTransaction(connection);

  }

  @Test
  void shouldGetUserArray() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User[] users = mapper.getUsers();
      assertEquals("User1", users[0].getName());
      assertEquals("User2", users[1].getName());
    }
  }

  @Test
  void simpleExecutorTest() throws Exception{
    SimpleExecutor executor = new SimpleExecutor(configuration, jdbcTransaction);
    MappedStatement mappedStatement = configuration.getMappedStatement("com.example.study.Mapper.getUsers");
    List<Object> objects = executor.doQuery(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(null));
    // 简单执行器：无论SQL是否一致，每次都会进行预编译（即这里会打印两次select * from users）
    executor.doQuery(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(null));
    System.out.println(objects);
  }

  @Test
  void reuseExecutorTest() throws Exception{
    // 可重用执行器，SQL相同时只会进行一次预编译（只打印一次select * from users，打印了两次parameter）
    ReuseExecutor executor = new ReuseExecutor(configuration, jdbcTransaction);
    MappedStatement mappedStatement = configuration.getMappedStatement("com.example.study.Mapper.getUsers");
    List<Object> objects = executor.doQuery(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(null));
    executor.doQuery(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER, mappedStatement.getBoundSql(null));
    System.out.println(objects);
  }

  @Test
  void batchExecutorTest() throws Exception{
    // 批处理执行器，执行查询操作时与SimpleExecutor一样，执行更新操作时可以批量执行
    BatchExecutor executor = new BatchExecutor(configuration, jdbcTransaction);
    MappedStatement mappedStatement = configuration.getMappedStatement("com.example.study.Mapper.updateNameById");
    Map<String, Object> map = new HashMap<>();
    map.put("name", "name");
    map.put("id", 1);
    executor.doUpdate(mappedStatement, map);
    map.put("id", 2);
    executor.doUpdate(mappedStatement, map);
    // 使用BatchExecutor时必须使用flush语句，上面的doUpdate只是设置参数的操作，并没有真正地执行。使用BatchExecutor时会自动关闭autocommit。
    executor.doFlushStatements(true);
  }

  @Test
  void baseExecutorTest() throws Exception{
    // Executor的实现类就有BaseExecutor，BaseExecutor实现了一级缓存，获取链接等公共操作
    Executor executor = new SimpleExecutor(configuration, jdbcTransaction);
    MappedStatement mappedStatement = configuration.getMappedStatement("com.example.study.Mapper.getUsers");
    // 只有这里会从数据库中取数据
    List<Object> objects = executor.query(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER);
    // 这里会从缓存中取数据
    executor.query(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER);
    System.out.println(objects);
  }

  @Test
  void cachingExecutorTest() throws Exception{
    Executor baseExecutor = new SimpleExecutor(configuration, jdbcTransaction);
    // 二级缓存CachingExecutor
    Executor executor = new CachingExecutor(baseExecutor);
    // 只有下面的getUsersXml方法开启了二级缓存
    MappedStatement mappedStatement = configuration.getMappedStatement("com.example.study.Mapper.getUsersXml");
    List<Object> objects = executor.query(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER);
    // 一级缓存与二级缓存不同，查询语句一旦查询到了，就会放置在一级缓存中，而二级缓存需要提交之后才会放置在二级缓存中。
    executor.commit(true);
    // 这里先走二级缓存，发现缓存存在，则会直接返回，并不会走一级缓存了
    executor.query(mappedStatement, null, RowBounds.DEFAULT, SimpleExecutor.NO_RESULT_HANDLER);
    System.out.println(objects);
  }

  @Test
  void sessionTest(){
    // debug时查看sqlSession结构可以发现：sqlSession默认使用DefaultSqlSession，他又包含了一个CachingExecutor，而CachingExecutor又包含了SimpleExecutor（父类为BaseExecutor）
    // sqlSession使用的是门面模式，提供一些增删改查的基本api和一些提交关闭会话等操作，内部的真实增删改查操作都是委派给了Executor实现
    List<Object> objects = sqlSession.selectList("com.example.study.Mapper.getUsersXml", null);
    System.out.println(objects);

    sqlSession.close();
    // 默认sqlSession使用的是SimpleExecutor，我们可以自己指定Executor类型：SIMPLE, REUSE, BATCH
    SqlSession reuseSqlSession = sqlSessionFactory.openSession(ExecutorType.REUSE, true);
  }

}
