package com.browxy.balloons.domain.compiler.response;

import com.browxy.balloons.domain.compiler.message.Message;
import com.browxy.balloons.domain.compiler.message.MessageFactory;

public class ResponseHandler {
	private Message message;
	private ResponseMessage responseMessage;

	public ResponseHandler(String jsonMessage) {
		this.message = MessageFactory.createMessage(jsonMessage);
		this.responseMessage = ResponseFactory.createResponse(this.message);
	}

	public String getResponse() {
		return this.responseMessage.handleClientRequest();
	}
}
