package xyz.ls.sjtuforum.exception;

public enum SFErrorCode implements MyErrorCode {

    POST_NOT_FOUND(2001, "话题不存在"),
    TARGET_PARAM_NOT_FOUND(2002, "未选中帖子或评论"),
    NO_LOGIN(2003, "用户未登录"),
    SYS_ERROR(2004, "服务器错误"),
    TYPE_PARAM_WRONG(2005, "评论类型不存在"),
    COMMENT_NOT_FOUND(2006, "评论不存在"),
    CONTENT_IS_EMPTY(2007, "内容不能为空"),
    READ_NOTIFICATION_FAIL(2008, "用户身份错误"),
    NOTIFICATION_NOT_FOUND(2009, "通知不存在"),
    FILE_UPLOAD_FAIL(2010, "图片上传失败"),
    INVALID_INPUT(2011, "非法输入"),
    INVALID_OPERATION(2012, "非法操作"),
    USER_DISABLE(2013, "用户禁用"),
    RATE_LIMIT(2014, "操作太快，请稍后重试"),
    ;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    private Integer code;
    private String message;

    SFErrorCode(Integer code, String message) {
        this.message = message;
        this.code = code;
    }
}
