package com.serverless.db;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.serverless.data.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoAdapter implements Adapter{

    private final AmazonDynamoDB client;
    private final String SERVICE_ENDPOINT = "https://dynamodb.us-east-1.amazonaws.com";
    private final String SERVICE_REGION = "us-east-1";
    private final static DynamoAdapter SINGLE_ADAPTER = new DynamoAdapter();
    private static final Logger LOG = LogManager.getLogger(DynamoAdapter.class);

    private DynamoAdapter() {
        client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(SERVICE_ENDPOINT, SERVICE_REGION)).build();
        LOG.info("DynamoDB client Created!");
    }

    public static DynamoAdapter getInstance(){
        return SINGLE_ADAPTER;
    }

    @Override
    public List<Message> getMessages(String userId) throws IOException {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":user", new AttributeValue().withS(userId));
        DynamoDBQueryExpression<Message> queryExpression = new DynamoDBQueryExpression<Message>()
                .withKeyConditionExpression("user_id = :user ")
                .withExpressionAttributeValues(values);
        return mapper.query(Message.class, queryExpression);
    }

    @Override
    public void putMessage(Message message) throws IOException {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(message);
    }

}
