FROM alpine

COPY /build/libs/web-scraping-1.0.0-SNAPSHOT.jar web-scraping.jar
COPY /locale/ /locale/