/**
 * NOTE: This class is auto generated by the swagger code generator program (1.0.16).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.irods.jargon.rest.base.api;

import org.irods.jargon.rest.base.annotations.*;
import org.irods.jargon.rest.base.model.PathInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Api(value = "path", description = "the path API")
public interface PathApi {

    @ApiOperation(value = "search and retrieve information on an iRODS path", nickname = "searchPaths", notes = "By passing in the appropriate options, you can search for available paths (files or collections)  ", response = PathInfo.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "search results matching criteria", response = PathInfo.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "bad input parameter") })
    @RequestMapping(value = "/path",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<PathInfo>> searchPaths(@ApiParam(value = "pass an iRODS path to retrieve. If the like parameter is not supplied, this method will look for an exact match. With the like parameter this method will treat as LIKE path% and return all matches.  ") @Valid @RequestParam(value = "path", required = false) String path,@ApiParam(value = "indicates the path is search with LIKE path% type query ", defaultValue = "false") @Valid @RequestParam(value = "like", required = false, defaultValue="false") Boolean like,@Min(0)@ApiParam(value = "number of records to skip for pagination") @Valid @RequestParam(value = "skip", required = false) Integer skip,@Min(0) @Max(50) @ApiParam(value = "maximum number of records to return") @Valid @RequestParam(value = "limit", required = false) Integer limit);

}
