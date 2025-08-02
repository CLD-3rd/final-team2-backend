package com.goteego.global.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류, 관리자에게 문의하세요"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다"),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다"),
    BLANK_INPUT_VALUE(HttpStatus.BAD_REQUEST, "빈 값이 입력되었습니다"),

    // 지역 관련 (Location)
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "유효하지 않거나 지원되지 않는 지역입니다."),
    
    //Auth
    AUTH_NOT_FOUND(HttpStatus.UNAUTHORIZED, "시큐리티 인증 정보를 찾을 수 없습니다."),
    UNKNOWN_ERROR(HttpStatus.UNAUTHORIZED, "알 수 없는 에러"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Token입니다"),

    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 길이 및 형식이 다른 Token입니다"),
    WRONG_TYPE_TOKEN(HttpStatus.UNAUTHORIZED, "서명이 잘못된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "토큰이 없습니다"),
    TOKEN_SUBJECT_FORMAT_ERROR(HttpStatus.UNAUTHORIZED, "Subject 값에 Long 타입이 아닌 다른 타입이 들어있습니다."),
    AT_EXPIRED_AND_RT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AT는 만료되었고 RT는 비어있습니다."),
    RT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "RT가 비어있습니다"),
    INVALID_PRINCIPAL(HttpStatus.UNAUTHORIZED, "잘못된 Principal입니다"),
    INVALID_PROVIDER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 제공자 타입입니다"),

    // 유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    OAUTH_PROVIDER_MISMATCH(HttpStatus.BAD_REQUEST, "OAuth 제공자가 일치하지 않습니다."),

    // 채팅 관련
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방이 존재하지 않습니다."),
    INVALID_CHAT(HttpStatus.BAD_REQUEST, "자신과는 채팅할 수 없습니다."),

    // 게시글 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."),
    UNAUTHORIZED_POST_UPDATE(HttpStatus.FORBIDDEN, "게시글 수정 권한이 없습니다."),
    UNAUTHORIZED_POST_DELETE(HttpStatus.FORBIDDEN, "게시글 삭제 권한이 없습니다."),
    INVALID_POST_DATA(HttpStatus.BAD_REQUEST, "잘못된 게시글 데이터입니다."),
    UNSUPPORTED_POST_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 게시글 타입입니다."),
    CANNOT_DELETE_TRAVEL_POST_WITH_APPROVED_PARTICIPANTS(HttpStatus.BAD_REQUEST, "승인된 참여자가 있어 게시글을 삭제할 수 없습니다."),

    // 참여 요청 관련
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "참여 요청이 존재하지 않습니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 참가 신청한 게시글입니다."),
    RECRUITMENT_FULL(HttpStatus.CONFLICT, "모집 인원이 마감되었습니다."),
    SELF_APPLICATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 게시글에는 참가 신청할 수 없습니다."),


    // ProfileAnswer 관련
    PROFILE_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 선호도를 찾을 수 없습니다."),
    PROFILE_ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용자 선호도가 존재합니다."),
    INVALID_PROFILE_DATA(HttpStatus.BAD_REQUEST, "유효하지 않은 선호도 데이터입니다."),
    PROFILE_ANSWER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "선호도 생성에 실패했습니다."),
    PROFILE_ANSWER_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "선호도 수정에 실패했습니다."),
    PROFILE_ANSWER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "선호도 삭제에 실패했습니다."),
    EMBEDDING_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 임베딩 생성에 실패했습니다."),
    EMBEDDING_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 임베딩 업데이트에 실패했습니다."),
    UNAUTHORIZED_PROFILE_ACCESS(HttpStatus.FORBIDDEN, "해당 선호도에 접근할 권한이 없습니다."),
    INVALID_SEARCH_PARAMETERS(HttpStatus.BAD_REQUEST, "유효하지 않은 검색 파라미터입니다."),

    // 뱃지 관련
    NOT_FOUND_BADGE_CODE(HttpStatus.NOT_FOUND, "존재하지 않는 뱃지 코드입니다."),
    INVALID_BADGE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 뱃지 타입입니다."),
    NOT_FOUND_BADGE(HttpStatus.NOT_FOUND, "해당 뱃지를 찾을 수 없습니다."),
    NOT_FOUND_BADGE_REQUEST(HttpStatus.NOT_FOUND, "해당 피드의 뱃지 요청이 존재하지 않습니다."),

    // 피드 관련
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "피드가 존재하지 않습니다."),
    UNAUTHORIZED_FEED_UPDATE(HttpStatus.FORBIDDEN, "해당 피드에 대한 수정 권한이 없습니다."),
    UNAUTHORIZED_FEED_DELETE(HttpStatus.FORBIDDEN, "해당 피드에 대한 삭제 권한이 없습니다."),

    // 댓글 관련
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."),
    UNAUTHORIZED_COMMENT_UPDATE(HttpStatus.FORBIDDEN, "해당 댓글에 대한 수정 권한이 없습니다."),
    UNAUTHORIZED_COMMENT_DELETE(HttpStatus.FORBIDDEN, "해당 댓글에 대한 삭제 권한이 없습니다."),
    // 리뷰 관련
    SELF_REVIEW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신에게는 리뷰를 작성할 수 없습니다."),
    INVALID_REVIEW_TARGET(HttpStatus.BAD_REQUEST, "같은 여행에 참여하지 않은 대상자입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_DUPLICATE(HttpStatus.CONFLICT, "중복된 리뷰입니다."),
    TRAVEL_NOT_FINISH(HttpStatus.BAD_REQUEST, "여행이 종료되지 않았습니다."),

    // ✅ 파일 업로드 유효성 검증 관련 (업로드 전)
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기는 5MB를 초과할 수 없습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원되지 않는 파일 유형입니다. jpg 또는 png 파일만 허용됩니다."),

    // ✅ AWS S3 관련 (업로드/삭제/폴더 삭제 등)
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드 중 오류가 발생했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제 중 오류가 발생했습니다."),
    S3_FOLDER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 폴더 삭제 중 오류가 발생했습니다."),
    S3_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 스트림 처리 중 오류가 발생했습니다."),
    S3_INVALID_URL(HttpStatus.BAD_REQUEST, "잘못된 S3 URL입니다."),
    S3_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 작업 중 알 수 없는 오류가 발생했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
