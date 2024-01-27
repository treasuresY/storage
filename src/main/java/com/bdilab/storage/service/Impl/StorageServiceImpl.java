package com.bdilab.storage.service.Impl;

import com.bdilab.storage.common.config.PathConfig;
import com.bdilab.storage.common.exception.InternalServerErrorException;
import com.bdilab.storage.common.utils.FileUtil;
import com.bdilab.storage.common.utils.HttpResponseUtil;
import com.bdilab.storage.dto.FileObjectInfoDTO;
import com.bdilab.storage.service.StorageService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {
    @Resource
    private PathConfig pathConfig;

    @Resource
    private RestTemplate restTemplate;

    private final ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    public StorageServiceImpl(@Qualifier("defaultThreadPoolExecutor") ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void createBucket(String bucketName) {
        // Word数据卷中创建桶
        String bucketPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName).toString();
        File bucketInWord = new File(bucketPathInWord);
        if (bucketInWord.exists()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶名称'%s'已被占用", bucketName)));
        }
        if (!bucketInWord.mkdirs()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶'%s'创建失败", bucketName)));
        }
        // Pdf数据卷中创建桶
        String bucketPathInPdf = Paths.get(pathConfig.getPdfDocumentPathBase(), bucketName).toString();
        File bucketInPdf = new File(bucketPathInPdf);
        if (bucketInPdf.exists()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶名称'%s'已被占用", bucketName)));
        }
        if (!bucketInPdf.mkdirs()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶'%s'创建失败", bucketName)));
        }
    }

    @Override
    public void removeBucket(String bucketName) {
        // 删除Word数据卷中的桶
        String bucketPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName).toString();
        File bucketInWord = new File(bucketPathInWord);
        if (!bucketInWord.exists()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("Word数据卷中不存在'%s'桶", bucketName)));
        }
        FileUtil.deleteFileOrDirObject(bucketInWord);
        // 删除Pdf数据卷中的桶
        String bucketPathInPdf = Paths.get(pathConfig.getPdfDocumentPathBase(), bucketName).toString();
        File bucketInPdf = new File(bucketPathInPdf);
        if (!bucketInPdf.exists()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("Pdf数据卷中不存在'%s'桶", bucketName)));
        }
        FileUtil.deleteFileOrDirObject(bucketInPdf);
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        if (!bucketExists(bucketName)) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("%s桶不存在", bucketName)));
        }
        // 删除word文件
        String objectPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName, objectName).toString();
        File objectInWord = new File(objectPathInWord);
        if (!objectInWord.exists()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("'%s'桶中不存在名称为'%s'的存储对象", bucketName, objectName)));
        }
        // 下面这行代码仅仅针对当前用例有效, 非此接口必须逻辑
        boolean isFile = objectInWord.isFile();
        FileUtil.deleteFileOrDirObject(objectInWord);
        // 删除pdf文件
        // 仅仅针对当前用例有效, 非此接口必须逻辑
        String objectPathInPdf;
        File objectInPdf;
        if (isFile) {
            String objectNameWithPdfExtension = String.join(".", objectName.substring(0, objectName.lastIndexOf(".")), "pdf");
            objectPathInPdf = Paths.get(pathConfig.getPdfDocumentPathBase(), bucketName, objectNameWithPdfExtension).toString();
            objectInPdf = new File(objectPathInPdf);
        } else {
            objectPathInPdf = Paths.get(pathConfig.getPdfDocumentPathBase(), bucketName, objectName).toString();
            objectInPdf = new File(objectPathInPdf);
        }
        if (!objectInPdf.exists()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("'%s'桶中不存在名称为'%s'的存储对象", bucketName, objectName)));
        }
        FileUtil.deleteFileOrDirObject(objectInPdf);
    }

    @Override
    public void putObject(String bucketName, String objectName, MultipartFile file, Boolean isOverride) throws InternalServerErrorException {
        if (!bucketExists(bucketName)) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("%s桶不存在", bucketName)));
        }
        // bucket path-pdf
        String bucketPathInPdf = Paths.get(pathConfig.getPdfDocumentPathBase(), bucketName).toString();
        File bucketInPdf = new File(bucketPathInPdf);
        if (!bucketInPdf.exists()) {
            if (!bucketInPdf.mkdirs()) {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("创建桶%s失败", bucketName)));
            }
        }
        // bucket path-word
        String bucketPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName).toString();
        File bucketInWord = new File(bucketPathInWord);
        if (!bucketInWord.exists()) {
            if (!bucketInWord.mkdirs()) {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("创建桶%s失败", bucketName)));
            }
        }
        // 1.存储doc、docx文件
        String objectPathInWord = Paths.get(bucketPathInWord, objectName).toString();
        File objectInWord = new File(objectPathInWord);
        // 检验文件是否存在？是否需要替换？
        if (objectInWord.exists()) {
            if (isOverride) {
                if (!objectInWord.delete()) {
                    throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶'%s'中'%s'存储对象删除失败", bucketName, objectName)));
                }
            } else {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶%s下存在同名存储对象%s, 请设置覆盖为True", bucketName, objectName)));
            }
        } else {
            try {
                FileUtil.createNewFile(objectInWord);
            } catch (Exception e) {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶'%s'中创建'%s'存储对象失败", bucketName, objectName)));
            }
        }
        try {
            file.transferTo(objectInWord);
        } catch (IOException ex) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("文件存储失败"));
        }
        // doc、docx转换为pdf，并存储pdf文件
        String objectNameWithPdfExtension = String.join(".", objectName.substring(0, objectName.lastIndexOf(".")), "pdf");
        String objectPathInPdf = Paths.get(bucketPathInPdf, objectNameWithPdfExtension).toString();
        File objectInPdf = new File(objectPathInPdf);
        try {
            FileUtil.convertWordToPdf(objectInWord, objectInPdf);
        } catch (Exception e) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(e.toString()));
        }
        // Call document embedding interface
        threadPoolExecutor.execute(() -> {
            // 读取文件为字节数组
            byte[] wordFileBytes;
            byte[] pdfFileBytes;
            try {
                wordFileBytes = Files.readAllBytes(Paths.get(objectPathInWord));
                pdfFileBytes = Files.readAllBytes(Paths.get(objectPathInPdf));
            } catch (IOException e) {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("文件读取失败"));
            }
            // 设置请求体参数
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file_category", "");
            body.add("file_name", objectName.substring(0, objectName.lastIndexOf(".")));
            body.add("is_overlap", isOverride);
            body.add("word_file", new ByteArrayResource(wordFileBytes) {
                @Override
                public String getFilename() {
                    return objectName;
                }
            });
            body.add("pdf_file", new ByteArrayResource(pdfFileBytes) {
                @Override
                public String getFilename() {
                    return objectNameWithPdfExtension;
                }
            });

            // 设置请求头部信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送 POST 请求
            ResponseEntity<String> response = restTemplate.exchange(pathConfig.getAddReportEndpoint(), HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("调用报告问答接口失败");
            } else {
                log.info("调用报告问答接口成功");
                log.info(String.format("响应体内容: %s", response.getBody()));
            }
        });
    }

    @Override
    public void putObjects(String bucketName, String objectName, List<MultipartFile> files, Boolean isOverride) {
        String objectPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName, objectName).toString();
        File objectInWord = new File(objectPathInWord);
        // 检验文件夹是否存在？是否需要替换？
        if (objectInWord.isDirectory() && objectInWord.exists()) {
            if (isOverride) {
                FileUtil.deleteFileOrDirObject(objectInWord);
            } else {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("'%s'桶下存在同名对象'%s', 请设置覆盖为True", bucketName, objectName)));
            }
        }
        for (MultipartFile file : files) {
            String fileObjectName = Paths.get(objectName, file.getOriginalFilename()).toString();
            putObject(bucketName, fileObjectName, file, true);
        }
    }

    @Override
    public List<FileObjectInfoDTO> listObjects(String bucketName) {
        String bucketPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName).toString();
        File bucketInWord = new File(bucketPathInWord);
        List<FileObjectInfoDTO> fileObjectInfoDTOList = new ArrayList<>();
        // 获取目录下所有文件名称
        List<String> fileObjectNameList;
        try {
            fileObjectNameList = FileUtil.listFileObjectNameInDirectory(bucketInWord);
        } catch (Exception e) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(e.toString()));
        }
        for (String fileObjectName : fileObjectNameList) {
            String fileNameWithoutExtension = FileUtil.removeFileNameExtension(fileObjectName);
            String objectPathInWord = Paths.get(bucketPathInWord, fileObjectName).toString();
            // 获取文件的属性对象
            BasicFileAttributes fileAttributes = FileUtil.getBasicFileAttributes(objectPathInWord);
            LocalDateTime uploadTime = LocalDateTime.ofInstant(fileAttributes.creationTime().toInstant(), ZoneId.systemDefault());
            long size = fileAttributes.size();
            FileObjectInfoDTO fileObjectInfoDTO = new FileObjectInfoDTO(fileObjectName, size, uploadTime);
            fileObjectInfoDTOList.add(fileObjectInfoDTO);
        }
        return fileObjectInfoDTOList;
    }

    @Override
    public byte[] getFileObjectBytes(File fileObject) {
        if (!fileObject.exists() && !fileObject.isFile()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("文件不存在"));
        }
        return FileUtil.getFileObjectBytes(fileObject);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        String bucketPathInWord = Paths.get(pathConfig.getWordDocumentPathBase(), bucketName).toString();
        File bucketInWord = new File(bucketPathInWord);
        return bucketInWord.exists();
    }
}
