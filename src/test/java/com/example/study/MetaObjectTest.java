package com.example.study;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/8 11:35
 * @description：
 * @modified By：
 * @version:
 */
public class MetaObjectTest {

  @Test
  void test1(){
    // 装饰器模式
    Blog blog = new Blog();
    Configuration configuration = new Configuration();
    MetaObject metaObject = configuration.newMetaObject(blog);
    // 对象属性赋值
    metaObject.setValue("title", "titleTest");
    System.out.println(blog);
    // 嵌套对象属性（会自动生成嵌套对象）
    metaObject.setValue("mainAuthor.id", 1);
    System.out.println(blog);
    blog.setAssociationAuthor(Collections.singletonList(new User(1, "test")));
    // 访问数组
    System.out.println(metaObject.getValue("associationAuthor[0].id"));
    // 支持命名
    String publishDate = metaObject.findProperty("publish_date", true);
    metaObject.setValue(publishDate, "2022");
    System.out.println(metaObject.getValue(publishDate));
  }

}
