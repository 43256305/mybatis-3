package com.example.study.plugin;

/**
 * @author ：xjh
 * @date ：Created in 2023/3/14 9:10
 * @description：
 * @modified By：
 * @version:
 */
public class Page {

  private Integer total;

  private Integer page = 1;

  private Integer row = 10;

  public Page(Integer page, Integer row) {
    this.page = page;
    this.row = row;
  }

  public Page() {
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getRow() {
    return row;
  }

  public void setRow(Integer row) {
    this.row = row;
  }

  @Override
  public String toString() {
    return "Page{" +
      "total=" + total +
      ", page=" + page +
      ", row=" + row +
      '}';
  }
}
