package com.swak.common.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author colley.ma
 * @since 2022/9/9 18:36
 */
public class ValidationResult implements java.io.Serializable {

    private static final long serialVersionUID = 2048421877163584998L;
    /**
     * 校验是否成功
     */
    private boolean success;

    //校验没通过的行数

    private int rowIndex;
    /**
     * 错误消息
     */
    private List<ErrorMessage> errorMessages = new ArrayList<>();


    /**
     * 构造
     *
     * @param success 是否验证成功
     */
    public ValidationResult(boolean success) {
        this.success = success;
    }

    public static ValidationResult success() {
        return new ValidationResult(true);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * 是否验证通过
     *
     * @return 是否验证通过
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置是否通过
     *
     * @param success 是否通过
     * @return this
     */
    public ValidationResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * 获取错误信息列表
     *
     * @return 错误信息列表
     */
    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    /**
     * 设置错误信息列表
     *
     * @param errorMessages 错误信息列表
     * @return this
     */
    public ValidationResult setErrorMessages(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
        return this;
    }

    /**
     * 增加错误信息
     *
     * @param errorMessage 错误信息
     * @return this
     */
    public ValidationResult addErrorMessage(ErrorMessage errorMessage) {
        this.errorMessages.add(errorMessage);
        return this;
    }

    /**
     * 错误消息，包括字段名（字段路径）、消息内容和字段值
     */
    @SuppressWarnings("serial")
    public static class ErrorMessage implements java.io.Serializable {

        /**
         * 属性字段名称
         */
        private String propertyName;
        /**
         * 错误信息
         */
        private String message;
        /**
         * 错误值
         */
        private Object value;

        public static ErrorMessage neErrMessage(String propertyName, String message) {
            ErrorMessage err = new ErrorMessage();
            err.setMessage(message);
            err.setPropertyName(propertyName);
            return err;
        }

        public static ErrorMessage neErrMessage(String message) {
            ErrorMessage err = new ErrorMessage();
            err.setMessage(message);
            return err;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "ErrorMessage{" + "propertyName='" + propertyName + '\'' + ", message='" + message + '\''
                    + ", value=" + value + '}';
        }
    }
}
