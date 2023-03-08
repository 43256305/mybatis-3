package com.example.study;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/7 16:49
 * @description：
 * @modified By：
 * @version:
 */
public class ParamResultTest {

  private static SqlSession sqlSession;

  private static SqlSessionFactory sqlSessionFactory;

  private static Mapper mapper;

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
    mapper = sqlSession.getMapper(Mapper.class);
  }

  @AfterAll
  static void close(){
    sqlSession.close();
  }

  @Test
  void paramTest(){
    User user = mapper.getByNameId( 1, "User1", RowBounds.DEFAULT, new User(3, "test"));
    System.out.println(user);
  }

  /**
   * 结果流向（获取结果的每一行都会经过这个链路）：ResultSetHandler（DefaultResultSetHandler）->ResultContext（DefaultResultContext）->ResultHandler（DefaultResultHandler）
   * */
  @Test
  void resultHandlerTest(){
    List<Object> list = new ArrayList<>();
    // 通过自定义ResultHandler来控制返回结果的数量。主要是使用resultContext用来控制当前结果的获取。
    sqlSession.select("com.example.study.Mapper.getUsers", (resultContext) -> {
      if (resultContext.getResultCount() > 0){
        // 一旦ResultCount > 0就停止
        resultContext.stop();
      }
      list.add(resultContext.getResultObject());
    });
    System.out.println(list);
  }

}
