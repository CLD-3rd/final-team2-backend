package com.goteego.global.s3;

import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.InvalidFileException;
import com.goteego.global.error.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * ✅ URL에서 S3 Key 추출<br>
     * 예: https://bucket.s3.region.amazonaws.com/feeds/123/uuid.jpg → feeds/123/uuid.jpg
     *
     * @param imageUrl
     * @return
     */
    /**
     * ✅ URL에서 S3 Key 추출 (예: feeds/123/uuid.jpg)
     */
    public static String extractKeyFromUrl(String imageUrl, String directoryType) {
        if (imageUrl == null || !imageUrl.contains(directoryType)) {
            throw new S3Exception(ErrorCode.S3_INVALID_URL);
        }
        return imageUrl.substring(imageUrl.indexOf(directoryType));
    }

    /**
     * ✅ 파일 업로드<br>
     * (유효성 검증 → S3 업로드 → URL 반환)
     */
    public String uploadFile(MultipartFile imageFile, String type, Long userId) {
        if (imageFile == null || imageFile.isEmpty()) return null;

        validateFile(imageFile);
        String key = buildS3Key(type, userId, Objects.requireNonNull(imageFile.getOriginalFilename()));

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(imageFile.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));

            return generateFileUrl(key);
        } catch (IOException e) {
            log.error("❌ [S3] 파일 스트림 처리 오류", e);
            throw new S3Exception(ErrorCode.S3_IO_ERROR);
        } catch (S3Exception e) { // AWS SDK에서 제공하는 S3 관련 예외
            log.error("❌ [S3] 업로드 실패 (AWS SDK 예외)", e);
            throw new S3Exception(ErrorCode.S3_UPLOAD_FAILED);
        } catch (Exception e) { // 기타 알 수 없는 예외
            log.error("❌ [S3] 알 수 없는 업로드 오류", e);
            throw new S3Exception(ErrorCode.S3_UNKNOWN_ERROR);
        }
    }

    /**
     * 파일 업로드 InputStream
     */
    public String uploadFile(InputStream inputStream, long contentLength, String contentType, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            return generateFileUrl(key);
        } catch (S3Exception e) {
            log.error("❌ [S3] 업로드 실패", e);
            throw new S3Exception(ErrorCode.S3_UPLOAD_FAILED);
        } catch (Exception e) {
            log.error("❌ [S3] 알 수 없는 오류", e);
            throw new S3Exception(ErrorCode.S3_UNKNOWN_ERROR);
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
     * ✅ 폴더(프리픽스) 삭제 (회원 탈퇴 시)
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

    /**
     * ✅ 파일 유효성 검증
     */
    private void validateFile(MultipartFile imageFile) {
        if (imageFile.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String fileName = imageFile.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new InvalidFileException(ErrorCode.INVALID_FILE_FORMAT);
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    /**
     * ✅ S3 파일 URL 생성
     */
    private String generateFileUrl(String key) {
        return String.format("%s/%s", s3Properties.getCdnDomain(), key);
    }

    /**
     * ✅ S3 Key 생성 (예: feeds/123/uuid.jpg)
     */
    private String buildS3Key(String type, Long userId, String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%d/%s%s", type, userId, uuid, ext);
    }
}