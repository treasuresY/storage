package com.bdilab.storage.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFileOrFolderVO {
    @NotNull
    @NotEmpty
    @ApiModelProperty(name = "bucketName", value = "存储桶名称", dataType = "String", required = true, example = "消防文档库")
    String bucketName;

    @NotNull
    @NotEmpty
    @ApiModelProperty(name = "objectName", value = "对象名称", dataType = "String", required = true, example = "2021年开发年报.docx、/2021年开发年报.docx、/开发年报/2021年开发年报.docx、/开发年报、/开发年报/2021")
    String objectName;
}
