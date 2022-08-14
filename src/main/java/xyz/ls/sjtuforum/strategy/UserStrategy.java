package xyz.ls.sjtuforum.strategy;

public interface UserStrategy {
    LoginUserInfo getUser(String code, String state);
    String getSupportedType();
}
