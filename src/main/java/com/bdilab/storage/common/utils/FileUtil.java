package com.bdilab.storage.common.utils;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.bdilab.storage.common.exception.InternalServerErrorException;
import com.bdilab.storage.common.exception.ResourceNotFoundException;

import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FileUtil {
    @ApiOperation("根据文件名前缀, 匹配路径下的文件, 并返回带扩展名的文件名, 只返回第一个匹配成功的文件名")
    public static String matchFilesByPrefix(File directory, String fileNameWithoutExtension) {
        String fileNameWithExtension = "";

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String filePrefix = fileName.substring(0, fileName.lastIndexOf("."));
                        if (filePrefix.equals(fileNameWithoutExtension)) {
                            fileNameWithExtension = fileName;
                            return fileNameWithExtension;
                        }
                    }
                }
                if (StringUtils.isEmpty(fileNameWithExtension)) {
                    // TODO pdf文档和word文档信息不匹配, 临时解决方案。
                    return fileNameWithoutExtension + ".pdf";
//                    throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("没有找到匹配文件"));
                }
                return fileNameWithExtension;
            } else {
                throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("目录为空或不可读"));
            }
        } else {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("指定的目录不存在或不是一个目录"));
        }
    }

    @ApiOperation("创建文件夹")
    public static void createFolder(String folderPath) {
        File newFolder = new File(folderPath);
        if (newFolder.exists() && newFolder.isDirectory()) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("文件夹已存在"));
        }
        if (newFolder.mkdir()) {
            return;
        } else {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("文件夹创建失败"));
        }
    }

    @ApiOperation("删除文件名中的扩展名")
    public static String removeFileNameExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex != -1) {
            return fileName.substring(0, lastIndex);
        } else {
            return fileName;
        }
    }

//    @ApiOperation("获取指定目录下的所有文件对象名称")
//    public static List<String> listFileObjectNameInDirectory(File dirObject) throws Exception {
//        List<String> fileNameList = new ArrayList<>();
//        // 检查目录是否存在
//        if (dirObject.exists() && dirObject.isDirectory()) {
//            // 获取文件夹中所有文件名称
//            File[] files = dirObject.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isFile()) {
//                        fileNameList.add(file.getName());
//                    }
//                }
//            }
//        } else {
//            throw new Exception(String.format("目录%s不存在", dirObject.getPath()));
//        }
//        return fileNameList;
//    }
    @ApiOperation("获取指定目录下的所有文件对象名称")
    public static List<String> listFileObjectNameInDirectory(File dirObject) throws Exception {
        List<String> fileObjectNameList = new ArrayList<>();
        // 检查目录是否存在
        if (dirObject.exists() && dirObject.isDirectory()) {
            try {
                Files.walkFileTree(dirObject.toPath(), EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                        System.out.println("File: " + file); // 输出文件路径
                        fileObjectNameList.add(file.subpath(dirObject.toPath().getNameCount(), file.getNameCount()).toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                        System.out.println("Directory: " + dir); // 输出目录路径
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        System.err.println("Failed to visit file: " + file); // 输出访问文件失败的信息
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new Exception(e.toString());
            }
        } else {
            throw new Exception(String.format("目录%s不存在", dirObject.getPath()));
        }
        return fileObjectNameList;
    }
    @ApiOperation("删除目录或文件对象")
    public static void deleteFileOrDirObject(File fileOrDirectoryObject) {
        if (fileOrDirectoryObject.isDirectory()) {
            // 获取目录中的所有文件和子目录
            File[] files = fileOrDirectoryObject.listFiles();
            if (files != null) {
                for (File child : files) {
                    // 递归删除子文件或子目录
                    deleteFileOrDirObject(child);
                }
            }
        }
        fileOrDirectoryObject.delete();
    }

    @ApiOperation("获取指定文件路径的'基本属性对象'")
    public static BasicFileAttributes getBasicFileAttributes(String filePath) {
        Path path = Paths.get(filePath);
        BasicFileAttributes fileAttributes;
        try {
            fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("获取文件属性对象失败"));
        }
        return fileAttributes;
    }

//    @ApiOperation("将指定路径下的Doc文件转换为Pdf文件，并将转换后的Pdf文件存储至指定指定路径下")
//    public static void convertDocToPdf(String DocFilePath, String pdfFilePath) {
//        try {
//            InputStream inputStream = Files.newInputStream(new File(DocFilePath).toPath());
//            Document doc = new Document(inputStream);
//            //新建一个空白pdf文档
//            File pdfFileObject = new File(pdfFilePath);
//            if (pdfFileObject.exists()) {
//                if (!pdfFileObject.delete()) {
//                    throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("文件'%s'删除失败", pdfFilePath)));
//                }
//            } else {
//                if (!pdfFileObject.mkdirs()) {
//                    throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData(String.format("桶'%s'中创建'%s'存储对象失败", bucketName, objectName)));
//                }
//            }
//            FileOutputStream os = new FileOutputStream(file);
//            //全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
//            doc.save(os, SaveFormat.PDF);
//            os.close();
//        } catch (Exception e) {
//            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("Doc或Docx转Pdf失败"));
//        }
//    }
    @ApiOperation("将Doc文件转换为Pdf文件，并存储转换后的Pdf文件")
    public static void convertWordToPdf(File wordFileObject, File pdfFileObject) throws Exception{
        if (!wordFileObject.exists()) {
            createNewFile(wordFileObject);
        }
        if (!pdfFileObject.exists()) {
            createNewFile(pdfFileObject);
        } else {
            if (!pdfFileObject.delete()) {
                throw new Exception("Word转Pdf失败, 具体原因: Pdf文件已存在且无法删除");
            }
            createNewFile(pdfFileObject);
        }
        if (!wordFileObject.isFile() || !pdfFileObject.isFile()) {
            throw new Exception("Word转Pdf失败, 具体原因: 文件对象类型不匹配");
        }
        InputStream inputStream = Files.newInputStream(wordFileObject.toPath());
        Document doc = new Document(inputStream);
        FileOutputStream os = new FileOutputStream(pdfFileObject);
        //全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
        doc.save(os, SaveFormat.PDF);
        os.close();
    }
    @ApiOperation("获取文件对象的二进制数据")
    public static byte[] getFileObjectBytes(File fileObject) {
        // 读取PDF文件内容
        byte[] fileObjectBytes;
        try {
            fileObjectBytes = Files.readAllBytes(fileObject.toPath());
        } catch (IOException e) {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("获取文件对象字节数失败"));
        }
        return fileObjectBytes;
    }
    public static void createNewFile(File fileObject) throws IOException {
        File parentDir = fileObject.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        fileObject.createNewFile();
    }

    @ApiOperation("获取指定路径下的所有文件夹的名称")
    public static List<String> getFolderName(String path) {
        ArrayList<String> folderNames = new ArrayList<>();
        // 创建一个File对象，表示指定的路径
        File directory = new File(path);

        // 检查路径是否存在
        if (directory.exists() && directory.isDirectory()) {
            // 获取路径下的所有文件夹
            File[] folders = directory.listFiles(File::isDirectory);

            if (folders != null) {
                for (File folder : folders) {
                    folderNames.add(folder.getName());
                }
            }
            return folderNames;
        } else {
            throw new InternalServerErrorException(HttpResponseUtil.generateExceptionResponseData("指定的路径不存在或不是一个文件夹"));
        }
    }
}
