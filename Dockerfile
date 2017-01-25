FROM tomcat:jre8-alpine
LABEL organization="RENCI"
LABEL maintainer="michael_conway@unc.edu"
LABEL description="iRODS Core REST API."
ADD runit.sh /

ADD target/irods-rest.war /usr/local/tomcat/webapps/
CMD ["/runit.sh"]



# build: docker build -t diceunc/rest:4.1.10.0-RC1 .

# run:  docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  --add-host irods419.irodslocal:172.16.250.100 diceunc/rest:4.1.10.0-RC1
