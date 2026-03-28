package com.jzy.aicodeplatform.core;

import cn.hutool.json.JSONUtil;
import com.jzy.aicodeplatform.ai.AiCodeGeneratorFactory;
import com.jzy.aicodeplatform.ai.AiCodeGeneratorService;
import com.jzy.aicodeplatform.ai.model.HtmlCodeResult;
import com.jzy.aicodeplatform.ai.model.MultiFileCodeResult;
import com.jzy.aicodeplatform.ai.model.message.AiResponseMessage;
import com.jzy.aicodeplatform.ai.model.message.ToolExecutedMessage;
import com.jzy.aicodeplatform.ai.model.message.ToolRequestMessage;
import com.jzy.aicodeplatform.core.parse.CodeParserExecutor;
import com.jzy.aicodeplatform.core.saver.CodeFileSaverExecutor;
import com.jzy.aicodeplatform.exception.BusinessException;
import com.jzy.aicodeplatform.exception.ErrorCode;
import com.jzy.aicodeplatform.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {


    @Resource
    private AiCodeGeneratorFactory aiCodeGeneratorFactory;


    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 当流式返回生成代码完成后，再保存代码

        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
//                Flux<String> codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
//                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
                TokenStream result = aiCodeGeneratorService.generateVueProjectCodeTokenStream(appId, userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.VUE_PROJECT, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };

    }


    /**
     * 通用生成代码并保存（流式）
     *
     * @param result
     * @param codeGenTypeEnum
     * @return 保存的目录
     */
    private Flux<String> processCodeStream(Flux<String> result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunk -> {
                    // 实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeHtmlCode = codeBuilder.toString();
                        Object parsedResult = CodeParserExecutor.executeParser(completeHtmlCode, codeGenTypeEnum);
                        // 保存代码到文件
                        File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenTypeEnum, appId);
                        log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                    }
                });
    }


    /*
     *通用生成代码并保存（流式）
     *
     * @param result
     * @param codeGenTypeEnum
     * @return 保存的目录
     */
    private Flux<String> processCodeStream(TokenStream tokenStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        Flux<String> fluxWithSave = Flux.create(sink -> {
            tokenStream
                    .onPartialResponse(partial -> {
                        // 保留完整文本用于最终解析和落盘
                        codeBuilder.append(partial);
                        //转换对应结构体
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partial);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onIntermediateResponse(intermediateResponse -> {
                        //工具请求内容
                        if (intermediateResponse.aiMessage() == null
                                || intermediateResponse.aiMessage().toolExecutionRequests() == null) {
                            return;
                        }
                        intermediateResponse.aiMessage().toolExecutionRequests()
                                .forEach(toolExecutionRequest -> sink.next(
                                        JSONUtil.toJsonStr(new ToolRequestMessage(toolExecutionRequest))
                                ));
                    })
                    .onToolExecuted(toolExecution -> {
                                //工具执行结果
                                ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                                sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                            }
                    )
                    .onCompleteResponse(completeResponse -> {
                        try {
                            String completeHtmlCode = codeBuilder.toString();
                            Object parsedResult = CodeParserExecutor.executeParser(completeHtmlCode, codeGenTypeEnum);
                            // 保存代码到文件
                            File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenTypeEnum, appId);
                            log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                        } catch (Exception e) {
                            log.error("保存失败: {}", e.getMessage());
                        }
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });

        return fluxWithSave;

    }

}

