package com.bdilab.storage.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteBucketVO {
    @NotNull
    @NotEmpty
//    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "请输入以字母开头的字母、数字、下划线和连字符的组合")
    @ApiModelProperty(name = "bucketName", value = "存储桶名称", dataType = "String", required = true, example = "消防文档库")
    String bucketName;
}