package com.jzy.aicodeplatform.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jzy.aicodeplatform.constant.AppConstant;
import com.jzy.aicodeplatform.exception.BusinessException;
import com.jzy.aicodeplatform.exception.ErrorCode;
import com.jzy.aicodeplatform.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;


    public final File save(T result, Long appId){
        //1.校验参数
        validateInput(result);
        //2.构建保存目录
        String baseDirPath = buildUniqueDir(appId);
        //3.保存文件
        saveFiles(result,baseDirPath);
        //4.返回文件信息
        return new File(baseDirPath);
    }

    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码结果对象不能为空");
        }
    }


    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    private String buildUniqueDir(Long appId) {
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        }
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    //获取当前类型
    protected abstract CodeGenTypeEnum getCodeType();

    //保存文件的具体实现，由子类实现
    protected abstract void saveFiles(T result,String baseDirPath);

    /**
     * 写入单个文件
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }
}
