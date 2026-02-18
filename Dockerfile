FROM ubuntu:latest
LABEL authors="fedor"

ENTRYPOINT ["top", "-b"]