/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * xjh-代表一个包含了<if>、<where>等标签的sql，每次执行sql语句都需要编译一次
 */
public class DynamicSqlSource implements SqlSource {

  private final Configuration configuration;
  // xjh-根节点，一般为MixedSqlNode
  private final SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  /**
   * 每次调用getBoundSql方法都会解析一遍sql。
   * */
  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // xjh-创建context
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    // 节点解析sql到context中。
    rootSqlNode.apply(context);
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    // 解析操作主要是完成一下两点：1.把#{}变成? 1.将#{}中的值拿出来变成ParameterMapping。生成的SqlSource，其实就是StaticSqlSource。
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    // 这里的sqlSource为上一步返回的StaticSqlSource，将我们ParamNameResolver解析出来的参数放置到boundSql中
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    // 将foreach中放置在binding中的参数全部放置到boundSql中。另外parameterObject还放置在了bindings中，key为：_parameter value：parameterObject
    context.getBindings().forEach(boundSql::setAdditionalParameter);
    return boundSql;
  }

}
