package com.example.study.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/14 9:11
 * @description：分页插件
 * @modified By：
 * @version:
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class PagePlugin implements Interceptor {


  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 1.检测当前是否满足分页条件
    StatementHandler target = (StatementHandler) invocation.getTarget();
    BoundSql boundSql = target.getBoundSql();
    Object parameterObject = boundSql.getParameterObject();
    Page page = null;
    if (parameterObject instanceof Page){
      page = (Page) parameterObject;
    }else if (parameterObject instanceof Map){
      page = (Page) ((Map<?, ?>) parameterObject).values().stream().filter(v -> v instanceof Page).findFirst().orElse(null);
    }
    if (page == null){
      invocation.proceed();
    }
    // 2.设置总行数total
    page.setTotal(selectCount(invocation));
    // 3.修改原有sql，添加分页
    String sql = String.format("%s limit %s , %s", boundSql.getSql(), (page.getPage() - 1) * page.getRow(), page.getRow());
    SystemMetaObject.forObject(boundSql).setValue("sql", sql);
    return invocation.proceed();
  }

  private int selectCount(Invocation invocation) throws SQLException {
    StatementHandler target = (StatementHandler) invocation.getTarget();
    String sql = String.format("select count(*) from (%s) as _page", target.getBoundSql().getSql());

    Connection connection = (Connection) invocation.getArgs()[0];
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    // 设置参数
    target.parameterize(preparedStatement);

    ResultSet resultSet = preparedStatement.executeQuery();
    int count = 0;
    if (resultSet.next()){
      count = resultSet.getInt(1);
    }
    resultSet.close();
    return count;
  }

  // 此方法用于实现动态代理，返回代理之后的对象。所有能代理的对象都会经过这里，所以我们必须要加上@Intercepts，指定我们要拦截的类、方法。
  @Override
  public Object plugin(Object target) {
    // 我们可以在这里包装我们自己的类，此类调用target的相关方法，在调用前后调用一些自定义逻辑。但是需要判断传入的是自己所需的类。使用这种方法不需要代理。
//    if (target instanceof CachingExecutor){
//      CachingExecutor executor = (CachingExecutor) target;
//      return new Executor() {
//        @Override
//        public int update(MappedStatement ms, Object parameter) throws SQLException {
//          return executor.update(ms, parameter);
//        }
//
//        @Override
//        public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
//          return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
//        }
//
//        @Override
//        public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
//          return executor.query(ms, parameter, rowBounds, resultHandler);
//        }
//
//        @Override
//        public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
//          return null;
//        }
//
//        @Override
//        public List<BatchResult> flushStatements() throws SQLException {
//          return null;
//        }
//
//        @Override
//        public void commit(boolean required) throws SQLException {
//
//        }
//
//        @Override
//        public void rollback(boolean required) throws SQLException {
//
//        }
//
//        @Override
//        public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
//          return null;
//        }
//
//        @Override
//        public boolean isCached(MappedStatement ms, CacheKey key) {
//          return false;
//        }
//
//        @Override
//        public void clearLocalCache() {
//
//        }
//
//        @Override
//        public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
//
//        }
//
//        @Override
//        public Transaction getTransaction() {
//          return null;
//        }
//
//        @Override
//        public void close(boolean forceRollback) {
//
//        }
//
//        @Override
//        public boolean isClosed() {
//          return false;
//        }
//
//        @Override
//        public void setExecutorWrapper(Executor executor) {
//
//        }
//      }
//    }
    return Interceptor.super.plugin(target);
  }

  @Override
  public void setProperties(Properties properties) {
    Interceptor.super.setProperties(properties);
  }
}
