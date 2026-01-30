package uk.gov.di.ipv.cri.lime.limeade.clientfactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkTelemetry;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.gov.di.ipv.cri.lime.limeade.awsconfig.AWSSDKHttpClientConfig;

public final class DynamoDbClientFactory {

    private static DynamoDbClient dynamoDbClient = null;

    private DynamoDbClientFactory() {
        // Intended
    }

    public static synchronized DynamoDbClient getDynamoDbClient() {
        if (dynamoDbClient == null) {
            dynamoDbClient =
                    DynamoDbClient.builder()
                            .credentialsProvider(AWSSDKHttpClientConfig.AWS_CREDENTIALS_PROVIDER)
                            .region(AWSSDKHttpClientConfig.AWS_REGION)
                            .httpClient(AWSSDKHttpClientConfig.SDK_HTTP_CLIENT)
                            .defaultsMode(AWSSDKHttpClientConfig.DEFAULTS_MODE)
                            .overrideConfiguration(
                                    ClientOverrideConfiguration.builder()
                                            .addExecutionInterceptor(
                                                    AwsSdkTelemetry.create(
                                                                    GlobalOpenTelemetry.get())
                                                            .newExecutionInterceptor())
                                            .build())
                            .build();
        }

        return dynamoDbClient;
    }
}
