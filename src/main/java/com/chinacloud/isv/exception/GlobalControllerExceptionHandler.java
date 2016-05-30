package com.chinacloud.isv.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

	private Logger logger = LogManager.getLogger(GlobalControllerExceptionHandler.class);
	
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody ErrorMessage handleException(Exception e){
		logger.info(e.getMessage(),e);
		return new ErrorMessage(e.getLocalizedMessage());	
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody ErrorMessage handleAllException(Exception e){
		logger.info(e.getMessage(),e);
		return new ErrorMessage(e.getLocalizedMessage());
	}
}
