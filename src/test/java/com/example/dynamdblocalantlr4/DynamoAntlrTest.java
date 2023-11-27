package com.example.dynamdblocalantlr4;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DynamoAntlrTest {

    public static final String DYNAMODB_LOCAL_PORT = "8080";
    private static DynamoDBProxyServer dynamoDB;
    private static DynamoDbClient dynamoDbClient;

    @BeforeAll
    static void setupDynamoDB() throws Exception {
        System.setProperty("aws.accessKeyId", "dummyAccessKey");
        System.setProperty("aws.secretAccessKey", "dummySecretKey");
        System.setProperty("sqlite4java.library.path", "native-libs");

        dynamoDB = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", DYNAMODB_LOCAL_PORT});
        dynamoDB.start();

        dynamoDbClient = DynamoDbClient.builder()
                .region(Region.EU_WEST_1)
                .endpointOverride(URI.create("http://localhost:" + DYNAMODB_LOCAL_PORT))
                .build();
    }

    @AfterAll
    static void tearDownDynamoDB() throws Exception {
        dynamoDB.stop();
    }

    @Test
    void test() {
        createTable();
        saveItem();
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#KEY_id", "id");
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":VAL_id", AttributeValue.builder().s("1").build());

        QueryRequest request = QueryRequest.builder()
                .tableName("test")
                .scanIndexForward(Boolean.TRUE)
                .limit(20)
                .exclusiveStartKey(null)
                .indexName("idIndex")
                .expressionAttributeValues(expressionValues)
                .expressionAttributeNames(expressionNames)
                .keyConditionExpression(Expression.builder().expression("#KEY_id=:VAL_id").build().expression())
                .build();

        var result = dynamoDbClient.query(request);
    }

    private static void createTable() {
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName("test")
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName("idIndex")
                                .keySchema(
                                        KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("id").build(),
                                        KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName("idRange").build()
                                )
                                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build())
                                .projection(Projection.builder().projectionType(ProjectionType.KEYS_ONLY).build())
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("idRange").attributeType(ScalarAttributeType.S).build())
                .keySchema(KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("id").build())
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build())
                .build();
        dynamoDbClient.createTable(createTableRequest);
    }

    private static void saveItem() {
        Map<String, AttributeValue> id = Map.of("id", AttributeValue.builder().s("1").build());
        PutItemRequest putItemRequest = PutItemRequest.builder().tableName("test").item(id).build();
        dynamoDbClient.putItem(putItemRequest);
    }

}
