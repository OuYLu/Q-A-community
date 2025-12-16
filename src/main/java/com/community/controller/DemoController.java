package com.community.controller;

import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @GetMapping("/success")
    public Result<String> success() {
        return Result.success("demo success");
    }

    @GetMapping("/error")
    public Result<Void> error() {
        throw new BizException(ErrorCode.BIZ_ERROR);
    }

    @GetMapping("/arg")
    public Result<String> arg(@RequestParam("name") String name) {
        return Result.success("hello " + name);
    }
}
