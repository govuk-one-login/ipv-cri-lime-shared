package uk.gov.account.ipv.cri.lime.limeade.clientfactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(SystemStubsExtension.class)
class DynamoDbClientFactoryTest {

    @SystemStub
    private static EnvironmentVariables environmentVariables =
            new EnvironmentVariables("AWS_REGION", "eu-west-2");

    private static void resetFactory() throws Exception {
        Field clientField = DynamoDbClientFactory.class.getDeclaredField("dynamoDbClient");
        clientField.setAccessible(true);
        clientField.set(null, null);

        Field interceptorsField =
                DynamoDbClientFactory.class.getDeclaredField("executionInterceptors");
        interceptorsField.setAccessible(true);
        interceptorsField.set(null, new java.util.ArrayList<>());
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @ExtendWith(MockitoExtension.class)
    class WithoutInterceptor {

        @Mock private DynamoDbClientBuilder mockBuilder;
        @Mock private DynamoDbClient mockClient;

        @BeforeAll
        static void beforeAll() throws Exception {
            resetFactory();
        }

        @Test
        @Order(1)
        void buildsClientWithoutOverrideConfigWhenNoInterceptorsRegistered() {
            try (MockedStatic<DynamoDbClient> mockedStatic = mockStatic(DynamoDbClient.class)) {
                mockedStatic.when(DynamoDbClient::builder).thenReturn(mockBuilder);
                when(mockBuilder.credentialsProvider(any())).thenReturn(mockBuilder);
                when(mockBuilder.region(any())).thenReturn(mockBuilder);
                when(mockBuilder.httpClient(any())).thenReturn(mockBuilder);
                when(mockBuilder.defaultsMode(any())).thenReturn(mockBuilder);
                when(mockBuilder.build()).thenReturn(mockClient);

                DynamoDbClient result = DynamoDbClientFactory.getDynamoDbClient();

                assertNotNull(result);
                verify(mockBuilder, never())
                        .overrideConfiguration(any(ClientOverrideConfiguration.class));
            }
        }

        @Test
        @Order(2)
        void returnsSameInstanceOnSubsequentCalls() {
            DynamoDbClient first = DynamoDbClientFactory.getDynamoDbClient();
            DynamoDbClient second = DynamoDbClientFactory.getDynamoDbClient();

            assertEquals(first, second);
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @ExtendWith(MockitoExtension.class)
    class WithInterceptor {

        @Mock private DynamoDbClientBuilder mockBuilder;
        @Mock private DynamoDbClient mockClient;
        @Mock private ExecutionInterceptor mockInterceptor;

        @BeforeAll
        static void beforeAll() throws Exception {
            resetFactory();
        }

        @Test
        @Order(1)
        void buildsClientWithOverrideConfigWhenInterceptorRegistered() {
            try (MockedStatic<DynamoDbClient> mockedStatic = mockStatic(DynamoDbClient.class)) {
                mockedStatic.when(DynamoDbClient::builder).thenReturn(mockBuilder);
                when(mockBuilder.credentialsProvider(any())).thenReturn(mockBuilder);
                when(mockBuilder.region(any())).thenReturn(mockBuilder);
                when(mockBuilder.httpClient(any())).thenReturn(mockBuilder);
                when(mockBuilder.defaultsMode(any())).thenReturn(mockBuilder);
                when(mockBuilder.overrideConfiguration(any(ClientOverrideConfiguration.class)))
                        .thenReturn(mockBuilder);
                when(mockBuilder.build()).thenReturn(mockClient);

                DynamoDbClientFactory.registerExecutionInterceptor(mockInterceptor);
                DynamoDbClient result = DynamoDbClientFactory.getDynamoDbClient();

                assertNotNull(result);
                InOrder inOrder = inOrder(mockBuilder);
                inOrder.verify(mockBuilder)
                        .overrideConfiguration(any(ClientOverrideConfiguration.class));
                inOrder.verify(mockBuilder).build();
            }
        }

        @Test
        @Order(2)
        void returnsSameInstanceOnSubsequentCalls() {
            DynamoDbClient first = DynamoDbClientFactory.getDynamoDbClient();
            DynamoDbClient second = DynamoDbClientFactory.getDynamoDbClient();

            assertEquals(first, second);
        }

        @Test
        @Order(3)
        void throwsWhenRegisteringInterceptorAfterClientBuilt() {
            assertThrows(
                    IllegalStateException.class,
                    () -> DynamoDbClientFactory.registerExecutionInterceptor(mockInterceptor));
        }
    }
}
