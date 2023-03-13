package com.example.study;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/13 9:32
 * @description：
 * @modified By：
 * @version:
 */
public class BoundSqlTest {

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
//    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
//      "com/example/study/CreateDB.sql");
    sqlSession = sqlSessionFactory.openSession();
    configuration = sqlSessionFactory.getConfiguration();
    mapper = sqlSession.getMapper(Mapper.class);
  }

  @Test
  void ognlTest(){
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
    Blog blog = new Blog();
    // 判断对象某个属性是否为空
    System.out.println("单个属性：" + expressionEvaluator.evaluateBoolean("id != null || title != null", blog));
    // 调用方法
    blog.setCommentList(Collections.singletonList(new Comment()));
    System.out.println("调用方法：" + expressionEvaluator.evaluateBoolean("commentList != null && commentList.size() > 0", blog));
    // 访问数组
    blog.getCommentList().get(0).setId(1);
    System.out.println("集合访问：" + expressionEvaluator.evaluateBoolean("commentList != null && commentList[0].id != null ", blog));
    // 遍历集合
    Iterable<?> commentList = expressionEvaluator.evaluateIterable("commentList", blog);
    commentList.forEach(System.out::println);
  }

  /**
   * DynamicSqlSource使用解释器模式来解析sql，他包含了此SqlNode子类的树结构，每经过一个节点，都会拼接相应解析的sql到context，直到sql解析结束。
   * sql节点只有1对1的上下级关系，如果需要一对多，则需要将多个子节点用MixedSqlNode包装
   * */
  @Test
  void ifTest(){
    User user = new User();
    user.setId(1);
    user.setName("test");
    DynamicContext dynamicContext = new DynamicContext(configuration, user);
    // 执行静态sql逻辑，将sql解析到context中
    new StaticTextSqlNode("select * from users").apply(dynamicContext);
    //准备if子节点
    IfSqlNode ifSqlNode1 = new IfSqlNode(new StaticTextSqlNode("and id=#{id}"), "id!=null");
    IfSqlNode ifSqlNode2 = new IfSqlNode(new StaticTextSqlNode("and name=#{name}"), "name!=null");
    // 将多个if子节点包装到mixedSqlNode中
    MixedSqlNode mixedSqlNode = new MixedSqlNode(Arrays.asList(ifSqlNode1, ifSqlNode2));
    // 将多个if子节点包装的mixedSqlNode放入到where节点中。
    // 执行where/if sql逻辑   if逻辑中会判断id是否为null，来决定是否将这个语句加入到sql结尾
    // 先执行StaticTextSqlNode逻辑，在执行WhereSqlNode逻辑，最后将解析的sql加入到context中
    new WhereSqlNode(configuration, mixedSqlNode).apply(dynamicContext);
    System.out.println(dynamicContext.getSql());

  }

  @Test
  void foreachTest(){
    User[] userList = mapper.findByIds(Arrays.asList(1, 2));
    System.out.println(Arrays.toString(userList));
  }

}
