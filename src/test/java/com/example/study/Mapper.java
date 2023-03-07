/**
 *    Copyright 2009-2016 the original author or authors.
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
package com.example.study;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface Mapper {

  @Select("select * from users")
  User[] getUsers();

  User[] getUsersXml();

  @Select("select id from users")
  Integer[] getUserIds();

  @Select("select id from users")
  int[] getUserIdsPrimitive();

  @Update("update users set name = #{name} where id = #{id}")
  void updateNameById(@Param("name") String name, @Param("id") Integer id);

  @Select("select * from users where id = #{id}")
  User getUserById(@Param("id") Integer id);

  @Select("select * from users where id = #{id}")
  User getUserByIdTest(@Param("id") Integer id);

  @Select("select * from users where id = #{id}")
  @Options(flushCache = Options.FlushCachePolicy.TRUE)
  User getUserByIdOption(@Param("id") Integer id);
}
