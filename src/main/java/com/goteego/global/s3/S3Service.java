package com.goteego.global.s3;

import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * ✅ 파일 업로드 (Public Read)
     */
    public String uploadFile(MultipartFile file, String type, Long id) {
        String key = buildS3Key(type, id, file.getOriginalFilename());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ) // ✅ Public Read
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    s3Properties.getBucketName(),
                    s3Properties.getRegion(),
                    key);

        } catch (IOException e) {
            log.error("❌ [S3] 파일 업로드 중 IO 오류 발생: {}", e.getMessage(), e);
            throw new S3Exception(ErrorCode.S3_IO_ERROR);
        } catch (S3Exception e) {
            throw e; // 이미 S3Exception으로 래핑된 경우 그대로 던짐
        } catch (Exception e) {
            log.error("❌ [S3] 파일 업로드 실패: {}", e.getMessage(), e);
            throw new S3Exception(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    /**
     * ✅ 단일 파일 삭제
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            log.info("✅ [S3] 파일 삭제 완료: {}", key);
        } catch (Exception e) {
            log.error("❌ [S3] 파일 삭제 실패: {}", key, e);
            throw new S3Exception(ErrorCode.S3_DELETE_FAILED);
        }
    }

    /**
     * ✅ 폴더(프리픽스) 삭제 (게시글 삭제 시)
     */
    public void deleteFolder(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Properties.getBucketName())
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            for (S3Object object : listResponse.contents()) {
                deleteFile(object.key());
            }
            log.info("✅ [S3] 폴더 삭제 완료: {}", prefix);
        } catch (Exception e) {
            log.error("❌ [S3] 폴더 삭제 실패: {}", prefix, e);
            throw new S3Exception(ErrorCode.S3_FOLDER_DELETE_FAILED);
        }
    }

    private String buildS3Key(String type, Long id, String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%d/%s%s", type, id, uuid, ext);
    }
}