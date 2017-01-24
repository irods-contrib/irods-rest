FROM tomcat:jre8-alpine
LABEL organization="RENCI"
LABEL maintainer="michael_conway@unc.edu"
LABEL description="iRODS Core REST API."
#VOLUME ["/etc/irods-ext/"]
# this code sets up a java cert, if no cert is needed use the nocert version
#ENV JAVA_CACERTS=$JAVA_HOME/jre/lib/security/cacerts
#ADD server.crt /tmp
#RUN echo yes | keytool -keystore JAVA_CACERTS -storepass changeit -importcert -alias myca -file /tmp/server.crt
ADD target/irods-rest.war /usr/local/tomcat/webapps/
CMD ["catalina.sh", "run"]
#ENTRYPOINT ["sh"] 



# build: docker build -t diceunc/rest:4.1.10.0-RC1 .

# run:  docker run -i -t --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  --add-host irods419.irodslocal:172.16.250.100 diceunc/rest:4.1.10.0-RC1
