package com.bdilab.storage.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileVO {
    @NotNull
    @NotEmpty
//    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "请输入以字母开头的字母、数字、下划线和连字符的组合")
    @ApiModelProperty(name = "bucketName", value = "存储桶名称", dataType = "String", required = true, example = "消防文档库")
    String bucketName;

    @NotNull
    @NotEmpty
//    @Pattern(regexp = "^/([a-zA-Z][a-zA-Z0-9_-]*)+$", message = "请输入符合 POSIX 格式的路径")
    @ApiModelProperty(name = "objectName", value = "对象名称", dataType = "String", required = true, example = "支持格式: /2021年开发年报.docx、/开发年报/2021年开发年报.docx")
    String objectName;

    @NotNull
    @ApiModelProperty(name = "file", value = "文件对象", dataType = "file", required = true)
    MultipartFile file;

    @NotNull
    @ApiModelProperty(name = "isOverride", value = "是否覆盖, 默认为False", dataType = "Boolean", required = true, example = "False")
    Boolean isOverride;
}
