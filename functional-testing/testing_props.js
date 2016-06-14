/**
 * Holder (hard coded at first) to handle properties to use in testing
 * Created by mconway on 4/8/16.
 */

var fs = require('fs');
var path = require('path');
var FormData = require('form-data');

exports.host = "localhost";
exports.port = "8080";
exports.user1 = "test1";
exports.password1 = "test";
exports.anonymous = "anonymous";
exports.protocol = 'http://';
exports.context = "/irods-rest/rest";
exports.zone = "tempZone";
exports.ticket1 = "frisby-test-tkt1";

/**
 * Return the url up to the given resource (protocol://host:port/context/resource)
 * @param resource rest resource in the URL string
 */
exports.urlPrefix = function(resource)  {
    if (!resource) {
        throw("missing resource");
    }

    return exports.protocol + exports.host + ":" + exports.port + exports.context +"/" + resource;

};

/**
 * Get the path to the file1 test file
 * @returns {*}
 */
exports.testfile1Path = function() {
  return path.resolve(__dirname, './sample-data/file1.txt');
}

/**
 * Get the user home path
 * @param zone
 * @param user
 * @returns {string}
 */
exports.userHome = function(zone, user) {

    if (!zone) {
        throw("missing zone");
    }

    if (!user) {
        throw("missing user");
    }

    return "/" + zone + "/home/" + user;


};