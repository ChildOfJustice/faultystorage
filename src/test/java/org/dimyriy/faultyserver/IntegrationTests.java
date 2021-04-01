package org.dimyriy.faultyserver;

import org.dimyriy.faultyserver.filesystem.impl.InMemoryInMemoryFileSystemImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InMemoryInMemoryFileSystemImpl fs;

    @Test
    public void exampleTest() {
        fs.put("smth", new byte[1]);
        String body = this.restTemplate.getForObject("/storage/files", String.class);
        assertThat(body).isEqualTo("[\"smth\"]");
    }

}
