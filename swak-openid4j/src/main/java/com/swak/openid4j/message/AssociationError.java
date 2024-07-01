package com.swak.openid4j.message;

import com.swak.openid4j.association.AssociationSessionType;
import com.swak.openid4j.exception.OpenIDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class AssociationError extends DirectError {
    private static Logger _log = LoggerFactory.getLogger(AssociationError.class);

    public static final String ASSOC_ERR = "unsupported-type";

    protected final static List requiredFields = Arrays.asList(new String[]{
            "ns",
            "error_code",
            "error",
    });

    protected final static List optionalFields = Arrays.asList(new String[]{
            "assoc_type",
            "session_type"
    });

    protected AssociationError(String msg, AssociationSessionType type) {
        super(msg);

        set("ns", OPENID2_NS);
        set("error_code", ASSOC_ERR);
        set("session_type", type.getSessionType());
        set("assoc_type", type.getAssociationType());
    }

    protected AssociationError(ParameterList params) {
        super(params);
    }

    public static AssociationError createAssociationError(
            String msg, AssociationSessionType type) {
        AssociationError err = new AssociationError(msg, type);

        try {
            err.validate();
        } catch (MessageException e) {
            _log.error("Invalid association error message created, " +
                    "type: " + type + " message: " + msg, e);
        }

        return err;
    }

    public static AssociationError createAssociationError(ParameterList params) {
        AssociationError err = new AssociationError(params);

        try {
            err.validate();
        } catch (MessageException e) {
            _log.error("Invalid association error message created: "
                    + err.keyValueFormEncoding(), e);
        }

        return err;
    }

    public List getRequiredFields() {
        return requiredFields;
    }

    public void setAssociationSessionType(AssociationSessionType type) {
        set("session_type", type.getSessionType());
        set("assoc_type", type.getAssociationType());
    }

    public String getSessionType() {
        return getParameterValue("session_type");
    }

    public String getAssocType() {
        return getParameterValue("assoc_type");
    }

    private String getErrorCode() {
        return getParameterValue("error_code");
    }

    public void validate() throws MessageException {
        super.validate();

        if (!(ASSOC_ERR.equals(getErrorCode()) &&
                OPENID2_NS.equals(getParameterValue("ns"))))
            throw new MessageException("Invalid Association Error: " +
                    "invalid error_code or missing ns param.",
                    OpenIDException.ASSOC_ERROR);
    }

}
