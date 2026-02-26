package com.community.controller.common;

import com.community.common.BizException;
import com.community.common.Result;
import com.community.common.ResultCode;
import com.community.vo.UploadFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/common/upload")
@Tag(name = "Common Upload")
public class FileUploadController {

    private static final String BIZ_AVATAR = "avatar";
    private static final String BIZ_EXPERT_PROOF = "expert-proof";
    private static final String BIZ_QUESTION = "question";
    private static final String BIZ_ANSWER = "answer";

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final Set<String> EXPERT_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "pdf", "doc", "docx", "txt", "md"
    );

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "bizType: avatar/expert-proof/question/answer")
    public Result<UploadFileVO> upload(@RequestPart("file") MultipartFile file,
                                       @RequestParam("bizType") String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "file is required");
        }
        if (!StringUtils.hasText(bizType)) {
            throw new BizException(ResultCode.BAD_REQUEST, "bizType is required");
        }

        String normalizedBizType = bizType.trim().toLowerCase(Locale.ROOT);
        if (!BIZ_AVATAR.equals(normalizedBizType)
            && !BIZ_EXPERT_PROOF.equals(normalizedBizType)
            && !BIZ_QUESTION.equals(normalizedBizType)
            && !BIZ_ANSWER.equals(normalizedBizType)) {
            throw new BizException(ResultCode.BAD_REQUEST, "unsupported bizType");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        validateExtension(normalizedBizType, extension);

        String generatedName = UUID.randomUUID().toString().replace("-", "");
        String filename = extension.isEmpty() ? generatedName : generatedName + "." + extension;

        Path baseDir = Paths.get(uploadDir, normalizedBizType).toAbsolutePath().normalize();
        Path target = baseDir.resolve(filename);
        try {
            Files.createDirectories(baseDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException(ResultCode.SERVER_ERROR, "upload failed: " + e.getMessage());
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }

        String url = "/api/common/upload/" + normalizedBizType + "/" + filename;
        return Result.success(new UploadFileVO(
            url,
            filename,
            originalFilename,
            contentType,
            file.getSize(),
            normalizedBizType
        ));
    }

    @GetMapping("/{bizType}/{filename}")
    @Operation(summary = "Get uploaded file")
    public ResponseEntity<Resource> get(@PathVariable String bizType, @PathVariable String filename) {
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(filename)) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedBizType = bizType.trim().toLowerCase(Locale.ROOT);
        if (!BIZ_AVATAR.equals(normalizedBizType)
            && !BIZ_EXPERT_PROOF.equals(normalizedBizType)
            && !BIZ_QUESTION.equals(normalizedBizType)
            && !BIZ_ANSWER.equals(normalizedBizType)) {
            return ResponseEntity.badRequest().build();
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = Paths.get(uploadDir, normalizedBizType).toAbsolutePath().normalize().resolve(filename).normalize();
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = "application/octet-stream";
        try {
            String detected = Files.probeContentType(filePath);
            if (StringUtils.hasText(detected)) {
                contentType = detected;
            }
        } catch (IOException ignored) {
            // fallback
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }

    private void validateExtension(String bizType, String extension) {
        if (BIZ_AVATAR.equals(bizType) && !IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST, "avatar only supports image files");
        }
        if (BIZ_QUESTION.equals(bizType) && !IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST, "question image only supports jpg/jpeg/png/gif/webp/bmp");
        }
        if (BIZ_ANSWER.equals(bizType) && !IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST, "answer image only supports jpg/jpeg/png/gif/webp/bmp");
        }
        if (BIZ_EXPERT_PROOF.equals(bizType) && !EXPERT_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST, "expert-proof only supports image/doc/pdf/txt");
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}