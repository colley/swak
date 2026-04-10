package com.swak.autoconfigure.handle;

import com.swak.common.dto.SelectDataVo;
import com.swak.common.spi.SpiPriority;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author colley.ma
 * @since 2.4.0
 */
public interface DictionaryHandler extends SpiPriority {
    List<SelectDataVo> onSorted(List<SelectDataVo> enumTypeDict,String categoryType);

    List<SelectDataVo> onFilter(List<SelectDataVo> enumTypeDict,String categoryType);

    default Set<String> supportType() {
        return Collections.emptySet();
    }

}
