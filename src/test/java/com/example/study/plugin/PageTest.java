package com.example.study.plugin;

import com.example.study.Mapper;
import com.example.study.User;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/14 10:24
 * @description：
 * @modified By：
 * @version:
 */
public class PageTest {

  private static SqlSession sqlSession;

  private static SqlSessionFactory sqlSessionFactory;

  private static Configuration configuration;

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
    configuration = sqlSessionFactory.getConfiguration();
    mapper = sqlSession.getMapper(Mapper.class);
  }

  @Test
  void test(){
    Page page = new Page(2, 1);
    User[] user = mapper.findByPage(page, "User1");
    System.out.println(Arrays.toString(user));
    System.out.println("总数量为：" + page.getTotal());
  }

  @Test
  void pluginTest(){
    // 自定义
    MyPluginInterface myPlugin = (MyPluginInterface) Plugin.wrap(new MyPluginClass(), new MyInterceptor());
    myPlugin.sayHello();

  }

  public interface MyPluginInterface{
    void sayHello();
  }

  public class MyPluginClass implements MyPluginInterface{
    @Override
    public void sayHello() {
      System.out.println("hello world!");
    }
  }

  @Intercepts(@Signature(type = MyPluginInterface.class, method = "sayHello", args = {}))
  public class MyInterceptor implements Interceptor{

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
      System.out.println("pre action");
      return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
      return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
      Interceptor.super.setProperties(properties);
    }
  }



}
