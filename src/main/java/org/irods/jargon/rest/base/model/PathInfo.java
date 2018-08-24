package org.irods.jargon.rest.base.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * PathInfo
 */
@Validated

public class PathInfo   {
  @JsonProperty("absolutePath")
  private String absolutePath = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("modifyDate")
  private String modifyDate = null;

  @JsonProperty("createDate")
  private String createDate = null;

  public PathInfo absolutePath(String absolutePath) {
    this.absolutePath = absolutePath;
    return this;
  }

  /**
   * Get absolutePath
   * @return absolutePath
  **/
  @ApiModelProperty(example = "/zone/home/user/file1", required = true, value = "")
  @NotNull


  public String getAbsolutePath() {
    return absolutePath;
  }

  public void setAbsolutePath(String absolutePath) {
    this.absolutePath = absolutePath;
  }

  public PathInfo name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "file1", required = true, value = "")
  @NotNull


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PathInfo modifyDate(String modifyDate) {
    this.modifyDate = modifyDate;
    return this;
  }

  /**
   * Get modifyDate
   * @return modifyDate
  **/
  @ApiModelProperty(example = "2016-08-29T09:12:33.001Z", value = "")


  public String getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(String modifyDate) {
    this.modifyDate = modifyDate;
  }

  public PathInfo createDate(String createDate) {
    this.createDate = createDate;
    return this;
  }

  /**
   * Get createDate
   * @return createDate
  **/
  @ApiModelProperty(example = "2016-08-29T09:12:33.001Z", value = "")


  public String getCreateDate() {
    return createDate;
  }

  public void setCreateDate(String createDate) {
    this.createDate = createDate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathInfo pathInfo = (PathInfo) o;
    return Objects.equals(this.absolutePath, pathInfo.absolutePath) &&
        Objects.equals(this.name, pathInfo.name) &&
        Objects.equals(this.modifyDate, pathInfo.modifyDate) &&
        Objects.equals(this.createDate, pathInfo.createDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(absolutePath, name, modifyDate, createDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathInfo {\n");
    
    sb.append("    absolutePath: ").append(toIndentedString(absolutePath)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    modifyDate: ").append(toIndentedString(modifyDate)).append("\n");
    sb.append("    createDate: ").append(toIndentedString(createDate)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

