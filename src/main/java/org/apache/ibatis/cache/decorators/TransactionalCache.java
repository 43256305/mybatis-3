/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session.
 * Entries are sent to the cache when commit is called or discarded if the Session is rolled back.
 * Blocking cache support has been added. Therefore any get() that returns a cache miss
 * will be followed by a put() so any lock associated with the key can be released.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class TransactionalCache implements Cache {

  //xjh-此类中所有操作，除了commit，都是对暂存区的操作，而没有修改缓存区
  // 另外，TransactionalCache与TransactionalCacheManager类都没有update操作，CachingExecutor的update操作都是直接操作操作BaseExecutor，并且根据flushCacheRequired判断是否清空暂存区。
  // 查询操作时（getObject），我们可以看到，是直接从缓存区（delegate）中取数据的，而没有从暂存区取数据。

  private static final Log log = LogFactory.getLog(TransactionalCache.class);

  // xjh-二级缓存的缓存区，一般为SynchronizedCache类
  private final Cache delegate;
  // xjh-缓存是否清空标记。比如在同一个事务中，我们先修改某条数据，再查询某条数据，而因为修改之后只有提交了才能清空缓存区，所以查询时我们会查询到修改之前的数据。因此我们需要这样一个标志，在更新之后将此标志置位true。
  private boolean clearOnCommit;
  // xjh-二级缓存的暂存区
  private final Map<Object, Object> entriesToAddOnCommit;
  private final Set<Object> entriesMissedInCache;

  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public Object getObject(Object key) {
    // issue #116
    // xjh-从缓存区（SynchronizedCache）获取数据
    Object object = delegate.getObject(key);
    if (object == null) {
      // 如果没有获取到，则记录此key缺失
      entriesMissedInCache.add(key);
    }
    // issue #146
    // clearOnCommit为缓存是否清空标记。比如在同一个事务中，我们先修改某条数据，再查询某条数据，而因为修改之后只有提交了才能清空缓存区，所以查询时我们会查询到修改之前的数据。因此我们需要这样一个标志，在更新之后将此标志置位true。
    // 这样就算我们从缓存区查询到了数据，我们也会返回null。
    if (clearOnCommit) {
      return null;
    } else {
      return object;
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    // xjh-将数据放入暂存区，注意，此时只是简单地将数据放入到暂存区中，缓存区中还没有
    entriesToAddOnCommit.put(key, object);
  }

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  @Override
  public void clear() {
    // xjh-清空操作：将clearOnCommit置位true，并且清空暂存区，所以清空操作并没有修改缓存区。
    clearOnCommit = true;
    entriesToAddOnCommit.clear();
  }

  public void commit() {
    // xjh-提交时，先要判断clearOnCommit是否为true，如果是，则要清空缓存区。
    if (clearOnCommit) {
      delegate.clear();
    }
    // 将数据刷新到缓存区中，如果clearOnCommit为true，则entriesToAddOnCommit也会为空，所以并不会将数据刷新进去。
    // 而如果clearOnCommit为false，且entriesToAddOnCommit有值时，才会将数据刷新到缓存区中。
    flushPendingEntries();
    // 重置暂存区
    reset();
  }

  public void rollback() {
    unlockMissedEntries();
    reset();
  }

  private void reset() {
    // xjh-reset操作就是重置了暂存区，而没有影响缓存区
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  private void flushPendingEntries() {
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    for (Object entry : entriesMissedInCache) {
      if (!entriesToAddOnCommit.containsKey(entry)) {
        delegate.putObject(entry, null);
      }
    }
  }

  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
            + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
      }
    }
  }

}
