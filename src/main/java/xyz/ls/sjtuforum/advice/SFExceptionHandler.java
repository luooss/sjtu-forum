package xyz.ls.sjtuforum.advice;

import com.alibaba.fastjson.JSON;
import xyz.ls.sjtuforum.dto.ResultDTO;
import xyz.ls.sjtuforum.exception.SFErrorCode;
import xyz.ls.sjtuforum.exception.SFException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice
@Slf4j
public class SFExceptionHandler {

    @ExceptionHandler(Exception.class)
    ModelAndView handle(Throwable e, Model model, HttpServletRequest request, HttpServletResponse response) {
        String contentType = request.getContentType();
        if ("application/json".equals(contentType)) {
            ResultDTO resultDTO;
            if (e instanceof SFException) {
                resultDTO = ResultDTO.errorOf((SFException) e);
            } else {
                log.error("handle error", e);
                resultDTO = ResultDTO.errorOf(SFErrorCode.SYS_ERROR);
            }
            try {
                response.setContentType("application/json");
                response.setStatus(200);
                response.setCharacterEncoding("utf-8");
                PrintWriter writer = response.getWriter();
                writer.write(JSON.toJSONString(resultDTO));
                writer.close();
            } catch (IOException ioe) {
            }
            return null;
        } else {
            if (e instanceof SFException) {
                model.addAttribute("message", e.getMessage());
            } else {
                log.error("handle error", e);
                model.addAttribute("message", SFErrorCode.SYS_ERROR.getMessage());
            }
            return new ModelAndView("error");
        }
    }
}
