/**
 *    Copyright 2009-2015 the original author or authors.
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

/**
 * @author Clinton Begin
 * xjh-SqlNode总接口。DynamicSqlSource使用解释器模式来解析sql，他包含了此SqlNode子类的树结构，每经过一个节点，都会拼接相应解析的sql到context，直到sql解析结束。
 */
public interface SqlNode {
  // xjh-将解析的文本放入到context中，并返回是否解析成功
  boolean apply(DynamicContext context);
}
