package uk.gov.account.ipv.cri.lime.limeade.awsconfig;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.regions.Region;
import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

@ExcludeClassFromGeneratedCoverageReport
public final class AWSSDKHttpClientConfig {

    public static final Region AWS_REGION = Region.of(System.getenv("AWS_REGION"));

    public static final AwsCredentialsProvider AWS_CREDENTIALS_PROVIDER =
            System.getenv("AWS_CONTAINER_CREDENTIALS_FULL_URI") == null
                    ? EnvironmentVariableCredentialsProvider.create()
                    : ContainerCredentialsProvider.builder().build();

    public static final DefaultsMode DEFAULTS_MODE = DefaultsMode.IN_REGION;

    public static final SdkHttpClient SDK_HTTP_CLIENT = AwsCrtHttpClient.builder().build();

    private AWSSDKHttpClientConfig() {
        // Intended
    }
}
