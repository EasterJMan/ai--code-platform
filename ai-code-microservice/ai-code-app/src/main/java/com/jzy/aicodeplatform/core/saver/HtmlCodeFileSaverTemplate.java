package com.jzy.aicodeplatform.core.saver;

import cn.hutool.core.util.StrUtil;
import com.jzy.aicodeplatform.ai.model.HtmlCodeResult;
import com.jzy.aicodeplatform.exception.BusinessException;
import com.jzy.aicodeplatform.exception.ErrorCode;
import com.jzy.aicodeplatform.model.enums.CodeGenTypeEnum;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath,"index.html",result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML代码内容不能为空");
        }
    }
}
