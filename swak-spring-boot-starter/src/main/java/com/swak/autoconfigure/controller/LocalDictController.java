package com.swak.autoconfigure.controller;

import com.google.common.collect.Sets;
import com.swak.autoconfigure.service.LocalDictionaryService;
import com.swak.common.dto.Response;
import com.swak.common.dto.SelectDataVo;
import com.swak.common.util.GetterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@RestController
@Validated
@RequestMapping("dictionary")
public class LocalDictController {

    @Autowired(required = false)
    private LocalDictionaryService localDictionaryService;

    /**
     * 获取本地枚举编码
     */
    @GetMapping("/localDict")
    public Response<List<SelectDataVo>> getLocalDict(@RequestParam(name = "category") @NotEmpty String category,
                                                     @RequestParam(name = "relation", required = false) String relation) {
        List<SelectDataVo> data = localDictionaryService.getLocalDictCode(category, relation);
        return Response.success(data);
    }

    /**
     * 批量获取获取本地枚举编码
     */
    @PostMapping("/localDict/batch")
    public Response<Map<String, Collection<SelectDataVo>>> getBatchLocalDict(@RequestParam(name = "category") @NotEmpty String category) {
        Set<String> categorySet = Sets.newHashSet(GetterUtil.getSplit2List(category));
        Map<String, Collection<SelectDataVo>> bathDictCode = localDictionaryService.getBathDictCode(categorySet);
        return Response.success(bathDictCode);
    }
}
