package org.dimyriy.faultyserver;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;


import org.dimyriy.faultyserver.filesystem.impl.InMemoryInMemoryFileSystemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;
import java.io.FileWriter;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;


    final String serverUrl = "/storage/files";

//    @Test
//    public void exampleTest() throws Exception {
//        fs.put("smth", new byte[1]);
//        String body = this.restTemplate.getForObject("/storage/files", String.class);
//        assertThat(body).isEqualTo("[\"smth\"]");
//    }


    String pathToTestFiles = "src/test/resources/TestFilesForUpload/";

    String testFileName1 = "TestPost1File";
    String testFileName2 = "TestPost2File";

    @Test
    @Order(1)
    public void testPostNonExistingFile() throws Exception {
        Assertions.assertThat(restTemplate).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(pathToTestFiles + testFileName1));
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);

        Assertions.assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());

//
//        List<String> expectedVINList = Stream.of("FR45212A24D4SED66", "FR4EDED2150RFT5GE", "XDFR6545DF3A5R896",
//                "XDFR64AE9F3A5R78S", "PQERS2A36458E98CD", "194678S400005", "48955460210").collect(Collectors.toList());
//
//        ResponseEntity<List<Vehicle>> responseEntity = this.restTemplate.exchange(baseUrl + port + "/demo/vehicles",
//                HttpMethod.GET, null, new ParameterizedTypeReference<List<Vehicle>>() {
//                });
//
//        List<Vehicle> vehiclesResponseList = responseEntity.getBody();
//
//        assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
//        assertTrue(vehiclesResponseList.size() > 7);
//        assertTrue(vehiclesResponseList.stream().anyMatch((vehicle) -> {
//            return expectedVINList.contains(vehicle.getVin());
//        }));
    }

//    @Test
//    void testPostExistingFile() throws Exception {
//        //Check whether if the file exists
//        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(serverUrl,
//            HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
//        });
//        List<String> allFilesList = responseEntity.getBody();
//
//        Assertions.assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
//        Assertions.assertThat(allFilesList).isNotNull();
//        Assertions.assertThat(allFilesList.size() > 0).isTrue();
//        Assertions.assertThat(allFilesList.stream().anyMatch(fileName -> fileName.equals(testFileName1))).isTrue();
//
//
//
//
//
//
//
//        Assertions.assertThat(fs.exists("1.txt")).isTrue();
//        MockMultipartFile file = new MockMultipartFile("file", "1.txt", "text/plain", "this is file 1".getBytes());
//        mockMvc.perform(multipart("/storage/files").file(file))
//                .andDo(print())
//                .andExpect(status().isConflict());
//        Assertions.assertThat(TestUtil.getContent(fs, "1.txt")).isEqualTo("this is an original file");
//    }

    @Test
    @Order(2)
    public void testList() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(get("/storage/files"))
//                .andDo(print())
//                .andExpect(content().string(containsString("1.txt")));
        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(serverUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
        });
        List<String> allFilesList = responseEntity.getBody();

        Assertions.assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
        Assertions.assertThat(allFilesList).isNotNull();
        System.out.println("!!!" + Arrays.toString(allFilesList.toArray()));
        //Assertions.assertThat(allFilesList.size() > 0).isTrue();
        //Assertions.assertThat(allFilesList.stream().anyMatch(fileName -> fileName.equals(testFileName1))).isTrue();

    }
//
//    @org.junit.jupiter.api.Test
//    void testGetExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(get("/storage/files/1.txt"))
//                .andDo(print())
//                .andExpect(content().string(containsString("this is file 1")));
//    }
//
//    @org.junit.jupiter.api.Test
//    void testGetNonExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(get("/storage/files/3.txt"))
//                .andExpect(status().isNotFound());
//    }
//
//    @org.junit.jupiter.api.Test
//    void testDeleteExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(delete("/storage/files/1.txt"))
//                .andExpect(status().is2xxSuccessful());
//        Assertions.assertThat(fs.exists("1.txt")).isFalse();
//    }
//
//    @org.junit.jupiter.api.Test
//    void testDeleteNonExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(delete("/storage/files/3.txt"))
//                .andExpect(status().isNotFound());
//        Assertions.assertThat(fs.exists("1.txt")).isTrue();
//    }




}
