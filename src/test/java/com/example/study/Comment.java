package com.example.study;

import java.io.Serializable;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/9 9:39
 * @description：
 * @modified By：
 * @version:
 */
public class Comment implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private Integer blogId;

  private String comment;

  private User user;

  private Blog blog;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getBlogId() {
    return blogId;
  }

  public void setBlogId(Integer blogId) {
    this.blogId = blogId;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Blog getBlog() {
    return blog;
  }

  public void setBlog(Blog blog) {
    this.blog = blog;
  }

  @Override
  public String toString() {
    return "Comment{" +
      "id=" + id +
      ", blogId=" + blogId +
      ", comment='" + comment + '\'' +
      ", user=" + user +
      '}';
  }
}
