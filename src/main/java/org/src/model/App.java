package org.src.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Application in the PlayStore.
 *
 * <p>This model class holds details about the app, such as its name, author, version, ratings, and
 * associated reviews.
 */
public final class App {
  private int id;

  @NotBlank(message = "App name is required")
  private String name;

  @NotNull(message = "Author information is required")
  private User author;

  @NotBlank(message = "Description cannot be empty")
  @Size(max = 200, message = "Description must not exceed 200 characters")
  private String description;

  @Positive(message = "Version must be a positive number")
  private double version;

  @NotEmpty(message = "Features list cannot be empty.")
  private List<String> features;

  private double rating = 0;
  private int installedCount = 0;
  private List<Review> reviews;

  /**
   * Default constructor required by Jackson for JSON deserialization.
   */
  public App() { }

  /**
   * Constructs a new App with specific details.
   *
   * @param id             Unique identifier for the app.
   * @param name           Name of the Application.
   * @param author         Author of the application.
   * @param description    Short description of the app.
   * @param version        Current Version Number.
   * @param features       List of application features.
   * @param rating         Application rating.
   * @param installedCount Total number of installations.
   */
  public App(
      final int id,
      final String name,
      User author,
      final String description,
      final double version,
      final List<String> features,
      final double rating,
      final int installedCount) {
    this.id = id;
    this.name = name;
    this.author = author;
    this.description = description;
    this.version = version;
    this.features = features;
    this.rating = rating;
    this.installedCount = installedCount;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(final User author) {
    this.author = author;
  }

  /**
   * Helper method to get the author's username safely.
   *
   * @return the username of the author
   */
  public String getAuthorName() {
    return author.getUsername();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public double getVersion() {
    return version;
  }

  public void setVersion(final double version) {
    this.version = version;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = features;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(final double rating) {
    this.rating = rating;
  }

  public int getInstalledCount() {
    return installedCount;
  }

  public void setInstalledCount(final int installedCount) {
    this.installedCount = installedCount;
  }

  public List<Review> getReviews() {
    return reviews;
  }

  public void setReviews(final List<Review> reviews) {
    this.reviews = reviews;
  }

  @Override
  public String toString() {
    final String authorName = (author != null) ? author.getUsername() : "Unknown";
    return "App{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", authorName='"
        + authorName
        + '\''
        + ", description='"
        + description
        + '\''
        + ", version="
        + version
        + ", rating="
        + rating
        + '}';
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    final App app = (App) object;

    return id == app.id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
