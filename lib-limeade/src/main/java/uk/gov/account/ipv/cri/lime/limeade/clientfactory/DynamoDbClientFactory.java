package uk.gov.account.ipv.cri.lime.limeade.clientfactory;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import uk.gov.account.ipv.cri.lime.limeade.awsconfig.AWSSDKHttpClientConfig;

import java.util.ArrayList;
import java.util.List;

public final class DynamoDbClientFactory {

    private static DynamoDbClient dynamoDbClient = null;

    private static List<ExecutionInterceptor> executionInterceptors = new ArrayList<>();

    private DynamoDbClientFactory() {
        // Intended
    }

    public static synchronized void registerExecutionInterceptor(
            ExecutionInterceptor executionInterceptor) {
        if (dynamoDbClient != null) {
            throw new IllegalStateException("Cannot register interceptor after client is built");
        }
        executionInterceptors.add(executionInterceptor);
    }

    public static synchronized DynamoDbClient getDynamoDbClient() {
        if (dynamoDbClient == null) {
            DynamoDbClientBuilder dynamoDbClientBuilder = DynamoDbClient.builder();

            dynamoDbClientBuilder.credentialsProvider(
                    AWSSDKHttpClientConfig.AWS_CREDENTIALS_PROVIDER);
            dynamoDbClientBuilder.region(AWSSDKHttpClientConfig.AWS_REGION);
            dynamoDbClientBuilder.httpClient(AWSSDKHttpClientConfig.SDK_HTTP_CLIENT);
            dynamoDbClientBuilder.defaultsMode(AWSSDKHttpClientConfig.DEFAULTS_MODE);

            if (!executionInterceptors.isEmpty()) {
                ClientOverrideConfiguration.Builder overrideBuilder =
                        ClientOverrideConfiguration.builder();
                executionInterceptors.forEach(overrideBuilder::addExecutionInterceptor);
                dynamoDbClientBuilder.overrideConfiguration(overrideBuilder.build());
            }

            dynamoDbClient = dynamoDbClientBuilder.build();

            // This is done to avoid holding references
            executionInterceptors = null;
        }

        return dynamoDbClient;
    }
}
