From openjdk:17-jdk

Workdir /app

copy target/PlayStore-App.jar app.jar

Cmd ["java","-jar","app.jar"]
