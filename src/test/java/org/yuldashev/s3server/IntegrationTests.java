package org.yuldashev.s3server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.yuldashev.s3server.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    final String serverUrl = "/storage/files";


    String pathToTestFiles = "src/test/resources/TestFilesForUpload/";

    String testFileName1 = "TestPost1File";
    String testFileName2 = "TestPost2File";

    @Test
    @Order(1)
    public void testPostNonExistingFile() {
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

    @Test
    @Order(2)
    void testPostExistingFile() {
        //Check whether if the file exists
        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(serverUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                });
        List<String> allFilesList = responseEntity.getBody();

        Assertions.assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
        Assertions.assertThat(allFilesList).isNotNull();
        Assertions.assertThat(allFilesList.size() > 0).isTrue();
        Assertions.assertThat(allFilesList.stream().anyMatch(fileName -> fileName.equals(testFileName1))).isTrue();


        //POST a different file but with the same name
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(pathToTestFiles + testFileName1));
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);

        Assertions.assertThat(HttpStatus.OK).isEqualTo(response.getStatusCode());
//        System.out.println(response.getStatusCode());
        //TODO check the contains of the file (after downloading from S3)
//        Assertions.assertThat(fs.exists("1.txt")).isTrue();
//        MockMultipartFile file = new MockMultipartFile("file", "1.txt", "text/plain", "this is file 1".getBytes());
//        mockMvc.perform(multipart("/storage/files").file(file))
//                .andDo(print())
//                .andExpect(status().isConflict());
//        Assertions.assertThat(TestUtil.getContent(fs, "1.txt")).isEqualTo("this is an original file");
    }

    @Test
    @Order(3)
    public void testList() {
        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(serverUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>(){
        });

        List<String> allFilesList = responseEntity.getBody();

        Assertions.assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
        Assertions.assertThat(allFilesList).isNotNull();
        System.out.println("Got the files: " + Arrays.toString(allFilesList.toArray()));
        Assertions.assertThat(allFilesList.size() > 0).isTrue();
        Assertions.assertThat(allFilesList.stream().anyMatch(fileName -> fileName.equals(testFileName1))).isTrue();

    }

    @Test
    @Order(4)
    void testGetExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(get("/storage/files/1.txt"))
//                .andDo(print())
//                .andExpect(content().string(containsString("this is file 1")));
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName1,
                HttpMethod.GET, null, new ParameterizedTypeReference<byte[]>(){
        });


        Assertions.assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        String fileContent = new String(responseEntity.getBody());
        System.out.println("Got the file content: " + fileContent);



        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            File myObj = new File(pathToTestFiles + testFileName1);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                resultStringBuilder.append(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("No test file. " + e.getMessage());
        }
        Assertions.assertThat(fileContent).isEqualTo(resultStringBuilder.toString());
    }

    @Test
    @Order(5)
    void testDeleteExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(delete("/storage/files/1.txt"))
//                .andExpect(status().is2xxSuccessful());
//        Assertions.assertThat(fs.exists("1.txt")).isFalse();
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName1,
                HttpMethod.DELETE, null, new ParameterizedTypeReference<byte[]>(){
        });
        Assertions.assertThat(HttpStatus.OK).isEqualTo(responseEntity.getStatusCode());
    }

    @Test
    @Order(6)
    void testGetNonExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(get("/storage/files/3.txt"))
//                .andExpect(status().isNotFound());
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName1,
                HttpMethod.GET, null, new ParameterizedTypeReference<byte[]>(){
        });


        Assertions.assertThat(HttpStatus.NOT_FOUND).isEqualTo(responseEntity.getStatusCode());
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        String responseMsg = new String(responseEntity.getBody());
        System.out.println("Got the response: " + responseMsg);
        Assertions.assertThat(responseMsg).contains("\"status\":404,\"error\":\"Not Found\",\"message\":\"The specified key does not exist.");
    }

//
//    @org.junit.jupiter.api.Test
//    void testDeleteNonExistingFile() throws Exception {
//        putContent(fs, "1.txt", "this is file 1");
//        mockMvc.perform(delete("/storage/files/3.txt"))
//                .andExpect(status().isNotFound());
//        Assertions.assertThat(fs.exists("1.txt")).isTrue();
//    }


}
