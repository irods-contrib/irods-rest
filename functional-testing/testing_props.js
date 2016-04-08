/**
 * Holder (hard coded at first) to handle properties to use in testing
 * Created by mconway on 4/8/16.
 */


exports.host = "localhost";
exports.port = "8080";
exports.user1 = "test1";
exports.password1 = "test";
exports.anonymous = "anonymous";
exports.protocol = 'http://';
exports.context = "/irods-rest/rest";

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