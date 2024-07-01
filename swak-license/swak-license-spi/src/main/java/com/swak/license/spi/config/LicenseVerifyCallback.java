package com.swak.license.spi.config;

import com.swak.license.api.License;

public interface LicenseVerifyCallback {

    void call(License license);

    default  void clear() {}
}
