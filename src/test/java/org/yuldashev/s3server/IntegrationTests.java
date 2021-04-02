package org.yuldashev.s3server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.yuldashev.s3server.web.UploadController;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    Logger logger = LoggerFactory.getLogger(UploadController.class);


    final String serverUrl = "/storage/files";


    String pathToTestFiles = "src/test/resources/TestFilesForUpload/";

    String testFileName = "TestPost1File";

    @Test
    @Order(1)
    public void testPostNonExistingFile() {
        System.out.println("Testing POST new a file");

        Assertions.assertThat(restTemplate).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(pathToTestFiles + testFileName));
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    @Order(2)
    public void testList() {
        System.out.println("Testing GET all files");

        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(serverUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                });

        List<String> allFilesList = responseEntity.getBody();

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(allFilesList).isNotNull();
        System.out.println("Got the files: " + Arrays.toString(allFilesList.toArray()));
        Assertions.assertThat(allFilesList.size() > 0).isTrue();
        Assertions.assertThat(allFilesList.stream().anyMatch(fileName -> fileName.equals(testFileName))).isTrue();
    }

    @Test
    @Order(3)
    void testGetExistingFile() {
        System.out.println("Testing GET a concrete file");

        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName,
                HttpMethod.GET, null, new ParameterizedTypeReference<byte[]>() {});
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        String fileContent = new String(responseEntity.getBody());
        System.out.println("Got the file content: " + fileContent);


        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            File myObj = new File(pathToTestFiles + testFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                resultStringBuilder.append(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            logger.error("There is no test file. " + e.getMessage());
        }
        Assertions.assertThat(fileContent).isEqualTo(resultStringBuilder.toString());
    }


    @Test
    @Order(4)
    void testUpdateExistingFile() {
        System.out.println("Testing POST an existing file with updating it's content");

        //Check whether if the file exists
        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(serverUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                });
        List<String> allFilesList = responseEntity.getBody();

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(allFilesList).isNotNull();
        Assertions.assertThat(allFilesList.size() > 0).isTrue();
        Assertions.assertThat(allFilesList.stream().anyMatch(fileName -> fileName.equals(testFileName))).isTrue();


        //Changing the file contents
        String newFileContent = "test text version 2";
        try {
            Writer fileWriter = new FileWriter(pathToTestFiles + testFileName);
            fileWriter.write(newFileContent);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.error("An error occurred with file overwriting " + e.getMessage());
        }

        //POST a different file contents
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(pathToTestFiles + testFileName));
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


        ResponseEntity<byte[]> getFileResponse = restTemplate.exchange(serverUrl + "/" + testFileName,
                HttpMethod.GET, null, new ParameterizedTypeReference<byte[]>() {});

        Assertions.assertThat(getFileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(getFileResponse.getBody()).isNotNull();
        String fileContent = new String(getFileResponse.getBody());
        System.out.println("Got the file content: " + fileContent);


        Assertions.assertThat(fileContent).isEqualTo(newFileContent);
    }


    @Test
    @Order(5)
    void testDeleteExistingFile() {
        System.out.println("Testing DELETE an existing file");
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName,
                HttpMethod.DELETE, null, new ParameterizedTypeReference<byte[]>() {});
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(6)
    void testGetNonExistingFile() {
        System.out.println("Testing GET non existing file");
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName,
                HttpMethod.GET, null, new ParameterizedTypeReference<byte[]>() {});

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(responseEntity.getBody()).isNotNull();
        String responseMsg = new String(responseEntity.getBody());
        System.out.println("Got the response: " + responseMsg);
        Assertions.assertThat(responseMsg).contains("\"status\":404,\"error\":\"Not Found\",\"message\":\"The specified key does not exist.");
    }


    @Test
    @Order(7)
    void testDeleteNonExistingFile() {
        System.out.println("Testing DELETE non existing file");
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(serverUrl + "/" + testFileName,
                HttpMethod.DELETE, null, new ParameterizedTypeReference<byte[]>() {});

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNull();
    }
}
