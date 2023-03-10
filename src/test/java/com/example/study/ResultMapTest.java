package com.example.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/9 9:49
 * @description：
 * @modified By：
 * @version:
 */
public class ResultMapTest {

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
  void resultMapTest(){
    Blog blog = mapper.getBlogById(1);
    System.out.println(blog);
  }

  @Test
  void lazyLoadTest() {
    Blog blog = mapper.getLazyBlogById(1);
    // 注意，任意调用get方法都会触发懒加载，所以我们在调试时只要查看了blog类的属性，就会触发懒加载，我们只有先把此属性设置为null，然后打印就会发现，commentList没有加载。
    // 我们可以通过调试时不查看blog对象的属性，查看控制台sql输出来判断有没有进行懒加载。
    blog.setCommentList(null);
    System.out.println(blog);
  }

  @Test
  void lazyLoadTest2() throws JsonProcessingException {
    Blog blog1 = mapper.getLazyBlogById(1);
    ObjectMapper objectMapper = new ObjectMapper();
    // 这里会报错，使用jackson对懒加载对象序列化不会成功。可以使用java原生的序列化
    String s = objectMapper.writeValueAsString(blog1);
    System.out.println(s);
  }


}
