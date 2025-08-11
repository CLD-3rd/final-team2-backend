//package com.goteego.badge.domain;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.S3Event;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.GetObjectRequest;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.*;
//
//public class ImageResizeLambda implements RequestHandler<S3Event, String> {
//
//    private final S3Client s3Client = S3Client.builder()
//            .region(Region.of("ap-northeast-2")) // 실제 S3 버킷 리전으로 변경
//            .build();
//
//    @Override
//    public String handleRequest(S3Event event, Context context) {
//
//        String bucket = null;
//        try {
//            // 이벤트 레코드 여러 개 처리
//            for (var record : event.getRecords()) {
//                bucket = record.getS3().getBucket().getName();
//
//                String keyRaw = record.getS3().getObject().getKey();
//                String key = java.net.URLDecoder.decode(keyRaw, "UTF-8");
//                context.getLogger().log("Received S3 key: " + key);
//
//                // badges/ 경로의 이벤트가 아닐경우 스킵
//                if (!key.startsWith("badges/")) {
//                    context.getLogger().log("Skipping non-badges key: " + key);
//                    continue;
//                }
//
//                // 원본 이미지 다운로드
//                InputStream originalImageStream = s3Client.getObject(
//                        GetObjectRequest.builder()
//                                .bucket(bucket)
//                                .key(key)
//                                .build());
//
//                BufferedImage originalImage = ImageIO.read(originalImageStream);
//                originalImageStream.close();
//
//                // 리사이즈 사이즈 정의
//                Dimension smallSize = new Dimension(100, 100);
//                Dimension mediumSize = new Dimension(300, 300);
//
//                // 리사이즈 및 업로드
//                resizeAndUpload(bucket, key, originalImage, smallSize, "thumb-small/");
//                resizeAndUpload(bucket, key, originalImage, mediumSize, "thumb-medium/");
//            }
//
//            return "Success resizing all images.";
//
//        } catch (Exception e) {
//            context.getLogger().log("Error: " + e.getMessage());
//            e.printStackTrace();
//            return "Error resizing images for bucket: " + bucket;
//        }
//    }
//
//    private void resizeAndUpload(String bucket, String originalKey, BufferedImage originalImage, Dimension targetSize, String prefix) throws IOException {
//        // 비율 유지하며 리사이즈
//        BufferedImage resizedImage = resizeImage(originalImage, targetSize.width, targetSize.height);
//
//        // 이미지 바이트로 변환
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(resizedImage, "png", os);
//        byte[] buffer = os.toByteArray();
//
//        // 새 파일명 생성 (예: badges/thumb-small/original.png)
//        String fileName = originalKey.substring(originalKey.lastIndexOf('/') + 1);
//        String newKey = "badges/" + prefix + fileName;
//
//        // S3 업로드
//        s3Client.putObject(
//                PutObjectRequest.builder()
//                        .bucket(bucket)
//                        .key(newKey)
//                        .contentType("image/png")
//                        .contentLength((long) buffer.length)
//                        .build(),
//                RequestBody.fromBytes(buffer)
//        );
//    }
//
//    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
//        // 원본 비율 유지
//        int originalWidth = originalImage.getWidth();
//        int originalHeight = originalImage.getHeight();
//
//        float widthRatio = (float) targetWidth / originalWidth;
//        float heightRatio = (float) targetHeight / originalHeight;
//        float ratio = Math.min(widthRatio, heightRatio);
//
//        int newWidth = Math.round(originalWidth * ratio);
//        int newHeight = Math.round(originalHeight * ratio);
//
//        Image tmp = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
//        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
//
//        Graphics2D g2d = resized.createGraphics();
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        g2d.drawImage(tmp, 0, 0, null);
//        g2d.dispose();
//
//        return resized;
//    }
//}