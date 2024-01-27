package com.bdilab.storage.controller;

import com.bdilab.storage.common.config.PathConfig;
import com.bdilab.storage.common.exception.BadRequestException;
import com.bdilab.storage.common.exception.InternalServerErrorException;
import com.bdilab.storage.common.response.HttpResponse;
import com.bdilab.storage.common.utils.HttpResponseUtil;
import com.bdilab.storage.dto.FileObjectInfoDTO;
import com.bdilab.storage.service.Impl.StorageServiceImpl;
import com.bdilab.storage.vo.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {
    @Resource
    private StorageServiceImpl storageService;

    @Resource
    private PathConfig pathConfig;


    @PostMapping("/bucket/create")
    @ApiOperation(value = "创建存储桶")
    public HttpResponse createBucket(@Valid @RequestBody CreateBucketVO createBucketVO) {
        storageService.createBucket(createBucketVO.getBucketName());
        return new HttpResponse(HttpResponseUtil.generateSuccessResponseData(String.format("'%s'桶创建成功", createBucketVO.getBucketName())));
    }

    @DeleteMapping("/bucket/delete")
    @ApiOperation(value = "删除存储桶")
    public HttpResponse deleteBucket(@Valid @RequestBody DeleteBucketVO deleteBucketVO) {
        storageService.removeBucket(deleteBucketVO.getBucketName());
        return new HttpResponse(HttpResponseUtil.generateSuccessResponseData(String.format("'%s'桶删除成功", deleteBucketVO.getBucketName())));
    }

    @PostMapping("/file/upload")
    @ApiOperation(value = "上传文件", notes = "上传'单个‘文件")
    public HttpResponse uploadFile(
            @Valid UploadFileVO uploadFileVO) {

        if (uploadFileVO.getObjectName().startsWith("/")) {
            uploadFileVO.setObjectName(uploadFileVO.getObjectName().replaceFirst("/", ""));
        }
        storageService.putObject(uploadFileVO.getBucketName(), uploadFileVO.getObjectName(), uploadFileVO.getFile(), uploadFileVO.getIsOverride());
        return new HttpResponse(HttpResponseUtil.generateSuccessResponseData("文件上传成功"));
    }

    @DeleteMapping("/file-or-folder/delete")
    @ApiOperation(value = "删除文件或目录", notes = "若删除目录, 将删除目录下的所有文件对象")
    public HttpResponse deleteFolder(@Valid @RequestBody DeleteFileOrFolderVO deleteFileOrFolderVO) {
        String bucketName = deleteFileOrFolderVO.getBucketName();
        String objectName = deleteFileOrFolderVO.getObjectName();
        // 删除前缀符'/'
        if (objectName.startsWith("/")) {
            objectName = objectName.replaceFirst("/", "");
        }
        storageService.removeObject(bucketName, objectName);
        return new HttpResponse(HttpResponseUtil.generateSuccessResponseData("文件或目录删除成功"));
    }

    @PostMapping("/folder/upload")
    @ApiOperation(value = "上传文件夹")
    public HttpResponse uploadFiles(@Valid UploadFolderVO uploadFolderVO) {
        String bucketName = uploadFolderVO.getBucketName();
        String objectName = uploadFolderVO.getObjectName();
        // 删除前缀符'/'
        if (objectName.startsWith("/")) {
            objectName = objectName.replaceFirst("/", "");
        }
        storageService.putObjects(bucketName, objectName, uploadFolderVO.getFiles(), uploadFolderVO.getIsOverride());
        return new HttpResponse(HttpResponseUtil.generateSuccessResponseData("文件上传成功"));
    }

    @GetMapping("/object-info/list")
    @ApiOperation(value = "获取指定桶下的的所有文件对象信息")
    public HttpResponse listFilesInFolder(@RequestParam String bucketName) {
        List<FileObjectInfoDTO> fileObjectInfoDTOS = storageService.listObjects(bucketName);
        return new HttpResponse(new HashMap<String, Object>(){
            {
                put("fileObjectInfoList", fileObjectInfoDTOS);
            }
        });
    }

    @PostMapping("/file/download-or-preview")
    @ApiOperation(value = "预览 or 下载文件")
    public ResponseEntity downloadFile(@Valid @RequestBody DownloadOrPreviewFileVO downloadOrPreviewFileVO) {
        String bukcetName = downloadOrPreviewFileVO.getBucketName();
        String objectName = downloadOrPreviewFileVO.getObjectName();
        // 删除前缀符'/'
        if (objectName.startsWith("/")) {
            objectName = objectName.replaceFirst("/", "");
        }
        // 获取文件对象名称后缀
        String objectNameSuffix = objectName.substring(objectName.lastIndexOf(".") + 1);
        // 获取文件二进制数据并依据文件类型，设置Header-Content-Type
        HttpHeaders headers = new HttpHeaders();
        byte[] bytes;
        if (objectNameSuffix.equalsIgnoreCase("pdf")) {
            String objectPathInPdf = Paths.get(pathConfig.getPdfDocumentPathBase(), bukcetName, objectName).toString();
            File objectInPdf = new File(objectPathInPdf);
            // 获取文件二进制数据
            bytes = storageService.getFileObjectBytes(objectInPdf);
            // 设置Header-Content-Type
            headers.add("Content-Type", "application/pdf");
        } else if (objectNameSuffix.equalsIgnoreCase("doc") || objectNameSuffix.equalsIgnoreCase("docx")) {
            String objectPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bukcetName, objectName).toString();
            File objectInWord = new File(objectPathInWord);
            // 获取文件二进制数据
            bytes = storageService.getFileObjectBytes(objectInWord);
            // 设置Header-Content-Type
            if (objectNameSuffix.equalsIgnoreCase("doc")) {
                headers.add("Content-Type", "application/msword");
            } else if (objectNameSuffix.equalsIgnoreCase("docx")) {
                headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }
        } else {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("不支持扩展名为'%s'的文件对象, 仅支持pdf、doc、docx", objectNameSuffix)));
        }
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

}
