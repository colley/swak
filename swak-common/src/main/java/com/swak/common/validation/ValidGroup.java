package com.swak.common.validation;

import javax.validation.groups.Default;

public interface ValidGroup extends Default {
    interface Insert extends ValidGroup {
    }

    interface Update extends ValidGroup {
    }

    interface Query extends ValidGroup {
    }

    interface Delete extends ValidGroup {
    }
}
