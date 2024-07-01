package com.swak.common.validation;

import com.swak.common.enums.IResultCode;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
public class MessagesFormat {

    private IResultCode resultCode;

    private String message;
    private Object[] arguments;

    public String toMessage() {
        if (StringUtils.isEmpty(message)) {
            return message;
        }
        if (message.indexOf("%s") > -1) {
            return String.format(message, arguments);
        }
        return java.text.MessageFormat.format(message, arguments);
    }
}
