package org.src.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * Represents a user review for a specific application.
 *
 * <p>Encapsulates details such as rating, user comment, and the identity of the reviewer.
 */
public final class Review {
  public static final int MIN_RATING = 1;
  public static final int MAX_RATING = 5;

  private int id;

  @Positive(message = "User ID must be valid")
  private int userId;

  private String userName;

  @Positive(message = "App ID must be valid")
  private int appId;

  @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
  @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
  private double rating;

  @NotBlank(message = "Review comment cannot be empty")
  @Size(max = 200, message = "Comment must not exceed 200 characters")
  private String comment;

  /**
   * Default constructor required for JSON deserialization.
   */
  public Review() { }

  /**
   * Constructs a new Review instance with all details.
   *
   * @param id       the unique ID of the review
   * @param userId   the ID of the user creating the review
   * @param userName the display name of the reviewer
   * @param appId    the ID of the application being reviewed
   * @param rating   the numeric rating
   * @param comment  the textual feedback provided by the user
   */
  public Review(
      final int id,
      final int userId,
      final String userName,
      final int appId,
      final double rating,
      final String comment) {
    this.id = id;
    this.userId = userId;
    this.userName = userName;
    this.appId = appId;
    this.rating = rating;
    this.comment = comment;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(final int userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(final String userName) {
    this.userName = userName;
  }

  public int getAppId() {
    return appId;
  }

  public void setAppId(final int appId) {
    this.appId = appId;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(final double rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  @Override
  public String toString() {
    return userName + ": " + rating + "* - " + comment;
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    final Review review = (Review) object;

    return id == review.id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
