package br.com.dwnl.spicehub;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(PostgresTestContainerConfig.class)
@SpringBootTest
class SpicehubMarketplaceApplicationTests {

    @Test
    void contextLoads() {
    }

}
