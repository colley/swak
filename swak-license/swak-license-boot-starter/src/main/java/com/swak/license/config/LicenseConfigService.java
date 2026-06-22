package com.swak.license.config;


public interface LicenseConfigService {

    String getConfig(String dataId);

    String getConfig(String dataId, String group);

}
