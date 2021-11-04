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
import java.util.List;
import java.util.Map;

public class GetMessagesHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(GetMessagesHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);
		List<Message> messages;

		try {
			Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
			String userId = pathParameters.get("user_id");
			LOG.info("Getting Messages for " + userId);
			messages = DynamoAdapter.getInstance().getMessages(userId);
		} catch (Exception e) {
			LOG.error(e, e);
			Response responseBody = new Response("Failure getting messages", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
					.build();
		}
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(messages)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();

	}
}
