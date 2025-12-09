FROM tomcat:11-jdk25

RUN sed -i 's/port="8080"/port="3000"/' /usr/local/tomcat/conf/server.xml

RUN rm -rf /usr/local/tomcat/webapps/*

COPY ROOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 3000

CMD ["catalina.sh", "run"]
