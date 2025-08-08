package com.browxy.balloons.domain.compiler.response;

import java.util.HashMap;
import java.util.Map;

import com.browxy.balloons.domain.compiler.lang.java.JavaResponseHandler;
import com.browxy.balloons.domain.compiler.message.JavaMessage;
import com.browxy.balloons.domain.compiler.message.Message;

public class ResponseFactory {

	public static ResponseMessage createResponse(Message message) {
		ResponseMessage responseMessage = getResponseType(message);
		if (responseMessage == null) {
			throw new IllegalArgumentException("Unknown message type");
		}
		return responseMessage;
	}

	private static ResponseMessage getResponseType(Message message) {
		Map<Class<? extends Message>, ResponseMessage> responseMessageMap = new HashMap<>();
		responseMessageMap.put(JavaMessage.class, new JavaResponseHandler(message));

		return responseMessageMap.get(message.getClass());

	}
}
