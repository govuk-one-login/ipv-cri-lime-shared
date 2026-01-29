package uk.gov.di.ipv.cri.lime.limeade.clientfactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class DynamoDbClientFactoryTest {

    @SystemStub private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void setUp() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");
    }

    @Test
    void testCreateDynamoDbClient() {
        assertDoesNotThrow(
                () -> {
                    DynamoDbClient dynamoDbClient = DynamoDbClientFactory.getDynamoDbClient();

                    assertNotNull(dynamoDbClient);
                });
    }

    @Test
    void testReturnTheSameDynamoDbClientWhenCalledMultipleTimes() {
        assertDoesNotThrow(
                () -> {
                    DynamoDbClient dynamoDbClient1 = DynamoDbClientFactory.getDynamoDbClient();

                    assertNotNull(dynamoDbClient1);

                    DynamoDbClient dynamoDbClient2 = DynamoDbClientFactory.getDynamoDbClient();

                    assertNotNull(dynamoDbClient2);

                    assertEquals(dynamoDbClient1, dynamoDbClient2);
                });
    }
}
