#################################
# step.1 : Application 빌드 단계 #
#################################
# 기반이되는 image를 하나 선택한다.
FROM amazoncorretto:17-alpine as builder

# Working Directory 설정하기
WORKDIR /app

# Gradle 관련 파일들을 복사한다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# gradlew 실행권한을 준다
RUN chmod +x ./gradlew

# 소스 코드 복사
COPY src src

# JAR 파일 빌드
# bootJar: 실행 가능한 JAR만 생성(-plain 안만듬)
RUN ./gradlew clean bootJar -x test --no-daemon

# 빌드된 JAR파일 이름을 변경
RUN mv build/libs/*.jar app.jar


#################################
# step.1 : Application 실행 단계 #
#################################
FROM amazoncorretto:17-alpine

WORKDIR /app

# 환경변수를 만듬
ENV TZ=Asia/Seoul

COPY --from=builder /app/app.jar app.jar

# 어플리케이션에서 사용할 포트 설정(강제성 없음)
EXPOSE 8070

# spring boot application 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 사용 방법
# 1. 이미지 빌드(이미지 만들기)
#   docker build -t auth .
#   ex)
#   docker build -t myapp:v1.0 .


# 2. 컨테이너로 실행하기
# docker run -d --name auth -p 8070:8070 \
# -e DB_URL=jdbc:mysql://host.docker.internal:3306/mannal?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true \
# -e DB_USERNAME=root \
# -e DB_PASSWORD=1234 \
# auth