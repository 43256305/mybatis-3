/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.builder;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * xjh-将被DynamicSqlSource与RawSqlSource编译的sql包装成BoundSql
 * DynamicSqlSource、RawSqlSource、StaticSqlSource都实现了SqlSource接口
 */
public class StaticSqlSource implements SqlSource {

  private final String sql;
  // parameterMappings为所有参数与sql中?的对应关系，list中第一个值代表sql中第一个?的值
  private final List<ParameterMapping> parameterMappings;
  private final Configuration configuration;

  public StaticSqlSource(Configuration configuration, String sql) {
    this(configuration, sql, null);
  }

  public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.configuration = configuration;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // parameterMappings为所有参数与sql中?的对应关系，list中第一个值代表sql中第一个?的值
    // parameterObject经过前面ParamNameResolver.getNamedParams(args)解析的参数
    return new BoundSql(configuration, sql, parameterMappings, parameterObject);
  }

}
