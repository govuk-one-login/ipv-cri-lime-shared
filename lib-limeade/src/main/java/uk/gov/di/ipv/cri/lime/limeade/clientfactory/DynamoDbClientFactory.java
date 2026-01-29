package uk.gov.di.ipv.cri.lime.limeade.clientfactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.awssdk.v2_2.AwsSdkTelemetry;
import uk.gov.di.ipv.cri.lime.limeade.awsconfig.AWSSDKHttpClientConfig;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public final class DynamoDbClientFactory {

    private static DynamoDbClient dynamoDbClient = null;

    private DynamoDbClientFactory() {
        // Intended
    }

    public static synchronized DynamoDbClient getDynamoDbClient() {
        if (dynamoDbClient == null) {
            AwsSdkTelemetry telemetry = AwsSdkTelemetry.create(GlobalOpenTelemetry.get());
            dynamoDbClient =
                    DynamoDbClient.builder()
                            .credentialsProvider(AWSSDKHttpClientConfig.AWS_CREDENTIALS_PROVIDER)
                            .region(AWSSDKHttpClientConfig.AWS_REGION)
                            .httpClient(AWSSDKHttpClientConfig.SDK_HTTP_CLIENT)
                            .defaultsMode(AWSSDKHttpClientConfig.DEFAULTS_MODE)
                            .overrideConfiguration(
                                    ClientOverrideConfiguration.builder()
                                            .addExecutionInterceptor(telemetry.newExecutionInterceptor())
                                            .build())
                            .build();
        }

        return dynamoDbClient;
    }
}
