package org.irods.jargon.rest.base.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.irods.jargon.rest.base.annotations.*;
import org.irods.jargon.rest.base.model.PathInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Controller
public class PathApiController implements PathApi {

    private static final Logger log = LoggerFactory.getLogger(PathApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public PathApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<List<PathInfo>> searchPaths(@ApiParam(value = "pass an iRODS path to retrieve. If the like parameter is not supplied, this method will look for an exact match. With the like parameter this method will treat as LIKE path% and return all matches.  ") @Valid @RequestParam(value = "path", required = false) String path,@ApiParam(value = "indicates the path is search with LIKE path% type query ", defaultValue = "false") @Valid @RequestParam(value = "like", required = false, defaultValue="false") Boolean like,@Min(0)@ApiParam(value = "number of records to skip for pagination") @Valid @RequestParam(value = "skip", required = false) Integer skip,@Min(0) @Max(50) @ApiParam(value = "maximum number of records to return") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<PathInfo>>(objectMapper.readValue("[ {  \"modifyDate\" : \"2016-08-29T09:12:33.001Z\",  \"name\" : \"file1\",  \"absolutePath\" : \"/zone/home/user/file1\",  \"createDate\" : \"2016-08-29T09:12:33.001Z\"}, {  \"modifyDate\" : \"2016-08-29T09:12:33.001Z\",  \"name\" : \"file1\",  \"absolutePath\" : \"/zone/home/user/file1\",  \"createDate\" : \"2016-08-29T09:12:33.001Z\"} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<PathInfo>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<PathInfo>>(HttpStatus.NOT_IMPLEMENTED);
    }

}
