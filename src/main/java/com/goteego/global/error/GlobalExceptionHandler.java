package com.goteego.global.error;

import com.goteego.global.error.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ✅ 전역 예외 처리 핸들러
 * - 모든 Controller에서 발생하는 예외를 처리
 * - 커스텀 예외는 공통 응답 포맷(ErrorResponse)으로 변환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ⚠️ 비즈니스 로직 관련 공통 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(final BusinessException exception, HttpServletRequest request) {
        log.warn("⚠️ [BusinessException] {} - {}", exception.getErrorCode(), exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * ⛔ 권한 없음 (AccessDenied) 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        log.warn("⛔ [AccessDeniedException] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * 🔍❌ 리소스를 찾을 수 없음 예외 처리
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
        log.warn("🔍❌ [NotFoundException] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }


    /**
     * 📍❌ 잘못된 지역(Location) 요청 예외 처리
     */
    @ExceptionHandler(InvalidLocationException.class)
    public ResponseEntity<Object> handleInvalidLocationException(InvalidLocationException exception, HttpServletRequest request) {
        log.warn("\uD83D\uDCCD❌ [InvalidLocationException] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * 📂❌ 잘못된 파일 업로드 예외 처리
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Object> handleInvalidFileException(InvalidFileException exception, HttpServletRequest request) {
        log.warn("📂❌ [InvalidFileException] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * 🏖❌ 잘못된 여행 태그(TravelTag) 요청 예외 처리
     */
    @ExceptionHandler(InvalidTravelTagException.class)
    public ResponseEntity<Object> handleInvalidTravelTagException(InvalidTravelTagException exception, HttpServletRequest request) {
        log.warn("🏖❌ [InvalidTravelTagException] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * 🔐 인증 실패(Unauthorized) 예외 처리
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccessException(UnauthorizedAccessException exception, HttpServletRequest request) {
        log.warn("🔐 [UnauthorizedAccessException] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * 📦 S3 업로드/삭제 실패 예외 처리
     */
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<Object> handleS3Exception(S3Exception exception, HttpServletRequest request) {
        log.error("📦 [S3Exception] {}", exception.getMessage());
        return buildErrorResponse(exception, request);
    }

    /**
     * 🛠️ 요청 값 검증 실패 (Validation Exception)
     * - @Valid, @Validated 검증 실패 시 발생
     * - 필드별 상세 에러 정보 반환
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Object> handleValidationException(Exception exception, HttpServletRequest request) {
        log.warn("🛠️ [ValidationException] 요청 값 검증 실패: {}", exception.getMessage());
        String path = request.getRequestURI();

        BindingResult bindingResult = (exception instanceof MethodArgumentNotValidException ex)
                ? ex.getBindingResult()
                : ((BindException) exception).getBindingResult();

        ErrorResponse response = ErrorResponse.fromValidationErrors(bindingResult, path);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 🧨 예상치 못한 서버 예외 처리
     * - 디버깅을 위해 전체 스택 로그 기록
     * - 클라이언트에는 내부 서버 오류(500)로 응답
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandledException(Exception exception, HttpServletRequest request) {
        log.error("🧨 [Unhandled Exception]", exception);
        String path = request.getRequestURI();

        ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR, path);
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * ✅ 공통 응답 빌더
     * - 커스텀 비즈니스 예외를 ErrorResponse로 변환
     */
    private ResponseEntity<Object> buildErrorResponse(BusinessException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        String path = request.getRequestURI();
        ErrorResponse response = ErrorResponse.from(errorCode, path);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}