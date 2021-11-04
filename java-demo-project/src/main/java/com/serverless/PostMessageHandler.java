package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.data.Message;
import com.serverless.db.DynamoAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class PostMessageHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(PostMessageHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		try{
			ObjectMapper mapper = new ObjectMapper();
			Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
			String userId = pathParameters.get("user_id");
			Message message = new Message();
			message.setUserId(userId);
			JsonNode body = mapper.readTree((String) input.get("body"));
			String text = body.get("text").asText();
			String messageId = body.get("message_id").asText();
			message.setText(text);
			message.setMessageDate(new Date(System.currentTimeMillis()));
			message.setMessageId(messageId);
			DynamoAdapter.getInstance().putMessage(message);
		} catch(Exception e){
			LOG.error(e,e);
			Response responseBody = new Response("Failure posting message", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
					.build();
		}

		Response responseBody = new Response("Message posted successfully!", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
