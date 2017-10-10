package com.team3s.lostpropertyse.Chat;

import java.util.Date;

/**
 * Created by MERİÇ BERBER on 10/10/17.
 */

public class CommentModel {

  private String commentText;
  private Date commentDate;
  private String commentUserUid;
  private String commentId;
  private String commentUsername;

  public CommentModel() {
  }

  public CommentModel(String commentText, Date commentDate, String commentUserUid, String commentId,String commentUsername) {
    this.commentText = commentText;
    this.commentDate = commentDate;
    this.commentUserUid = commentUserUid;
    this.commentId = commentId;
    this.commentUsername = commentUsername;
  }

  public String getCommentText() {
    return commentText;
  }

  public void setCommentText(String commentText) {
    this.commentText = commentText;
  }

  public Date getCommentDate() {
    return commentDate;
  }

  public void setCommentDate(Date commentDate) {
    this.commentDate = commentDate;
  }

  public String getCommentUserUid() {
    return commentUserUid;
  }

  public void setCommentUserUid(String commentUserUid) {
    this.commentUserUid = commentUserUid;
  }

  public String getCommentId() {
    return commentId;
  }

  public void setCommentId(String commentId) {
    this.commentId = commentId;
  }

  public String getCommentUsername() {
    return commentUsername;
  }

  public void setCommentUsername(String commentUsername) {
    this.commentUsername = commentUsername;
  }
}
