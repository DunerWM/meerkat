package com.meerkat.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.meerkat.base.db.annotation.EntityReference;
import com.meerkat.base.util.NormalDateSerializer;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by wm on 16/9/14.
 */
@Table
public class Blog {

    @Id
    private Long id;
    @Column
    private String title;
    @Column
    private String summary;
    @Column
    private String content;
    @Column
    private String bannerImage;
    @Column
    private Long author;
    @EntityReference(referenceProperty = "author", inverse = false)
    private User user;
    @Column
    private Long categoryId;
    @Column
    private int views;  // 阅读数
    @Column
    private int comments;  //评论数
    @Column
    private Boolean deleted;
    @JsonSerialize(using = NormalDateSerializer.class)
    @Column
    private Date createdAt;
    @Column
    private Date updatedAt;

    public Blog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }
}
