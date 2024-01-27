package com.bdilab.storage.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFolderVO {
    @NotNull
    @NotEmpty
    @ApiModelProperty(name = "bucketName", value = "存储桶名称", dataType = "String", required = true, example = "消防文档库")
    String bucketName;

    @NotNull
    @NotEmpty
    @ApiModelProperty(name = "objectName", value = "对象名称", dataType = "String", required = true, example = "支持格式: 年报、/年报、/年报/2021")
    String objectName;

    @NotNull
    @ApiModelProperty(name = "files", value = "文件对象-批量", dataType = "file", required = true)
    List<MultipartFile> files;

    @NotNull
    @ApiModelProperty(name = "isOverride", value = "是否覆盖, 默认为False", dataType = "Boolean", required = true, example = "False")
    Boolean isOverride = Boolean.FALSE;
}
