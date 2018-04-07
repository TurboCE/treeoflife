FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/treeoflife.jar /treeoflife/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/treeoflife/app.jar"]
