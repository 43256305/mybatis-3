package com.example.study;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/8 11:40
 * @description：
 * @modified By：
 * @version:
 */
public class Blog implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String publishDate;

  private String title;

  private User mainAuthor;

  private String body;

  private List<User> associationAuthor;

  private Map<String, String> label;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(String publishDate) {
    this.publishDate = publishDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public User getMainAuthor() {
    return mainAuthor;
  }

  public void setMainAuthor(User mainAuthor) {
    this.mainAuthor = mainAuthor;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public List<User> getAssociationAuthor() {
    return associationAuthor;
  }

  public void setAssociationAuthor(List<User> associationAuthor) {
    this.associationAuthor = associationAuthor;
  }

  public Map<String, String> getLabel() {
    return label;
  }

  public void setLabel(Map<String, String> label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return "Blog{" +
      "id=" + id +
      ", publishDate='" + publishDate + '\'' +
      ", title='" + title + '\'' +
      ", mainAuthor=" + mainAuthor +
      ", body='" + body + '\'' +
      ", associationAuthor=" + associationAuthor +
      ", label=" + label +
      '}';
  }
}
