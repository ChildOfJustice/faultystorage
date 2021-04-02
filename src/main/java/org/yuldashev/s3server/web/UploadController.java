package org.yuldashev.s3server.web;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.yuldashev.s3server.util.AppConfiguration;
import org.yuldashev.s3server.util.Util;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.yuldashev.s3server.validation.Constants.FILENAME_REGEX;

@RestController
@RequestMapping("/storage")
public class UploadController {
    Logger logger = LoggerFactory.getLogger(UploadController.class);

    @NotNull
    private final AppConfiguration config;
    @NotNull
    private final AmazonS3 s3client;


    @Autowired
    public UploadController() {
        @NotNull AppConfiguration _config;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            _config = objectMapper.readValue(new File("src/main/resources/config.json"), AppConfiguration.class);
        } catch (IOException e) {
            logger.error("Cannot read the configuration file: " + e.getMessage());
            _config = null;
        }
        config = _config;
        if (config == null) {
            logger.error("No AWS configuration provided!");
            System.exit(-9);
        }

        AWSCredentials credentials = new BasicAWSCredentials(
                config.accessKeyId,
                config.secretAccessKey
        );
        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(config.region)
                .build();
    }

    @GetMapping(value = "files", produces = MediaType.APPLICATION_JSON_VALUE)
    @Validated
    public List<String> list() {
        ObjectListing objectListing = s3client.listObjects(config.bucketName);
        List<String> objectNames = new ArrayList<>();
        for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
            objectNames.add(os.getKey());
        }

        return objectNames;
    }

    @PostMapping(value = "files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Validated
    public ResponseEntity<?> upload(@RequestParam("file") @NotNull(message = "'file' should be present") final MultipartFile file) {
        final String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filename should be present");
        }
        try {
            final byte[] content = file.getBytes();

            s3client.putObject(config.bucketName, originalFilename, new String(content));

            return ResponseEntity.ok().build();

        } catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "uploading failed");
        } catch (SdkClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @NotNull
    @DeleteMapping(value = "files/{filename}")
    @Validated
    public ResponseEntity<?> delete(@PathVariable
                                    @NotNull(message = "filename should be provided")
                                    @Pattern(regexp = FILENAME_REGEX, message = "filename is invalid") final String filename) {
        try {
            s3client.deleteObject(new DeleteObjectRequest(config.bucketName, filename));
        } catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (SdkClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @NotNull
    @GetMapping(value = "files/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Validated
    public ResponseEntity<Resource> download(@PathVariable
                                             @NotNull(message = "filename should be provided")
                                             @Pattern(regexp = FILENAME_REGEX, message = "filename is invalid") final String filename) {

        System.out.println("Downloading an object");
        ResponseHeaderOverrides headerOverrides = new ResponseHeaderOverrides()
                .withCacheControl("No-cache")
                .withContentDisposition("attachment; filename=" + filename);
        GetObjectRequest getObjectRequestHeaderOverride = new GetObjectRequest(config.bucketName, filename)
                .withResponseHeaders(headerOverrides);
        byte[] fileContent = null;
        try {
            S3Object fullObject = s3client.getObject(getObjectRequestHeaderOverride);
            fileContent = IOUtils.toByteArray(fullObject.getObjectContent());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (SdkClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        if (fileContent == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "byte array is null");
        }
        return ResponseEntity.ok(Util.asOctetStream(fileContent));
    }

}
